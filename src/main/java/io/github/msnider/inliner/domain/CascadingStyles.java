package io.github.msnider.inliner.domain;

import io.github.msnider.inliner.utils.CSSUtils;
import io.github.msnider.inliner.utils.URLUtils;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.phloc.commons.charset.CCharset;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSDeclaration;
import com.phloc.css.decl.CSSExpressionMemberTermURI;
import com.phloc.css.decl.CSSImportRule;
import com.phloc.css.decl.CSSMediaRule;
import com.phloc.css.decl.CSSURI;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ICSSTopLevelRule;
import com.phloc.css.decl.visit.AbstractModifyingCSSUrlVisitor;
import com.phloc.css.decl.visit.CSSVisitor;
import com.phloc.css.decl.visit.DefaultCSSUrlVisitor;
import com.phloc.css.handler.LoggingCSSParseExceptionHandler;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.reader.errorhandler.LoggingCSSParseErrorHandler;
import com.phloc.css.writer.CSSWriter;
import com.phloc.css.writer.CSSWriterSettings;

public class CascadingStyles {
	private static final Logger logger = LoggerFactory.getLogger(CascadingStyles.class);

	private final URI baseURI;
	private final UserAgent userAgent;
	private String styles;
	private ResponseHeaders responseHeaders = null;

	public CascadingStyles(URL url, UserAgent userAgent) throws URISyntaxException {
		if (url == null)
			throw new IllegalArgumentException("Argument `url` cannot be null");
		this.baseURI = url.toURI();
		this.userAgent = userAgent;
		
		HttpRequest request = HttpRequest.get(url)
				.followRedirects(true)
				.userAgent(userAgent.getUAString());
		if (request.ok()) {
			this.styles = request.body();
			this.responseHeaders = new ResponseHeaders(request.headers());
		} else {
			this.styles = "";
		}
	}
	
	public CascadingStyles(URL url, String styles, UserAgent userAgent) throws URISyntaxException {
		if (url == null)
			throw new IllegalArgumentException("Argument `url` cannot be null");
		if (styles == null)
			throw new IllegalArgumentException("Argument `styles` cannot be null");
		if (userAgent == null)
			throw new IllegalArgumentException("Argument `userAgent` cannot be null");
		
		this.baseURI = url.toURI();
		this.userAgent = userAgent;
		this.styles = styles;
	}
	
	public CascadingStyles restrictToMediaQuery(String mediaQuery) {
		if (mediaQuery == null || mediaQuery.trim().isEmpty())
			return this;
		this.styles = String.format("@import url('%s') %s;", baseURI.toString(), mediaQuery);
		return this;
	}
	
	public CascadingStyles attachHeaders(HttpServletResponse response) {
		if (this.responseHeaders != null) {
			this.responseHeaders.attachCacheHeaders(response);
		}
		return this;
	}
	
	public String inline() {
		CascadingStyleSheet css = null;
		try {
			css = CSSReader.readFromString(
					styles, CCharset.CHARSET_UTF_8_OBJ, ECSSVersion.LATEST,
				new LoggingCSSParseErrorHandler(),//DoNothingCSSParseErrorHandler.getInstance(),
				new LoggingCSSParseExceptionHandler());//DoNothingCSSParseExceptionHandler.getInstance());
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage());
			return "";
		}

		// Build URLs in the CSS to be absolute from the original document
		CSSVisitor.visitCSSUrl(css, new AbstractModifyingCSSUrlVisitor() {
			@Override
			protected String getModifiedURI(String sURI) {
				return URLUtils.resolve(baseURI, sURI);
			}
		});
		
		// Remove blocks of media queries where non-matching to media query specs
		if (userAgent != null) {
			if (css.hasMediaRules()) {
				// Iterate through each Rule, if its a MediaQuery, remove it if it doesn't match
				// Go backwards through the list so we can easily delete elements as necessary
				for(int i = css.getRuleCount(); i >= 0; --i) {
					ICSSTopLevelRule topLevelRule = css.getRuleAtIndex(i);
					if (topLevelRule instanceof CSSMediaRule) {
						CSSMediaRule mediaRule = (CSSMediaRule) topLevelRule;
						if (mediaRule.hasMediaQueries() && mediaRule.hasRules()) {
							if (CSSUtils.matchesMediaQueries(mediaRule.getAllMediaQueries(), userAgent)) {
								// If these media queries do match, then all the rules below will apply
								// Insert them after this rule, one-by-one
								for(int j = mediaRule.getRuleCount(); j >= 0; --j) {
									ICSSTopLevelRule mediaSubRule = mediaRule.getRule(j);
									if (mediaSubRule != null)
										css.addRule(i + j + 1, mediaSubRule);
								}
							}
							
							// Remove the Media Query Rule
							css.removeRule(i);
						}
					}
				}
			}
		}
		
		// Grab CSS for All @import declarations & inline data-uri's where possible
		final StringBuilder importedCSS = new StringBuilder();
		CSSVisitor.visitCSSUrl(css, new DefaultCSSUrlVisitor() {
			@Override
			public void onImport(CSSImportRule aImportRule) {
				// Add the import if the media query matches, recurses on the imported CSS
				if (CSSUtils.matchesMediaQueries(aImportRule.getAllMediaQueries(), userAgent)) {
					final String sURI = aImportRule.getLocationString();
	
					try {
						String importCSS = new CascadingStyles(new URL(sURI), userAgent).inline();
						if (importCSS != null) {
							importedCSS.append(importCSS).append("\n");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onUrlDeclaration(ICSSTopLevelRule aTopLevelRule,
					CSSDeclaration aDeclaration,
					CSSExpressionMemberTermURI aURITerm) {
				CSSURI cssURI = aURITerm.getURI();
				if (!cssURI.isDataURL()) {
					// Convert non-data-uri's to data-uri's
					try {
						HttpRequest request = HttpRequest.get(cssURI.getURI()).userAgent(userAgent.getUAString());
						if (request.ok()) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							request.receive(baos);
							
							// 2) Convert to data-uri
							// TODO: Should we also sniff the content-type by file extension?
							String mimeType = request.contentType();
							String base64 = Base64.encodeBase64String(baos.toByteArray());
							String dataURI = "data:" + mimeType + ";base64," + base64;
							
							// 3) Replace existing URL with data-uri
							aURITerm.setURI(new CSSURI(dataURI));
						}
					} catch (HttpRequestException e) {
						logger.error(e.getLocalizedMessage());
					}
				}
			}
		});
		
		// Remove all import rules
		css.removeAllImportRules();

		// Output the concatenated CSS
		CSSWriterSettings writeSettings = new CSSWriterSettings(ECSSVersion.CSS30, false); // Optimizer = false
		CSSWriter writer = new CSSWriter(writeSettings);
		writer.setWriteHeaderText(false);
		writer.setWriteFooterText(false);
		return importedCSS.toString() + "\n" + writer.getCSSAsString(css);
	}
}

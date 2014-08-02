package io.github.msnider.inliner.domain;

import io.github.msnider.inliner.utils.URLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kevinsawicki.http.HttpRequest;

public class HTML {
	private static final Logger logger = LoggerFactory.getLogger(HTML.class);
	
	private final URI baseURI;
	private final String baseURL;
	private final UserAgent userAgent;
	private final Document doc;
	
	// TODO: Make sure we use the right charset
	public HTML(URI baseUrl, String html, UserAgent userAgent) throws IOException {
		this.baseURI = baseUrl;
		this.baseURL = baseURI.toString();
		this.userAgent = userAgent;
		
		InputStream in = new ByteArrayInputStream(html.getBytes());
		this.doc = Jsoup.parse(in, Charset.defaultCharset().name(), baseURL);
	}

	public HTML(URI baseUrl, UserAgent userAgent) throws IOException {
		this.baseURI = baseUrl;
		this.baseURL = baseURI.toString();
		this.userAgent = userAgent;
		this.doc = Jsoup.connect(baseURL)
				.userAgent(userAgent.getUAString())
				.timeout(5000)
				.followRedirects(true)
				.get();
	}
	
	public String inline() {
		// TODO: Check for a base tag and adjust?
		
		// [1] Remove Script & NoScript tags
		for (Element element : doc.select("script,noscript")) {
			element.remove();
		}
		
		// [2] All links should become no-follows & absolute
		for (Element element : doc.select("a[href]")) {
			element.attr("rel", "nofollow");
			element.attr("target", "_blank");
			
			String href = element.attr("href");
			if (href != null && !href.isEmpty()) {
				String url = resolveURL(href);
				element.attr("href", url);
			}
		}
		
		// [3] Re-Process Style tags (in-case they link to images or other)
		//	MUST come before Stylesheet inlining or we'll do a lot of extra work
		for (Element element : doc.select("style")) {
			String rawStyles = element.data();
			logger.info("=== STYLE TAG ===");
			logger.info("Doc Base URI: " + doc.baseUri());
			logger.info("=== /STYLE TAG ===");
			try {
				String css = new CascadingStyles(new URL(doc.baseUri()), rawStyles, userAgent).inline();
				if (css != null && !css.trim().isEmpty()) {
					Element styleTag = new Element(Tag.valueOf("style"), doc.baseUri());
					DataNode styles = new DataNode(css.trim(), doc.baseUri());
					styleTag.appendChild(styles);
					element.replaceWith(styleTag);
				}
			} catch (MalformedURLException e) {
				//e.printStackTrace();
			} catch (URISyntaxException e) {
				//e.printStackTrace();
			}
		}
		
		// [4] Inline all Stylesheets
		for (Element linkTag : doc.select("link")) {
			String type = linkTag.attr("type");
			String rel = linkTag.attr("rel");
			String href = linkTag.attr("href");
			String url = resolveURL(href);

			// TODO: Technically, this could be a data-uri too, but its highly unlikely
			logger.info("HREF: " + href);
			logger.info("URL: " + url);
			
			// Either the type is text/css OR the rel is stylesheet, or neither
			if (	(rel == null && type == null) || 
					(type != null && type.equalsIgnoreCase("text/css")) ||
					(rel != null && rel.equalsIgnoreCase("stylesheet"))
					) {
				String media = linkTag.attr("media");
				if (media == null)
					media = "";
				String fauxStyle = String.format("@import url(%s) %s;", url, media);
				try {
					String css = new CascadingStyles(new URL(url), fauxStyle, userAgent).inline();
					if (css != null && !css.trim().isEmpty()) {
						// Replace with a Style tag
						Element styleTag = new Element(Tag.valueOf("style"), doc.baseUri());
						DataNode styles = new DataNode(css.trim(), doc.baseUri());
						styleTag.appendChild(styles);
						linkTag.replaceWith(styleTag);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		
		// [5] Remove all remaining <link> tags
		doc.select("link").remove();
		
		// [6] Remove META REFRESH tag(s) if present
		doc.select("meta[http-equiv=refresh]").remove();
		
		// [7] Add a meta noindex tag so this page is not indexable
		Element metaNoIndex = new Element(Tag.valueOf("meta"), doc.baseUri());
		metaNoIndex.attr("name", "robots");
		metaNoIndex.attr("content", "noindex");
		doc.head().prependChild(metaNoIndex);

		// [8] Inline images
		for (Element element : doc.select("img")) {
			String src = element.attr("src");
			if (src != null && !src.startsWith("data:")) {
				String url = resolveURL(src);
				
				HttpRequest request = HttpRequest.get(url)
						.followRedirects(true)
						.userAgent(userAgent.getUAString());
				if (request.ok()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					request.receive(baos);
					String mimeType = request.contentType();
					String base64 = Base64.encodeBase64String(baos.toByteArray());
					String dataURI = "data:" + mimeType + ";base64," + base64;
					element.attr("src", dataURI);
				}
			}
		}
		
		// [9] Process Browser Conditionals - should we just ignore these?
		// NOTE: There is a bug in Jsoup that prevents this from working correctly...
		// TODO: Parse conditional comments correctly
		// @see https://github.com/jhy/jsoup/issues/329
		// @see https://github.com/jhy/jsoup/issues/360
		// FOR NOW, Let's just remove all comments
		for (Element element : doc.getAllElements()){
			for (Node node : element.childNodes()) {
				if (node instanceof Comment) {
					//node.remove();
				}
			}
		}
		
		// [10] Absolutize Form Actions
		for(Element element : doc.select("form")) {
			element.attr("target", "_blank");
			
			String action = element.attr("action");
			if (action != null && !action.isEmpty()) {
				String url = resolveURL(action);
				element.attr("action", url);
			}
		}
		
		// [11] Absolute URLs to all other media (iframe, video, flash, audio, etc)
		
		// Input [input.src] - TODO: convert to data-uri if possible
		for(Element element : doc.select("input[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// iFrame [iframe.src]
		for(Element element : doc.select("iframe[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// Frame [frameset > frame.src]
		for(Element element : doc.select("frame[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// Object [object.data, object.codebase, object.archive]
		for(Element element : doc.select("object")) {
			String data = element.attr("data");
			if (data != null && !data.isEmpty()) {
				String url = resolveURL(data);
				element.attr("data", url);
			}
			
			String codebase = element.attr("codebase");
			if (codebase != null && !codebase.isEmpty()) {
				String url = resolveURL(codebase);
				element.attr("codebase", url);
			}
			
			String archive = element.attr("archive");
			if (archive != null && !archive.isEmpty()) {
				String url = resolveURL(archive);
				element.attr("archive", url);
			}
		}
		
		// Embed [embed.src]
		for(Element element : doc.select("embed[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// Source [audio > source.src, video > source.src]
		for(Element element : doc.select("source[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// Track [video > track.src]
		for(Element element : doc.select("track[src]")) {
			String src = element.attr("src");
			if (src != null && !src.isEmpty()) {
				String url = resolveURL(src);
				element.attr("src", url);
			}
		}
		
		// Input [input.formtarget] - Never seen this one in the wild
		for(Element element : doc.select("input[formtarget]")) {
			String formtarget = element.attr("formtarget");
			if (formtarget != null && !formtarget.isEmpty()) {
				String url = resolveURL(formtarget);
				element.attr("formtarget", url);
			}
		}
		
		// Applet [applet.code] - Not supported in HTML5, use <object>
		for(Element element : doc.select("applet[code]")) {
			String code = element.attr("code");
			if (code != null && !code.isEmpty()) {
				String url = resolveURL(code);
				element.attr("code", url);
			}
		}
		
		doc.outputSettings().prettyPrint(true);
		return doc.html();
	}
	
	protected String resolveURL(String inURL) {
		try {
			return URLUtils.resolve(baseURI, inURL);
		} catch (Exception e) {
			return inURL;
		}
	}
}

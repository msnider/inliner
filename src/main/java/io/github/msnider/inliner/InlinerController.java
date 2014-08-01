package io.github.msnider.inliner;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import io.github.msnider.inliner.domain.CascadingStyles;
import io.github.msnider.inliner.domain.UserAgent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class InlinerController {
	private static final String DEFAULT_USER_AGENT = 
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";
	
	/**
	 * Given the contents of a <style> tag, a base URL, and User Agent parameters,
	 * this method will return the inlined CSS.
	 * 
	 * @param url
	 * @param styles
	 * @param ua
	 * @param width
	 * @param height
	 * @param deviceWidth
	 * @param deviceHeight
	 * @param devicePixelRatio
	 * @param defaultFontSizePx
	 * @param request
	 * @param response
	 * @return inlined CSS
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	@RequestMapping(value = "/styles.css", 
			method = { RequestMethod.GET, RequestMethod.POST }, 
			produces = { "text/css" }, 
			params = { "styles" })
	public @ResponseBody String styleTag(
			@RequestParam(value = "url", required = true) String url,
			@RequestParam(value = "styles", defaultValue = "") String styles,
			@RequestParam(value = "ua", defaultValue = DEFAULT_USER_AGENT) String ua,
			@RequestParam(value = "width", required = true) Integer width,
			@RequestParam(value = "height", required = true) Integer height,
			@RequestParam(value = "deviceWidth", required = false) Integer deviceWidth,
			@RequestParam(value = "deviceHeight", required = false) Integer deviceHeight,
			@RequestParam(value = "devicePixelRatio", defaultValue = "1.0") Double devicePixelRatio,
			@RequestParam(value = "defaultFontSizePx", defaultValue = "16") Integer defaultFontSizePx,
			HttpServletRequest request, HttpServletResponse response) 
					throws MalformedURLException, URISyntaxException {
		if (deviceWidth == null)
			deviceWidth = width;
		if (deviceHeight == null)
			deviceHeight = height;
		
		UserAgent userAgent = new UserAgent(ua, 
				width, height, 
				deviceWidth, deviceHeight, 
				devicePixelRatio, defaultFontSizePx);
		CascadingStyles cascadingStyles = new CascadingStyles(new URL(url), styles, userAgent);
		return cascadingStyles.inline();
	}
	
	/**
	 * Given the contents of a style attribute, a base URL, and User Agent parameters,
	 * this method will return the inlined CSS.
	 * 
	 * @param url
	 * @param attributeStyles
	 * @param ua
	 * @param width
	 * @param height
	 * @param deviceWidth
	 * @param deviceHeight
	 * @param devicePixelRatio
	 * @param defaultFontSizePx
	 * @param request
	 * @param response
	 * @return inlined CSS
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	@RequestMapping(value = "/styles.css", 
			method = { RequestMethod.GET, RequestMethod.POST }, 
			produces = { "text/css" }, 
			params = { "attribute" })
	public @ResponseBody String styleAttribute(
			@RequestParam(value = "url", required = true) String url,
			@RequestParam(value = "attribute", defaultValue = "") String attributeStyles,
			@RequestParam(value = "ua", defaultValue = DEFAULT_USER_AGENT) String ua,
			@RequestParam(value = "width", required = true) Integer width,
			@RequestParam(value = "height", required = true) Integer height,
			@RequestParam(value = "deviceWidth", required = false) Integer deviceWidth,
			@RequestParam(value = "deviceHeight", required = false) Integer deviceHeight,
			@RequestParam(value = "devicePixelRatio", defaultValue = "1.0") Double devicePixelRatio,
			@RequestParam(value = "defaultFontSizePx", defaultValue = "16") Integer defaultFontSizePx,
			HttpServletRequest request, HttpServletResponse response) 
					throws MalformedURLException, URISyntaxException {
		if (deviceWidth == null)
			deviceWidth = width;
		if (deviceHeight == null)
			deviceHeight = height;
		
		UserAgent userAgent = new UserAgent(ua, 
				width, height, 
				deviceWidth, deviceHeight, 
				devicePixelRatio, defaultFontSizePx);
		CascadingStyles cascadingStyles = new CascadingStyles(new URL(url), String.format("div{ %s }", attributeStyles), userAgent);
		String styles = cascadingStyles.inline();
		return styles.substring(styles.indexOf("{") + 1, styles.lastIndexOf("}")).trim();
	}
	
	/**
	 * Given the URL and media query of a <link> tag and User Agent parameters,
	 * this method will return the inlined CSS.
	 * 
	 * @param url
	 * @param mediaQuery
	 * @param ua
	 * @param width
	 * @param height
	 * @param deviceWidth
	 * @param deviceHeight
	 * @param devicePixelRatio
	 * @param defaultFontSizePx
	 * @param request
	 * @param response
	 * @return inlined CSS
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	@RequestMapping(value = "/styles.css", 
			method = { RequestMethod.GET }, 
			produces = { "text/css" })
	public @ResponseBody String linkTag(
			@RequestParam(value = "url", required = true) String url,
			@RequestParam(value = "mediaQuery", defaultValue = "") String mediaQuery,
			@RequestParam(value = "ua", defaultValue = DEFAULT_USER_AGENT) String ua,
			@RequestParam(value = "width", required = true) Integer width,
			@RequestParam(value = "height", required = true) Integer height,
			@RequestParam(value = "deviceWidth", required = false) Integer deviceWidth,
			@RequestParam(value = "deviceHeight", required = false) Integer deviceHeight,
			@RequestParam(value = "devicePixelRatio", defaultValue = "1.0") Double devicePixelRatio,
			@RequestParam(value = "defaultFontSizePx", defaultValue = "16") Integer defaultFontSizePx,
			HttpServletRequest request, HttpServletResponse response) 
					throws MalformedURLException, URISyntaxException {
		if (deviceWidth == null)
			deviceWidth = width;
		if (deviceHeight == null)
			deviceHeight = height;
		
		UserAgent userAgent = new UserAgent(ua, 
				width, height, 
				deviceWidth, deviceHeight, 
				devicePixelRatio, defaultFontSizePx);
		CascadingStyles cascadingStyles = new CascadingStyles(new URL(url), userAgent)
			.restrictToMediaQuery(mediaQuery)
			.attachHeaders(response);
		return cascadingStyles.inline();
	}
}

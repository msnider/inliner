package io.github.msnider.inliner.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLUtils {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);
	
	private static final String EMPTY = "";
	
	public static boolean isDataURI(String url) {
		return url != null && url.startsWith("data:");
	}
	
	public static boolean isJavaScriptURI(String url) {
		return url != null && url.startsWith("javascript:");
	}
	
	public static String resolve(URL baseURL, String relativeURL) {
		if (relativeURL == null || relativeURL.trim().isEmpty())
			return relativeURL;
		if (isDataURI(relativeURL))
			return relativeURL;
		if (isJavaScriptURI(relativeURL))
			return EMPTY;
		
		try {
			return new URL(baseURL, relativeURL).toExternalForm().replaceAll("/../", "/");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// ignore this error and return the original relative url
		}
		return relativeURL;
	}

	public static String resolve(URI baseURI, String relativeURL) {
		if (relativeURL == null || relativeURL.trim().isEmpty())
			return relativeURL;
		if (isDataURI(relativeURL))
			return relativeURL;
		if (isJavaScriptURI(relativeURL))
			return EMPTY;
		
		try {
			return new URL(baseURI.toURL(), relativeURL).toExternalForm().replaceAll("/../", "/");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// ignore this error and return the original relative url
		}
		return relativeURL;
	}
}

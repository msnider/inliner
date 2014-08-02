package io.github.msnider.inliner.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLUtils {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);
	
	public static boolean isDataURI(String url) {
		return url != null && url.startsWith("data:");
	}

	public static String resolve(URI baseURI, String relativeURL) {
		if (relativeURL == null || relativeURL.isEmpty())
			return relativeURL;
		if (isDataURI(relativeURL))
			return relativeURL;
		
		try {
			return new URL(baseURI.toURL(), relativeURL).toExternalForm().replaceAll("/../", "/");
			//URI uri = baseURI.resolve(relativeURL);
			//return uri.toString();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return relativeURL;
	}
}

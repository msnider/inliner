package io.github.msnider.inliner.utils;

import java.net.URL;

import com.github.kevinsawicki.http.HttpRequest;

public class HttpUtils {

	public static HttpRequest getRequest(String url, String ua) {
		return HttpRequest.get(url)
				.acceptGzipEncoding()
				.trustAllCerts()
				.ignoreCloseExceptions(true)
				.uncompress(true)
				.followRedirects(true)
				.userAgent(ua);
	}
	
	public static HttpRequest getRequest(URL url, String ua) {
		return HttpRequest.get(url)
				.acceptGzipEncoding()
				.trustAllCerts()
				.ignoreCloseExceptions(true)
				.uncompress(true)
				.followRedirects(true)
				.userAgent(ua);
	}
}

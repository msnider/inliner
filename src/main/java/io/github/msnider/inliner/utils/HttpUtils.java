package io.github.msnider.inliner.utils;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.kevinsawicki.http.HttpRequest;

public class HttpUtils {

	public static HttpRequest getRequest(String url, String ua) {
		try {
			return getRequest(new URL(url), ua);
		} catch (MalformedURLException e) {
			throw new HttpRequest.HttpRequestException(e);
		}
	}
	
	public static HttpRequest getRequest(URL url, String ua) {
		HttpRequest req =  HttpRequest.get(url)
				.acceptGzipEncoding()
				.trustAllCerts()
				.ignoreCloseExceptions(true)
				.uncompress(true)
				.followRedirects(true)
				.userAgent(ua);
		//int code = req.code();
		
		// Handle the case in which the redirect fails to be followed, this would occur
		// when its being redirected from HTTP to HTTPS (protocol changes)
		/*if (code == HttpURLConnection.HTTP_MOVED_PERM ||
				code == HttpURLConnection.HTTP_MOVED_TEMP) {
			String location = req.header("Location");
			if (location != null && !location.trim().isEmpty()) {
				return getRequest(location, ua);
			}
		}*/
		return req;
	}
}

package io.github.msnider.inliner.domain;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

//Age:123345
// Cache-Control:public, max-age=31536000
// Connection:keep-alive
// Content-Encoding:gzip
// Content-Type:text/css
// Date:Sat, 12 Jul 2014 04:21:28 GMT
// Expires:Sun, 12 Jul 2015 04:21:28 GMT
// Last-Modified:Mon, 26 May 2014 22:48:44 GMT
// Server:cloudflare-nginx
// Transfer-Encoding:chunked
// Vary:Accept-Encoding
public class ResponseHeaders {
	
	public void attachCacheHeaders(HttpServletResponse response) {
		if (this.cacheControl != null) {
			response.setHeader("Cache-Control", cacheControl);
		}
		if (this.expires != null) {
			response.setHeader("Expires", expires);
		}
		if (this.lastModified != null) {
			response.setHeader("Last-Modified", lastModified);
		}
		if (this.pragma != null) {
			response.setHeader("Pragma", pragma);
		}
	}
	
	public ResponseHeaders() {
		this(null);
		// TODO: Setup a default of 5 minutes cache
	}
	
	public ResponseHeaders(Map<String, List<String>> headers) {
		if (headers != null) {
			for(String key : headers.keySet()) {
				if (key == null || key.isEmpty() || !headers.containsKey(key))
					continue;
				String value = headers.get(key).get(0);
				switch (key.trim().toLowerCase()) {
					case "age":
						this.age = value;
						break;
					case "cache-control":
						this.cacheControl = value;
						break;
					case "date":
						this.date = value;
						break;
					case "expires":
						this.expires = value;
						break;
					case "last-modified":
						this.lastModified = value;
						break;
					case "pragma":
						this.pragma = value;
						break;
					default:
				}
			}
		}
	}

	/**
	 * Age:123345
	 * The Age response-header field conveys the sender's estimate of the 
	 * amount of time since the response (or its revalidation) was generated 
	 * at the origin server.
	 */
	@SuppressWarnings("unused")
	private String age = null;
	
	/**
	 * Cache-Control:public, max-age=31536000
	 */
	private String cacheControl = null;
	
	/**
	 * Date:Sat, 12 Jul 2014 04:21:28 GMT
	 */
	@SuppressWarnings("unused")
	private String date = null;
	
	/**
	 * Expires:Sun, 12 Jul 2015 04:21:28 GMT
	 */
	private String expires = null;
	
	/**
	 * Last-Modified:Mon, 26 May 2014 22:48:44 GMT
	 */
	private String lastModified = null;
	
	/**
	 * Pragma: no-cache
	 */
	private String pragma = null;
}

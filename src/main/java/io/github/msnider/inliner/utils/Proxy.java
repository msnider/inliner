package io.github.msnider.inliner.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;

import com.github.kevinsawicki.http.HttpRequest;

public class Proxy {
	
	private final String proxyUrl;
	private final String proxyParam;
	private final boolean shouldProxy;

	public Proxy(String proxyUrl, String proxyParam) {
		this.proxyUrl = proxyUrl;
		this.proxyParam = proxyParam;
		this.shouldProxy = (this.proxyParam != null && this.proxyUrl != null && 
				!this.proxyParam.trim().isEmpty() && !this.proxyUrl.trim().isEmpty());
	}
	
	protected String buildProxiedUrl(String url) {
		if (!shouldProxy)
			return url;
		try {
			URIBuilder b = new URIBuilder(this.proxyUrl);
			b.addParameter(this.proxyParam, url);
			return b.build().toURL().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	protected URL buildProxiedUrl(URL url) {
		if (!shouldProxy)
			return url;
		try {
			URIBuilder b = new URIBuilder(this.proxyUrl);
			b.addParameter(this.proxyParam, url.toString());
			return b.build().toURL();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public HttpRequest getRequest(String url, String ua) {
		return HttpUtils.getRequest(buildProxiedUrl(url), ua);
	}
	
	public HttpRequest getRequest(URL url, String ua) {
		return HttpUtils.getRequest(url, ua);
	}
}

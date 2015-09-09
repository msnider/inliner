package io.github.msnider.inliner;

import io.github.msnider.inliner.domain.HTML;
import io.github.msnider.inliner.domain.UserAgent;
import io.github.msnider.inliner.utils.Proxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class InlinerService {

	/**
	 * Inlines the HTML `contents` and accompanying CSS
	 * @param url
	 * @param contents
	 * @param ua
	 * @param width
	 * @param height
	 * @param deviceWidth
	 * @param deviceHeight
	 * @param devicePixelRatio
	 * @param defaultFontSizePx
	 * @return inlined HTML document
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	public static String htmlUrl(
			String url,
			String contents,
			String ua,
			Integer width,
			Integer height,
			Integer deviceWidth,
			Integer deviceHeight,
			Double devicePixelRatio,
			Integer defaultFontSizePx,
			Proxy proxy) 
					throws URISyntaxException, IOException {
		if (deviceWidth == null)
			deviceWidth = width;
		if (deviceHeight == null)
			deviceHeight = height;
		if (devicePixelRatio == null)
			devicePixelRatio = 1.0;
		if (defaultFontSizePx == null)
			defaultFontSizePx = 16;
		
		UserAgent userAgent = new UserAgent(ua, 
				width, height, 
				deviceWidth, deviceHeight, 
				devicePixelRatio, defaultFontSizePx);
		HTML html = (contents == null || contents.isEmpty()) ? 
				new HTML(new URI(url), userAgent, proxy) : new HTML(new URI(url), contents, userAgent, proxy);
		return html.inline();
	}
	
}

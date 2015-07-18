package io.github.msnider.inliner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class CommandLineApp {

	public static void main(String[] args) throws Exception {
		// Parse Command Line provided properties
		String url = System.getProperty("page.url");
		String ua = System.getProperty("page.ua");
		Integer width = toInteger(System.getProperty("page.width"), null);
		Integer height = toInteger(System.getProperty("page.height"), null);
		Integer deviceWidth = toInteger(System.getProperty("page.deviceWidth"), width);
		Integer deviceHeight = toInteger(System.getProperty("page.deviceHeight"), height);
		Double devicePixelRatio = toDouble(System.getProperty("page.devicePixelRatio"), 1.0);
		Integer defaultFontSizePx = toInteger(System.getProperty("page.defaultFontSizePx"), 14);
		
		// Read input file from STDIN
		StringBuilder builder = new StringBuilder();
		BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
		String input;
		while((input = br.readLine()) != null){
			builder.append(input);
		}
		
		// Inline HTML
		try {
			String html = InlinerService.htmlUrl(
					url, 
					builder.toString(), 
					ua, 
					width, 
					height, 
					deviceWidth, 
					deviceHeight, 
					devicePixelRatio,
					defaultFontSizePx);
			System.out.print(html);
			System.exit(0);
		} catch (URISyntaxException | IOException e) {
			System.err.println("Error Processing HTML: " + e.getLocalizedMessage());
			System.exit(1);
		}
	}
	
	public static Integer toInteger(String value, Integer defaultValue) {
		try {
			Integer v = Integer.valueOf(value);
			return (v == null) ? defaultValue : v;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public static Double toDouble(String value, Double defaultValue) {
		try {
			Double v = Double.valueOf(value);
			return (v == null) ? defaultValue : v;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}

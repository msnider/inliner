package io.github.msnider.inliner.domain;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

/**
 * @see http://www.quirksmode.org/css/tests/mediaqueries/
 * @see https://developer.mozilla.org/en-US/docs/Web/Guide/CSS/Media_queries
 * @author Matt Snider
 *
 * TODO: Proper em calculations
 */
public class UserAgent {
	//http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-logging
	//view-source:http://www.vogella.com/tutorials/JUnit/article.html
	//http://www.vogella.com/css/A.articles.css.pagespeed.cf.gZJqTjSP1p.css

	public boolean matchesMedium(String medium) {
		if (medium == null || medium.trim().isEmpty())
			return true;
		return mediums.contains(medium.trim().toLowerCase());
	}
	
	public boolean matchesFeature(String feature, String value) {
		switch (feature) {
			case "width":
				return (this.width == toPX(value));
			case "min-width":
				return (this.width >= toPX(value));
			case "max-width":
				return (this.width <= toPX(value));
				
			case "height":
				return (this.height == toPX(value));
			case "min-height":
				return (this.height >= toPX(value));
			case "max-height":
				return (this.height <= toPX(value));
				
			case "device-width":
				return (this.deviceWidth == toPX(value));
			case "min-device-width":
				return (this.deviceWidth >= toPX(value));
			case "max-device-width":
				return (this.deviceWidth <= toPX(value));
				
			case "device-height":
				return (this.deviceHeight == toPX(value));
			case "min-device-height":
				return (this.deviceHeight >= toPX(value));
			case "max-device-height":
				return (this.deviceHeight <= toPX(value));
				
			case "orientation":
				return (this.orientation == toOrientation(value));
				
			case "aspect-ratio":
				return (this.aspectRatio.compareTo(toRatio(value)) == 0);
			case "min-aspect-ratio":
				return (this.aspectRatio.compareTo(toRatio(value)) > 0);
			case "max-aspect-ratio":
				return (this.aspectRatio.compareTo(toRatio(value)) < 0);
				
			case "device-aspect-ratio":
				return (this.deviceAspectRatio.compareTo(toRatio(value)) == 0);
			case "min-device-aspect-ratio":
				return (this.deviceAspectRatio.compareTo(toRatio(value)) > 0);
			case "max-device-aspect-ratio":
				return (this.deviceAspectRatio.compareTo(toRatio(value)) < 0);

			// RESOLUTION & DEVICE PIXEL RATIO
			//http://drewwells.net/blog/2013/working-with-dppx/
			case "resolution":
			case "device-pixel-ratio":
			case "-webkit-device-pixel-ratio":
			case "-moz-device-pixel-ratio":
				return (this.resolution == toDPPX(value));
			case "min-resolution":
			case "min-device-pixel-ratio":
			case "-webkit-min-device-pixel-ratio":
			case "min--moz-device-pixel-ratio":
				return (this.resolution >= toDPPX(value));
			case "max-resolution":
			case "max-device-pixel-ratio":
			case "-webkit-max-device-pixel-ratio":
			case "max--moz-device-pixel-ratio":
				return (this.resolution <= toDPPX(value));
			
			// color, min-color, max-color
			// color-index, min-color-index, max-color-index
			// monochrome, min-monochrome, max-monochrome
			// scan, grid
			default:
		}
		return true;
	}
	
	public String getUAString() {
		return this.ua;
	}
	
	/**
	 * NOTE: We will also need the browser's default font-size to calculate the size of an 'em'
	 * @see http://stackoverflow.com/questions/1442542/how-can-i-get-default-font-size-in-pixels-by-using-javascript-or-jquery
	 * 
function getDefaultFontSize(pa){
 pa= pa || document.body;
 var who= document.createElement('div');

 who.style.cssText='display:inline-block; padding:0; line-height:1; position:absolute; visibility:hidden; font-size:1em';

 who.appendChild(document.createTextNode('M'));
 pa.appendChild(who);
 var fs= [who.offsetWidth, who.offsetHeight];
 pa.removeChild(who);
 return fs;
}
	 */
	
	/**
	 * @param ua is equal to navigator.userAgent (Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36).
	 * @param width is equal to window.innerWidth (1366).
	 * @param height is equal to window.innerHeight (677).
	 * @param deviceWidth is equal to screen.width (1366).
	 * @param deviceHeight is equal to screen.height (768).
	 * @param devicePixelRatio is equal to window.devicePixelRatio (1).
	 * @param defaultFontSizePx is usually 16 or can be calculated using the function above `getDefaultFontSize`
	 */
	public UserAgent(
			String ua,
			int width, int height,
			int deviceWidth, int deviceHeight,
			double devicePixelRatio,
			int defaultFontSizePx) {
		this.mediums.add("all");
		this.mediums.add("screen");
		this.ua = ua;
		this.width = width;
		this.height = height;
		this.deviceWidth = deviceWidth;
		this.deviceHeight = deviceHeight;
		this.orientation = (height >= width) ? Orientation.PORTRAIT : Orientation.LANDSCAPE;
		this.aspectRatio = new Fraction(width, height);
		this.deviceAspectRatio = new Fraction(deviceWidth, deviceHeight);
		this.resolution = devicePixelRatio;
		this.defaultFontSizePx = defaultFontSizePx;
		// color, colorIndex, monochrome, scan, grid
	}

	/**
	 * The media type theoretically says something about your device, 
	 * but in practice doesn't make the distinctions we're looking for.
	 * 
	 * The only type that works properly is print. You can actually use it 
	 * to define styles for printed Web pages. So we keep that one. I haven't 
	 * researched the aural- or braille-related types. The others, notably 
	 * handheld and tv, don't work.
	 * 
	 * Consider handheld. You'd say it would denote mobile phones, and that's 
	 * indeed exactly how it started out. However, the handheld media type was 
	 * then used to serve up crappy, simplistic styles, which were tailored 
	 * to the phones of the mid-noughties. When modern smartphones came out 
	 * they supported proper CSS, and didn't want to be associated with the 
	 * crappy handheld styles. That's why they do not suppor this media type 
	 * any more (except for Symbian WebKit).
	 * 
	 * Internet-enabled TVs turn out not to support the tv type. I'm not sure 
	 * why not; maybe it's for fear of being served simplistic styles, or maybe 
	 * because the TV vendors just didn't think of it. In any case, you can't 
	 * detect TVs by type.
	 */
	private Set<String> mediums = new HashSet<String>();
	
	/**
	 * The userAgent property returns the value of the user-agent header sent by 
	 * the browser to the server.
	 * The value returned, contains information about the name, version and platform 
	 * of the browser.
	 */
	private String ua;
	
	/**
	 * 4.1. width
	 * Value: <length>
	 * Applies to: visual and tactile media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'width' media feature describes the width of the targeted display area 
	 * of the output device. For continuous media, this is the width of the viewport 
	 * (as described by CSS2, section 9.1.1 [CSS21]) including the size of a rendered 
	 * scroll bar (if any). For paged media, this is the width of the page box 
	 * (as described by CSS2, section 13.2 [CSS21]).
	 * 
	 * A specified <length> cannot be negative.
	 * 
	 * Resolution Units: px
	 * Available Units: px, em, in, cm
	 */
	private int width;
	
	/**
	 * 4.2. height
	 * Value: <length>
	 * Applies to: visual and tactile media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'height' media feature describes the height of the targeted display area 
	 * of the output device. For continuous media, this is the height of the viewport 
	 * including the size of a rendered scroll bar (if any). For paged media, this 
	 * is the height of the page box.
	 * 
	 * A specified <length> cannot be negative.
	 * 
	 * Resolution Units: px
	 * Available Units: px, em, in, cm
	 */
	private int height;
	
	/**
	 * 4.3. device-width
	 * 
	 * Value: <length>
	 * Applies to: visual and tactile media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'device-width' media feature describes the width of the rendering surface 
	 * of the output device. For continuous media, this is the width of the screen. 
	 * For paged media, this is the width of the page sheet size.
	 * 
	 * A specified <length> cannot be negative.
	 * 
	 * Resolution Units: px
	 * Available Units: px, em, in, cm
	 */
	private int deviceWidth;
	
	/**
	 * 4.4. device-height
	 * 
	 * Value: <length>
	 * Applies to: visual and tactile media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'device-height' media feature describes the height of the rendering surface 
	 * of the output device. For continuous media, this is the height of the screen. 
	 * For paged media, this is the height of the page sheet size.
	 * 
	 * A specified <length> cannot be negative.
	 * 
	 * Resolution Units: px
	 * Available Units: px, em, in, cm
	 */
	private int deviceHeight;
	
	/**
	 * 4.5. orientation
	 * Value: portrait | landscape
	 * Applies to: bitmap media types
	 * Accepts min/max prefixes: no
	 * 
	 * The 'orientation' media feature is 'portrait' when the value of the 'height' media 
	 * feature is greater than or equal to the value of the 'width' media feature. 
	 * Otherwise 'orientation' is 'landscape'.
	 */
	private Orientation orientation;
	
	/**
	 * 4.6. aspect-ratio
	 * Value: <ratio>
	 * Applies to: bitmap media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'aspect-ratio' media feature is defined as the ratio of the value of the 
	 * 'width' media feature to the value of the 'height' media feature.
	 */
	private Fraction aspectRatio;
	
	/**
	 * 4.7. device-aspect-ratio
	 * Value: <ratio>
	 * Applies to: bitmap media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'device-aspect-ratio' media feature is defined as the ratio of the value of 
	 * the 'device-width' media feature to the value of the 'device-height' media feature.
	 */
	private Fraction deviceAspectRatio;
	
	/**
	 * 4.8. color
	 * Value: <integer>
	 * Applies to: visual media types
	 * Accept min/max prefixes: yes
	 * 
	 * The 'color' media feature describes the number of bits per color component of the 
	 * output device. If the device is not a color device, the value is zero.
	 * 
	 * A specified <integer> cannot be negative.
	 * 
	 * The color media query gives the number of bits per color that the device uses. 
	 * In practice this is usually 8.
	 * 
	 * @see http://www.quirksmode.org/css/tests/mediaqueries/color.html
	 */
	//private int color = 8;
	
	/**
	 * 4.9. color-index
	 * Value: <integer>
	 * Applies to: visual media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'color-index' media feature describes the number of entries in the color lookup 
	 * table of the output device. If the device does not use a color lookup table, the 
	 * value is zero.
	 * 
	 * A specified <integer> cannot be negative.
	 * 
	 * The color-index media query returns the number of entries in the device's color 
	 * lookup table. In practice this is always 0; I'm still looking for a device that 
	 * returns a different value.
	 */
	//private int colorIndex = 0;
	
	/**
	 * 4.10. monochrome
	 * Value: <integer>
	 * Applies to: visual media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'monochrome' media feature describes the number of bits per pixel in a monochrome 
	 * frame buffer. If the device is not a monochrome device, the output device value will 
	 * be 0.
	 * 
	 * A specified <integer> cannot be negative.
	 * 
	 * The monochrome media query tells you if the device is monochrome. That said, I ever 
	 * tested only on one monochrome device, an old Kindle, and it told me it was not a 
	 * monochrome device. So much for this media query.
	 */
	//private int monochrome = 0;
	
	/**
	 * 4.11. resolution
	 * Value: <resolution>
	 * Applies to: bitmap media types
	 * Accepts min/max prefixes: yes
	 * 
	 * The 'resolution' media feature describes the resolution of the output device, i.e. 
	 * the density of the pixels. When querying devices with non-square pixels, in 
	 * 'min-resolution' queries the least-dense dimension must be compared to the specified 
	 * value and in 'max-resolution' queries the most-dense dimensions must be compared 
	 * instead. A 'resolution' (without a "min-" or "max-" prefix) query never matches a 
	 * device with non-square pixels.
	 * 
	 * For printers, this corresponds to the screening resolution (the resolution for 
	 * printing dots of arbitrary color).
	 * 
	 * Resolution Units: dppx
	 * Available Units: dpi, dpcm, & dppx
	 */
	private double resolution;
	
	/**
	 * 4.12. scan
	 * Value: progressive | interlace
	 * Applies to: "tv" media types
	 * Accepts min/max prefixes: no
	 * 
	 * The 'scan' media feature describes the scanning process of "tv" output devices.
	 */
	//private Scan scan = Scan.PROGRESSIVE;
	
	/**
	 * 4.13. grid
	 * Value: <integer>
	 * Applies to: visual and tactile media types
	 * Accepts min/max prefixes: no
	 * 
	 * The 'grid' media feature is used to query whether the output device is grid or bitmap. 
	 * If the output device is grid-based (e.g., a "tty" terminal, or a phone display with 
	 * only one fixed font), the value will be 1. Otherwise, the value will be 0.
	 * 
	 * Only 0 and 1 are valid values. (This includes -0.) Thus everything else creates a 
	 * malformed media query.
	 */
	//private int grid = 0;
	
	/**
	 * The device-pixel-ratio media query mirrors the window.devicePixelRatio property, which 
	 * gives the number of physical pixels divided by the number of dips. See my blog post for 
	 * an explanation.
	 * 
	 * devicePixelRatio is the ratio between physical pixels and device-independent pixels 
	 * (dips) on the device.
	 * 
	 * devicePixelRatio = physical pixels / dips
	 * 
	 * Feature: device-pixel-ratio
	 * Vendor Prefixing: -moz-device-pixel-ratio, -webkit-device-pixel-ratio, -ms-device-pixel-ratio, -o-device-pixel-ratio
	 * 
	 * @see http://www.quirksmode.org/blog/archives/2012/07/more_about_devi.html
	 */
	//private double devicePixelRatio = 1.0;
	
	/**
	 * The default browser font size in pixels. Generally defaults to 16, sometimes 12.
	 */
	private int defaultFontSizePx = 16;
	
	/**
	 * Resolution Units
	 * 
	 * dpi: This unit represents the number of dots per inch. A screen typically contains 72 or 96 
	 * 	dpi; a printed document usually reach much greater dpi. As 1 inch is 2.54 cm, 1dpi ≈ 0.39dpcm.
	 * 
	 * dpcm: This unit represents the number of dots per centimeter. As 1 inch is 2.54 cm, 1dpcm ≈ 2.54dpi.
	 * 
	 * dppx: This unit represents the number of dots per px unit. Due to the 1:96 fixed ratio of CSS in 
	 * 	to CSS px, 1dppx is equivalent to 96dpi, that corresponds to the default resolution of images 
	 * 	displayed in CSS as defined by image-resolution.
	 */
	private static final Pattern REGEX_RESOLUTION_VALUES_UNITS = Pattern.compile("^([\\d.]+)\\s*(dpi|dpcm|dppx)$");
	
	protected static double toDPPX(String input) {
		Matcher matcher = REGEX_RESOLUTION_VALUES_UNITS.matcher(input.trim().toLowerCase());
		if (matcher.find() && matcher.groupCount() == 2) {
			double value = Double.parseDouble(matcher.group(1));
			String units = matcher.group(2);
			switch(units) {
				case "dpcm":
					// Convert dpcm to dpi and fall-thru
					value = value * 0.39;
				case "dpi":
					return value / 96.0;
				case "dppx":
				default:
					return value;
			}
		}
		
		// Try evaluating as straight dppx
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 1.0;
	}
	
	private static final Pattern REGEX_LENGTH_VALUES_UNITS = Pattern.compile("^([\\d.]+)\\s*(px|cm|in|em)$");
	protected double toPX(String input) {
		// Try matching with units
		Matcher matcher = REGEX_LENGTH_VALUES_UNITS.matcher(input.trim().toLowerCase());
		if (matcher.find() && matcher.groupCount() == 2) {
			double value = Double.parseDouble(matcher.group(1));
			String units = matcher.group(2);
			switch(units) {
				case "cm":
					return value / (2.54 / 72.0 * 0.75);
				case "in":
					return value / (1.0 / 72.0 * 0.75);
				case "em":
					return value / this.defaultFontSizePx;
				case "px":
				default:
					return value;
			}
		}
		
		// Try evaluating as straight pixels
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	protected Orientation toOrientation(String input) {
		try {
			return Orientation.valueOf(input.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return Orientation.PORTRAIT;
	}
	
	private static final FractionFormat FRACTION_FORMAT = new FractionFormat();
	protected Fraction toRatio(String input) {
		try {
			return FRACTION_FORMAT.parse(input);
		} catch (MathParseException e) {
			e.printStackTrace();
		}
		
		try {
			double d = Double.parseDouble(input);
			return new Fraction(d);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return new Fraction(0.0);
	}
}

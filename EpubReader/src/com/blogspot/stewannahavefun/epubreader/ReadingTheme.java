package com.blogspot.stewannahavefun.epubreader;

public class ReadingTheme {

	private static final String STYLE_ID = "-style-injected-by-epubreader";

	public ReadingTheme() {
	}

	private static String constructJSUrl(String input) {
		String js = "javascript:(function() {"
				+ input
				+ "}) ();";

		return js;
	}

	private static String constructThemeJS(String css) {
		String js = "var style = document.getElementById('" + STYLE_ID + "');"
				+ "if (style == null) {"
				+ "style = document.createElement('style');"
				+ "style.id = '" + STYLE_ID + "';"
				+ "style.setAttribute('rel', 'stylesheet');"
				+ "style.setAttribute('type', 'text/css');"
				+ "document.head.appendChild(style);"
				+ "}"
				+ "style.textContent = \"" + css + "\";";

		return js;
	}

	public static String constructThemeUrl(String css) {
		return constructJSUrl(constructThemeJS(css));
	}
}

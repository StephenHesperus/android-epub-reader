package com.blogspot.stewannahavefun.epubreader;


public class ReadingTheme {

	public ReadingTheme() {
	}

	private static String constructJSUrl(String input) {
		String js = "javascript:(function() {"
				+ input
				+ "}) ();";

		return js;
	}

	private static String constructThemeJS(String css) {
		String js = "var style = document.createElement('style');"
				+ "style.setAttribute('rel', 'stylesheet');"
				+ "style.setAttribute('type', 'text/css');"
				+ "style.textContent = '" + css + "';"
				+ "document.head.appendChild(style);";

		return js;
	}

	public static String constructThemeUrl(String css) {
		return constructJSUrl(constructThemeJS(css));
	}
}

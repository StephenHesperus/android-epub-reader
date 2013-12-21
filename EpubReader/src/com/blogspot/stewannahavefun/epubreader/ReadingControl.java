package com.blogspot.stewannahavefun.epubreader;

public class ReadingControl {

	private static final String SCRIPT_ID = "-script-injected-by-epubreader";

	public ReadingControl() {

	}

	public static String getImageListenerUrl() {
		return getJSUrl(getJS_RegisterImageListener());
	}
}

package com.blogspot.stewannahavefun.epubreader;

import com.blogspot.stewannahavefun.epubreader.ReadingActivity.WebInterface;

public class ReadingControl {

	private static final String SCRIPT_ID = "-script-injected-by-epubreader";

	public ReadingControl() {

	}

	public static String getImageListenerUrl() {
		return getJSUrl(getJS_RegisterImageListener());
	}

	private static String getJS_RegisterImageListener() {
		String registerJS = "var images = document.getElementsByTagName('img');"
				+ "function onImageClickListener(e) {"
				+ "var src = this.src;"
				+ WebInterface.getInterfaceName()
				+ "."
				+ "onImageClick(src);"
				+ "}"
				+ "for (var i = 0; i < images.length; i++) {"
				+ "images[i].addEventListener('click', onImageClickListener, false);"
				+ "}";

	private static String getJS_AppendScriptToBodyEnd(String scriptContent) {
		String js = "var script = document.getElementById('" + SCRIPT_ID
				+ "');"
				+ "if (script == null) {"
				+ "script = document.createElement('script');"
				+ "script.id = '" + SCRIPT_ID + "';"
				+ "script.setAttribute('type', 'text/javascript');"
				+ "document.head.appendChild(script);"
				+ "script.textContent = \"" + scriptContent + "\";"
				+ "}";

		return js;
	}

	public static String getJSUrl() {
		String js = getJS_AppendScriptToBodyEnd(mScriptTextContent);

		String jsUrl = "javascript:(function() {"
				+ js
				+ "}) ();";

		return jsUrl;
	}
	}
}

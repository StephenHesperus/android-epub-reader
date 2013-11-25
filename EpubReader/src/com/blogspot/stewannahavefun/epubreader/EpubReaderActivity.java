package com.blogspot.stewannahavefun.epubreader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.webkit.WebView;

public class EpubReaderActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_epub_reader);

		WebView bookView = (WebView) findViewById(R.id.book_content);

		String url = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/doc/hello-small.html";
		bookView.loadUrl(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.epub_reader, menu);
		return true;
	}

}

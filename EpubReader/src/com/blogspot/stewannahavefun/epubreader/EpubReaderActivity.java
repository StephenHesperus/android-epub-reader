package com.blogspot.stewannahavefun.epubreader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.GridView;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;

public class EpubReaderActivity extends Activity {
	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_epub_reader);

		int[] to = {
				R.id.title,
				R.id.author,
				R.id.publisher,
				R.id.added_date,
				R.id.last_reading_date
		};

		mAdapter = new SimpleCursorAdapter(
				this,
				R.layout.book_list_item,
				null,
				EpubReader.BOOK_LIST_PROJECTION,
				to,
				0);

		GridView bookList = (GridView) findViewById(R.id.book_list);
		bookList.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.epub_reader, menu);
		return true;
	}

}

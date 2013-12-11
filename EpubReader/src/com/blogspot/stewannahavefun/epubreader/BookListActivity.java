package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;

public class BookListActivity extends Activity implements
		LoaderCallbacks<Cursor>, FilePickerDialog.OnFilePickListener {

	private static final String DIALOG_OPEN_EPUB_FILE = "DIALOG_OPEN_EPUB_FILE";
	protected static final String SUFFIX_ADDED_DATE = "Added";
	protected static final String SUFFIX_LAST_READING_DATE = "Last Read";
	private SimpleCursorAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_list);

		int[] to = {
				R.id.title,
				R.id.author,
				R.id.publisher,
				R.id.cover,
				R.id.added_date,
				R.id.last_reading_date
		};

		mAdapter = new SimpleCursorAdapter(
				this,
				R.layout.book_list_item,
				null,
				EpubReader.BOOK_LIST_FROM,
				to,
				0);

		SimpleCursorAdapter.ViewBinder binder = new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String column = cursor.getColumnName(columnIndex);

				if (column.equals(Books.ADDED_DATE)
						|| column.equals(Books.LAST_READING_DATE)) {
					DateTextView dateTextView = (DateTextView) view;
					long ms = cursor.getLong(columnIndex);
					String suffix = column.equals(Books.ADDED_DATE)
							? SUFFIX_ADDED_DATE
							: SUFFIX_LAST_READING_DATE;

					dateTextView.setDate(ms, suffix);

					return true;
				} else if (column.equals(Books.COVER)) {
					ImageView cover = (ImageView) view;

					cover.setImageResource(R.drawable.ic_epub_cover);

					return true;
				}

				return false;
			}
		};

		mAdapter.setViewBinder(binder);

		GridView bookList = (GridView) findViewById(R.id.book_list);
		bookList.setAdapter(mAdapter);
		bookList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onBookClick(id);
			}
		});

		getLoaderManager().initLoader(0, null, this);
	}

	private void onBookClick(final long id) {
		Intent reading = new Intent(this, ReadingActivity.class);

		reading.putExtra(Books._ID, id);
		startActivity(reading);
	}

	@Override
	protected void onResume() {
		super.onResume();

		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.booklist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_open:
			FilePickerDialog dialog = new FilePickerDialog();
			dialog.show(getFragmentManager(), DIALOG_OPEN_EPUB_FILE);

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				this,
				Books.BOOKS_URI,
				EpubReader.BOOK_LIST_PROJECTION,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onFilePick(Context context, File file) {
		Intent process = new Intent(BookListActivity.this,
				ProcessEpubFileService.class);

		process.setData(Uri.fromFile(file));
		context.startService(process);
	}

}

package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

public class ReadingActivity extends Activity implements
		LoaderCallbacks<Cursor> {
	private DrawerLayout mDrawerLayout;
	private ListView mNavigationList;
	private ActionBarDrawerToggle mDrawerToggle;
	private WebView mBookView;

	private SimpleCursorAdapter mAdapter;

	private String mNavigationDrawerTitle;
	private String mActivityTitle;

	private String EPUB_LOCATION;

	private static final String SCHEME = "file://";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reading);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mActivityTitle = mNavigationDrawerTitle = getTitle().toString();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.reading_drawer_layout);
		mNavigationList = (ListView) findViewById(R.id.navigation_list);
		mBookView = (WebView) findViewById(R.id.book_view);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
				) {

					@Override
					public void onDrawerClosed(View drawerView) {
						getActionBar().setTitle(mActivityTitle);
						invalidateOptionsMenu();
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						getActionBar().setTitle(mNavigationDrawerTitle);
						invalidateOptionsMenu();
					}

				};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		String[] from = { Contents.NAVIGATION_LABEL, Contents.NAVIGATION_DEPTH };
		int[] to = { R.id.navigation_item };

		mAdapter = new NavigationAdapter(this, from, to);
		mNavigationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				onNavigationLabelClick(id);
			}
		});

		Intent start = getIntent();

		if (start.hasExtra(Books.BOOK_ID)) {
			prepareReadingSession(start.getStringExtra(Books.BOOK_ID));
		}
	}

	private void prepareReadingSession(String bookId) {
		Bundle args = new Bundle();

		args.putString(Books.BOOK_ID, bookId);
		getLoaderManager().initLoader(0, args, this);
	}

	private void onNavigationLabelClick(long id) {
		Uri navigation = ContentUris.withAppendedId(
				Contents.CONTENTS_ID_URI_BASE, id);
		Cursor c = getContentResolver().query(
				navigation,
				EpubReader.CONTENTS_ITEM_PROJECTION,
				null, null, null);
		String link = c.getString(1);

		mBookView.loadUrl(constructUrl(link));
	}

	private String constructUrl(String link) {
		return SCHEME + EPUB_LOCATION + File.separator + link;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reading, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mDrawerToggle.syncState();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String bookId = args.getString(Books.BOOK_ID);
		String selection = Books.BOOK_ID + " = " + "'" + bookId + "'";

		return new CursorLoader(
				this,
				Contents.CONTENTS_URI,
				EpubReader.CONTENTS_PROJECTION,
				selection,
				null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private class NavigationAdapter extends SimpleCursorAdapter {

		public NavigationAdapter(Context context, String[] from, int[] to) {
			super(context, R.layout.navigation_list_item, null, from, to, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String label = cursor.getString(cursor
					.getColumnIndex(Contents.NAVIGATION_LABEL));
			int depth = cursor.getInt(cursor
					.getColumnIndex(Contents.NAVIGATION_DEPTH));
			TextView navItemView = (TextView) view;
			int initialPaddingLeft = navItemView.getPaddingLeft();
			int newPaddingLeft = initialPaddingLeft * depth;
			int top = navItemView.getPaddingTop();
			int right = navItemView.getPaddingRight();
			int bottom = navItemView.getPaddingBottom();

			navItemView.setText(label);
			navItemView.setPadding(newPaddingLeft, top, right, bottom);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			TextView textView = (TextView) inflater.inflate(
					R.layout.navigation_list_item,
					null);

			return textView;
		}

	}
}

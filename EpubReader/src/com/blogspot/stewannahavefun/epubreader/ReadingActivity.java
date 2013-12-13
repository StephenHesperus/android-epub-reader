package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.widget.AbsListView;
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

	private String mLocationBase;
	private String mBookId;
	private int mLastOrder;
	private long m_Id;

	private static final String SCHEME = "file://";
	private static final String THEME_EDITOR_DIALOG = "THEME_EDITOR_DIALOG";

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
		mNavigationList.setAdapter(mAdapter);
		mNavigationList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mNavigationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				onNavigationLabelClick(id);
			}
		});

		Intent start = getIntent();

		if (start.hasExtra(Books._ID)) {
			prepareReadingSession(start.getLongExtra(Books._ID, 1));
		}
	}

	private void prepareReadingSession(long id) {
		m_Id = id;
		Uri current = ContentUris.withAppendedId(Books.BOOK_ID_URI_BASE, id);
		Cursor c = getContentResolver().query(
				current,
				EpubReader.READ_BOOK_PROJECTION,
				null, null, null);

		if (c != null && c.moveToFirst()) {
			mLocationBase = c.getString(c.getColumnIndex(Books.LOCATION));
			mBookId = c.getString(c.getColumnIndex(Books.BOOK_ID));
			mLastOrder = c.getInt(c
					.getColumnIndex(Books.LAST_READING_POINT_NAVIGATION_ORDER));

			mNavigationDrawerTitle = c.getString(c.getColumnIndex(Books.TITLE));
			setTitle(mActivityTitle);
		}

		getLoaderManager().initLoader(0, null, this);
	}

	private void onNavigationLabelClick(long id) {
		Uri navigation = ContentUris.withAppendedId(
				Contents.CONTENTS_ID_URI_BASE, id);
		Cursor c = getContentResolver().query(
				navigation,
				EpubReader.CONTENTS_ITEM_PROJECTION,
				null, null, null);

		if (c != null && c.moveToFirst()) {
			String link = c.getString(c
					.getColumnIndex(Contents.NAVIGATION_LINK));
			String label = c.getString(c
					.getColumnIndex(Contents.NAVIGATION_LABEL));

			mActivityTitle = label;
			mBookView.loadUrl(constructPageUrl(link));

			mLastOrder = c.getInt(c
					.getColumnIndex(Contents.NAVIGATION_ORDER));
		}
	}

	private String constructPageUrl(String link) {
		return SCHEME + mLocationBase + File.separator + link;
	}

	@Override
	protected void onResume() {
		super.onResume();

		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		ContentValues v = new ContentValues();
		Uri lastRead = ContentUris.withAppendedId(Books.BOOK_ID_URI_BASE, m_Id);
		v.put(Books.LAST_READING_POINT_NAVIGATION_ORDER, mLastOrder);
		getContentResolver().update(lastRead, v, null, null);
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
		case R.id.action_edit_theme:
			ThemeEditorDialog dialog = new ThemeEditorDialog();
			Bundle args = new Bundle();
			args.putString(ARG_CSS, mCSS);

			dialog.setArguments(args);
			dialog.show(getFragmentManager(), THEME_EDITOR_DIALOG);

			return true;

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
		String selection = Books.BOOK_ID + " = " + "'" + mBookId + "'";

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

		mNavigationList.setItemChecked(mLastOrder - 1, true);
		onNavigationLabelClick(mNavigationList
				.getItemIdAtPosition(mLastOrder - 1));
		setTitle(mActivityTitle);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private class NavigationAdapter extends SimpleCursorAdapter {

		private int mDepth;
		private int mLeft;
		private int mPadding;
		private int mPaddingRight;

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

			if (cursor.isFirst()) {
				mDepth = depth;
			}

			int newPaddingLeft = mLeft * (depth - mDepth + 1) * 2;
			navItemView.setText(label);
			navItemView.setPadding(newPaddingLeft,
					mPadding, mPaddingRight, mPadding);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			TextView textView = (TextView) inflater.inflate(
					R.layout.navigation_list_item,
					null);

			mLeft = textView.getPaddingLeft();
			mPadding = textView.getPaddingTop();
			mPaddingRight = textView.getPaddingRight();

			return textView;
		}

	}
}

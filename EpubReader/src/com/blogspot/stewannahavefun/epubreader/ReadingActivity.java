package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;
import com.blogspot.stewannahavefun.epubreader.ThemeEditorDialog.ThemeEditorListener;

public class ReadingActivity extends Activity implements
		LoaderCallbacks<Cursor>, ThemeEditorListener {
	public static class WebInterface {
		private static final String WEB_INTERFACE_NAME = "EpubReaderWebInterface";
		private Context mContext;

		public WebInterface(Context context) {
			mContext = context;
		}

		@JavascriptInterface
		public void onImageClick(String src) {
			Uri data = Uri.parse(src);
			Intent view = new Intent(Intent.ACTION_VIEW);

			view.setDataAndType(data, "image/*");

			mContext.startActivity(view);
		}

		public static String getInterfaceName() {
			return WEB_INTERFACE_NAME;
		}

		@JavascriptInterface
		public void onAnchorClick(String href) {
			Toast.makeText(mContext, href, Toast.LENGTH_SHORT).show();
		}
	}

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
	private String mCSS;
	private RelativeLayout mNavigationDrawer;
	private SharedPreferences mPref;
	private String mLastLink;
	private int mLastPosition;
	private final Handler mHandler = new Handler();
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	protected ImageButton mBackwardButton;
	private Stack<String> mHistoryStack;
	private Runnable mScrollRunnable;

	private static final String SCHEME = "file://";
	private static final String THEME_EDITOR_DIALOG = "THEME_EDITOR_DIALOG";
	private static final String ARG_CSS = "ARG_CSS";
	private static final String KEY_CSS = "KEY_CSS";
	private static final String DEFAULT_ENCODING = "utf-8";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reading);

		mHistoryStack = new Stack<String>();

		mScrollRunnable = new Runnable() {

			@Override
			public void run() {
				if (mBookView.getContentHeight() > 0) {
					mBookView.scrollTo(0, mLastPosition);

					mHandler.removeCallbacks(this);
				} else {
					mHandler.postDelayed(this, 100);
				}
			}
		};

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mActivityTitle = mNavigationDrawerTitle = getTitle().toString();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.reading_drawer_layout);
		mNavigationList = (ListView) findViewById(R.id.navigation_list);

		ViewStub viewStub = (ViewStub) findViewById(R.id.view_stub);

		viewStub.setOnInflateListener(new OnInflateListener() {

			@Override
			public void onInflate(ViewStub stub, View inflated) {

				mPreviousButton = (ImageButton) findViewById(R.id.previous);
				mNextButton = (ImageButton) findViewById(R.id.next);

				mPreviousButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int position = (mLastOrder != 1)
								? (mLastOrder - 2)
								: 0;
						long id = mNavigationList.getItemIdAtPosition(position);

						onNavigationLabelClick(id);
						mNavigationList.setItemChecked(position, true);
						mNavigationList.setSelection(position);
					}
				});
				mNextButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int position = (mLastOrder < mNavigationList.getCount())
								? mLastOrder
								: (mNavigationList.getCount() - 1);
						long id = mNavigationList.getItemIdAtPosition(position);

						onNavigationLabelClick(id);
						mNavigationList.setItemChecked(position, true);
						mNavigationList.setSelection(position);
					}
				});

				mBackwardButton = (ImageButton) findViewById(R.id.history_backward);

				mBackwardButton.setVisibility(View.INVISIBLE);
				mBackwardButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String url = mHistoryStack.pop();

						if (url != null) {
							preparePageJumping(url);
							mBookView.loadUrl(url);
							mHandler.postDelayed(mScrollRunnable, 100);
						}
					}
				});
			}
		});

		viewStub.inflate();

		mBookView = (WebView) findViewById(R.id.book_view);
		mNavigationDrawer = (RelativeLayout) findViewById(R.id.navigation_drawer);

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

		// WebView setup
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mCSS = mPref.getString(KEY_CSS, ReadingTheme.DEFAULT_CSS);

		WebSettings webSettings = mBookView.getSettings();

		enableJavaScript(webSettings);
		webSettings.setDefaultTextEncodingName(DEFAULT_ENCODING);

		WebViewClient webViewClient = new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				applyTheme();

				mLastLink = url;

				mBackwardButton.setVisibility(mHistoryStack.empty()
						? View.INVISIBLE
						: View.VISIBLE);

				registerListeners();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				preparePageJumping(url);
				mLastPosition = mBookView.getScrollY();

				String first = mBookView.getOriginalUrl();

				mHistoryStack.push(first);

				return super.shouldOverrideUrlLoading(view, url);
			}

		};

		mBookView.setWebViewClient(webViewClient);

		mBookView.addJavascriptInterface(new WebInterface(this),
				WebInterface.getInterfaceName());

		Intent start = getIntent();

		if (start.hasExtra(Books._ID)) {
			prepareReadingSession(start.getLongExtra(Books._ID, 1));
		}
	}

	protected void registerListeners() {
		ReadingControl.addImageListener();
		ReadingControl.addAnchorListener();

		mBookView.loadUrl(ReadingControl.getJSUrl());
	}

	protected void preparePageJumping(String url) {
		String path = url.substring(SCHEME.length()
				+ mLocationBase.length() + 1);
		String book = Contents.BOOK_ID + " = \"" + mBookId + "\"";
		String link = Contents.NAVIGATION_LINK + " = \"" + path
				+ "\"";
		String selection = "(" + book + ") AND (" + link + ")";
		Cursor c = getContentResolver().query(
				Contents.CONTENTS_URI,
				EpubReader.CONTENTS_ITEM_PROJECTION,
				selection,
				null,
				null);

		if (c != null && c.moveToFirst()) {
			mLastOrder = c.getInt(c
					.getColumnIndex(Contents.NAVIGATION_ORDER));

			mNavigationList.setItemChecked(mLastOrder - 1, true);
			mNavigationList.setSelection(mLastOrder - 1);

			c.close();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void enableJavaScript(WebSettings webSettings) {
		webSettings.setJavaScriptEnabled(true);
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
			mLastLink = c.getString(c
					.getColumnIndex(Books.LAST_READING_POINT_NAVIGATION_LINK));
			mLastPosition = c.getInt(c
					.getColumnIndex(Books.LAST_READING_POINT_PAGE_NUMBER));

			mNavigationDrawerTitle = c.getString(c.getColumnIndex(Books.TITLE));
			setTitle(mNavigationDrawerTitle);

			mBookView.loadUrl(mLastLink);

			mHandler.postDelayed(mScrollRunnable, 100);

			c.close();
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
			setTitle(mActivityTitle);
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
		mLastPosition = mBookView.getScrollY();

		v.put(Books.LAST_READING_POINT_NAVIGATION_ORDER, mLastOrder);
		v.put(Books.LAST_READING_POINT_NAVIGATION_LINK, mLastLink);
		v.put(Books.LAST_READING_DATE, System.currentTimeMillis());
		v.put(Books.LAST_READING_POINT_PAGE_NUMBER, mLastPosition);

		getContentResolver().update(lastRead, v, null, null);

		SharedPreferences.Editor editor = mPref.edit();

		editor.putString(KEY_CSS, mCSS);
		editor.apply();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mNavigationDrawer);

		for (int i = 0; i < menu.size(); i++)
			menu.getItem(i).setVisible(!drawerOpen);

		return super.onPrepareOptionsMenu(menu);
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

		case R.id.action_about:
			Intent about = new Intent(this, AboutActivity.class);

			startActivity(about);

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
		mNavigationList.setSelection(mLastOrder - 1);

		if (mLastLink.isEmpty()) {
			onNavigationLabelClick(mNavigationList
					.getItemIdAtPosition(mLastOrder - 1));
		}
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

	@Override
	public void onThemeChange(String rawCSS) {
		mCSS = rawCSS;

		applyTheme();
	}

	private void applyTheme() {
		String css = mCSS.replaceAll("\n", "");
		String js = ReadingTheme.constructThemeUrl(css);

		mBookView.loadUrl(js);
	}

}

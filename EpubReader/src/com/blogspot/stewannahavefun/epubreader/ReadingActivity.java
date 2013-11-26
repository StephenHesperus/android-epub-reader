package com.blogspot.stewannahavefun.epubreader;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;

public class ReadingActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mNavigationList;
	private ActionBarDrawerToggle mDrawerToggle;
	private WebView mBookView;

	private String mNavigationDrawerTitle;
	private String mActivityTitle;

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
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
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

}

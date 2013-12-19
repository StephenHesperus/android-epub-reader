package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Toast;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;

public class BookListActivity extends Activity implements
		LoaderCallbacks<Cursor>, FilePickerDialog.OnFilePickListener {

	private class ProcessorReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_DUPLICATION.equals(intent.getAction())) {
				String filename = intent
						.getStringExtra(ACTION_DUPLICATION_EXTRA);
				String msg = filename + " already exists!";

				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			} else if (ACTION_UNSUPPORTED_FILE.equals(intent.getAction())) {
				String filename = intent
						.getStringExtra(ACTION_UNSUPPORTED_FILE_EXTRA);
				String msg = filename + " is not a valid epub file!";

				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			} else if (ACTION_DELETION_SUCCESS.equals(intent.getAction())) {
				String filename = intent
						.getStringExtra(ACTION_DELETION_SUCCESS_EXTRA);
				String msg = filename
						+ " is deleted from both book list and storage!";

				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			} else if (ACTION_RESCAN_ONE_BOOK_SUCCESS
					.equals(intent.getAction())) {
				String filename = intent
						.getStringExtra(ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA);
				String msg = filename
						+ " is added during rescanning!";

				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			} else if (ACTION_ADD_BOOK_SUCCESS.equals(intent.getAction())) {
				String filename = intent
						.getStringExtra(ACTION_ADD_BOOK_SUCCESS_EXTRA);
				String msg = filename
						+ " is newly added successfully!";

				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			} else if (ACTION_RESCAN_RESULT.equals(intent.getAction())) {
				int booksFound = intent
						.getIntExtra(ACTION_RESCAN_RESULT_EXTRA, 0);
				String msg = "Rescan finishes, "
						+ booksFound
						+ ((booksFound > 1) ? " books " : " book ")
						+ " found!";

				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}

	}

	private static final String DIALOG_OPEN_EPUB_FILE = "DIALOG_OPEN_EPUB_FILE";
	protected static final String SUFFIX_ADDED_DATE = "Added";
	protected static final String SUFFIX_LAST_READING_DATE = "Last Read";
	private static final String ACTION_DUPLICATION = "com.blogspot.stewannahavefun.epubreader.ACTION_DUPLICATION";
	private static final String ACTION_DUPLICATION_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DUPLICATION_EXTRA";
	private static final String ACTION_UNSUPPORTED_FILE = "com.blogspot.stewannahavefun.epubreader.ACTION_UNSUPPORTED_FILE";
	private static final String ACTION_UNSUPPORTED_FILE_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_UNSUPPORTED_FILE_EXTRA";
	private static final String ACTION_RESCAN = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN";
	private static final String ACTION_ADD_EPUB = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_EPUB";
	private static final String MIMETYPE = "application/epub+zip";
	protected static String ACTION_DELETE_EPUB = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETE_EPUB";
	protected static final String ACTION_DELETE_EPUB_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETE_EPUB_EXTRA";
	private static final String ACTION_DELETION_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETION_SUCCESS";
	private static final String ACTION_DELETION_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETION_SUCCESS_EXTRA";
	private static final String ACTION_RESCAN_ONE_BOOK_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_ONE_BOOK_SUCCESS";
	private static final String ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA";
	private static final String ACTION_ADD_BOOK_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_BOOK_SUCCESS";
	private static final String ACTION_ADD_BOOK_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_BOOK_SUCCESS_EXTRA";
	private static final String ACTION_RESCAN_RESULT = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_RESULT";
	private static final String ACTION_RESCAN_RESULT_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_RESULT_EXTRA";
	private SimpleCursorAdapter mAdapter;
	private ProcessorReceiver mReceiver;
	private GridView mBookList;

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

		mBookList = (GridView) findViewById(R.id.book_list);
		mBookList.setAdapter(mAdapter);
		mBookList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onBookClick(id);
			}
		});

		mBookList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		mBookList.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			private int mCheckedItems;

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();

				inflater.inflate(R.menu.actionmode_booklist, menu);

				mCheckedItems = 0;

				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_delete:
					showDeletionConfirmDialog(mode);

					return true;

				default:
					return false;
				}
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position,
					long id, boolean checked) {
				if (checked) {
					mCheckedItems++;
				} else {
					mCheckedItems--;
				}

				String title = mCheckedItems
						+ (mCheckedItems > 1 ? " Books" : "Book")
						+ " Selected";
				mode.setTitle(title);
			}
		});

		getLoaderManager().initLoader(0, null, this);
	}

	protected void showDeletionConfirmDialog(final ActionMode mode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.deletion_confirm_dialog_title)
				.setMessage(R.string.deletion_confirm_dialog_message)
				.setCancelable(true)
				.setNegativeButton(android.R.string.cancel,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								mode.finish();
							}
						})
				.setPositiveButton(R.string.action_delete,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent delete = new Intent(ACTION_DELETE_EPUB);
								long[] ids = mBookList.getCheckedItemIds();

								delete.putExtra(ACTION_DELETE_EPUB_EXTRA, ids);

								startService(delete);

								mode.finish();
							}
						});

		builder.create()
				.show();
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

		IntentFilter filter = new IntentFilter();
		mReceiver = new ProcessorReceiver();

		filter.addAction(ACTION_DUPLICATION);
		filter.addAction(ACTION_UNSUPPORTED_FILE);
		filter.addAction(ACTION_DELETION_SUCCESS);
		filter.addAction(ACTION_RESCAN_ONE_BOOK_SUCCESS);
		filter.addAction(ACTION_ADD_BOOK_SUCCESS);
		filter.addAction(ACTION_RESCAN_RESULT);
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mReceiver);
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

		case R.id.action_rescan:
			showRescanWarningDialog();

			return true;

		case R.id.action_about:
			Intent about = new Intent(this, AboutActivity.class);

			startActivity(about);

			return true;

		case R.id.action_recover:
			showRecoveryDialog();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showRescanWarningDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.rescan_warning_dialog_title)
				.setMessage(R.string.rescan_warning_dialog_message)
				.setCancelable(true)
				.setNegativeButton(android.R.string.cancel,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(R.string.rescan,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent rescan = new Intent(ACTION_RESCAN);

								startService(rescan);
							}
						});

		builder.create()
				.show();
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
		Intent process = new Intent(ACTION_ADD_EPUB);

		process.setDataAndType(Uri.fromFile(file), MIMETYPE);
		context.startService(process);
	}
}

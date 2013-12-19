package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.io.FilenameFilter;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.blogspot.stewannahavefun.epubreader.EpubFileProcessor.FileIsNotConstructedException;
import com.blogspot.stewannahavefun.epubreader.EpubFileProcessor.UnsupportedFileException;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

public class EpubReaderService extends IntentService {
	public class Processor extends EpubFileProcessor {

		public Processor(File epub, File output) {
			super(epub, output);
		}

		@Override
		public void getContentValues(ContentValues v) {
			v.put(Contents.BOOK_ID, mBookId);

			getContentResolver().insert(Contents.CONTENTS_URI, v);
		}

	}

	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";
	private static final String EXTRA_EPUB_PATH = "EXTRA_EPUB_PATH";
	private static final String EXTRA_OUTPUT_DIRECTORY = "EXTRA_OUTPUT_DIRECTORY";
	private static final String NCX_PATH = "NCX_PATH";
	private static final String ACTION_DUPLICATION = "com.blogspot.stewannahavefun.epubreader.ACTION_DUPLICATION";
	private static final String ACTION_DUPLICATION_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DUPLICATION_EXTRA";
	private static final String ACTION_UNSUPPORTED_FILE = "com.blogspot.stewannahavefun.epubreader.ACTION_UNSUPPORTED_FILE";
	private static final String ACTION_UNSUPPORTED_FILE_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_UNSUPPORTED_FILE_EXTRA";
	private static final String ACTION_RESCAN = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN";
	private static final String ACTION_ADD_EPUB = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_EPUB";
	private static final String ACTION_DELETE_EPUB = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETE_EPUB";
	private static final String ACTION_DELETE_EPUB_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETE_EPUB_EXTRA";
	private static final String BASE = Environment
			.getExternalStorageDirectory()
			.getAbsolutePath() + "/epubreader-test/data/";
	private static final String ACTION_DELETION_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETION_SUCCESS";
	private static final String ACTION_DELETION_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_DELETION_SUCCESS_EXTRA";
	private static final String ACTION_RESCAN_ONE_BOOK_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_ONE_BOOK_SUCCESS";
	private static final String ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA";
	private static final String ACTION_ADD_BOOK_SUCCESS = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_BOOK_SUCCESS";
	private static final String ACTION_ADD_BOOK_SUCCESS_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_BOOK_SUCCESS_EXTRA";
	private static final String ACTION_RESCAN_RESULT = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_RESULT";
	private static final String ACTION_RESCAN_RESULT_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_RESCAN_RESULT_EXTRA";
	private static final String ACTION_ADD_EPUB_EXTRA = "com.blogspot.stewannahavefun.epubreader.ACTION_ADD_EPUB_EXTRA";
	private static final String TEMPORARY_TITLE_TO_SHOW_PROGRESS_BAR = "~com.blogspot.stewannahavefun.epubreader.TEMPORARY_TITLE_TO_SHOW_PROGRESS_BAR";
	private String mBookId;

	public EpubReaderService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ACTION_RESCAN.equals(intent.getAction())) {
			rescanExistingBooks(intent);
		} else if (ACTION_ADD_EPUB.equals(intent.getAction())) {
			addEpubFile(intent);
		} else if (ACTION_DELETE_EPUB.equals(intent.getAction())) {
			deleteEpubs(intent);
		}
	}

	private void deleteEpubs(Intent intent) {
		long[] ids = intent.getLongArrayExtra(ACTION_DELETE_EPUB_EXTRA);

		for (long id : ids) {
			Uri delete = ContentUris.withAppendedId(Books.BOOK_ID_URI_BASE, id);

			Cursor book = getContentResolver().query(delete, null, null, null,
					null);

			if (book != null && book.moveToFirst()) {
				String bookId = book.getString(book
						.getColumnIndex(Books.BOOK_ID));

				String selection = Contents.BOOK_ID + " = '" + bookId + "'";

				getContentResolver().delete(
						Contents.CONTENTS_URI,
						selection,
						null);

				String location = book.getString(book
						.getColumnIndex(Books.LOCATION));
				String epubName = location.substring(BASE.length()).split("/")[0];
				File epubDir = new File(BASE, epubName);

				recursiveDeleteDirectory(epubDir, new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						if (filename.equals(dir.getName())) {
							return false;
						}

						return true;
					}
				});

				getContentResolver().delete(delete, null, null);

				book.close();

				Intent deletionOk = new Intent(ACTION_DELETION_SUCCESS);

				deletionOk.putExtra(ACTION_DELETION_SUCCESS_EXTRA, epubName);
				sendBroadcast(deletionOk);
			}
		}
	}

	private void recursiveDeleteDirectory(File directory, FilenameFilter filter) {
		File[] files = directory.listFiles(filter);

		for (File file : files) {
			if (file.isDirectory()) {
				recursiveDeleteDirectory(file, filter);
			} else if (file.isFile()) {
				file.delete();
			}
		}

		directory.delete();
	}

	private void addEpubFile(Intent intent) {
		if (intent.hasExtra(EXTRA_EPUB_PATH)
				|| intent.hasExtra(EXTRA_OUTPUT_DIRECTORY)) {
			try {
				throw new Exception(
						"Legacy parameter, it does nothing now. Use setData Uri from File, that will be all.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Uri data = intent.getData();
		File epub = new File(data.getPath());
		File base = new File(BASE);
		File output = new File(base, epub.getName());

		boolean fromBackup = intent.getBooleanExtra(ACTION_ADD_EPUB_EXTRA,
				false);

		if (!fromBackup && output.isDirectory()) {
			Intent duplication = new Intent(ACTION_DUPLICATION);

			duplication.putExtra(ACTION_DUPLICATION_EXTRA, epub.getName());
			sendBroadcast(duplication);
			return;
		}

		output.mkdir();

		Processor processor = new Processor(epub, output);

		Uri newRow = insertTemporaryRowIntoBookTable();

		try {
			boolean success = processor.unZipEpubFile();

			if (success) {
				processor.backupEpubFile();
				processor.readContainerDotXmlFile();

				ContentValues bookInfo = processor.readOpfFile();
				bookInfo.remove(NCX_PATH);

				mBookId = bookInfo.getAsString(Books.BOOK_ID);

				processor.readNcxFile();

				getContentResolver().update(newRow, bookInfo, null, null);

				Intent add = new Intent(ACTION_ADD_BOOK_SUCCESS);

				add.putExtra(ACTION_ADD_BOOK_SUCCESS_EXTRA, epub.getName());
			}
		} catch (UnsupportedFileException e) {
			getContentResolver().delete(newRow, null, null);

			Intent unsupported = new Intent(ACTION_UNSUPPORTED_FILE);

			unsupported.putExtra(ACTION_UNSUPPORTED_FILE_EXTRA, epub.getName());
			sendBroadcast(unsupported);
		} catch (FileIsNotConstructedException e) {
			e.printStackTrace();
		}
	}

	private Uri insertTemporaryRowIntoBookTable() {
		ContentValues tmp = new ContentValues();

		tmp.put(Books.BOOK_ID, Books.BOOK_ID);
		tmp.put(Books.TITLE, TEMPORARY_TITLE_TO_SHOW_PROGRESS_BAR);
		tmp.put(Books.LOCATION, Books.LOCATION);

		Uri newRow = getContentResolver().insert(Books.BOOKS_URI, tmp);

		return newRow;
	}

	private void rescanExistingBooks(Intent intent) {
		getContentResolver().delete(Books.BOOKS_URI, null, null);
		getContentResolver().delete(Contents.CONTENTS_URI, null, null);

		File base = new File(BASE);
		File[] epubList = base.listFiles();
		int booksFound = 0;

		for (final File epub : epubList) {
			File[] files = epub.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if (filename.equals(epub.getName())) {
						return false;
					}

					return true;
				}
			});

			if (files.length == 0) {
				continue;
			}

			Processor processor = new Processor(null, epub);

			Uri newRow = insertTemporaryRowIntoBookTable();

			processor.readContainerDotXmlFile();
			try {
				ContentValues bookInfo = processor.readOpfFile();

				bookInfo.remove(NCX_PATH);

				mBookId = bookInfo.getAsString(Books.BOOK_ID);

				processor.readNcxFile();

				// update book table at last so that contents table is ready to
				// use when the book shows up in book list
				getContentResolver().update(newRow, bookInfo, null, null);

				Intent oneBookOk = new Intent(ACTION_RESCAN_ONE_BOOK_SUCCESS);

				oneBookOk.putExtra(ACTION_RESCAN_ONE_BOOK_SUCCESS_EXTRA,
						epub.getName());
				sendBroadcast(oneBookOk);
			} catch (FileIsNotConstructedException e) {
				e.printStackTrace();
			}

			booksFound++;
		}

		Intent rescanResult = new Intent(ACTION_RESCAN_RESULT);

		rescanResult.putExtra(ACTION_RESCAN_RESULT_EXTRA, booksFound);
		sendBroadcast(rescanResult);
	}
}

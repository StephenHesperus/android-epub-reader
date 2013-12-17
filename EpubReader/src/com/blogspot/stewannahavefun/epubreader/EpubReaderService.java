package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
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

		if (output.isDirectory()) {
			Intent duplication = new Intent(ACTION_DUPLICATION);

			duplication.putExtra(ACTION_DUPLICATION_EXTRA, epub.getName());
			sendBroadcast(duplication);
			return;
		}

		output.mkdir();

		Processor processor = new Processor(epub, output);

		try {
			boolean success = processor.unZipEpubFile();

			if (success) {
				processor.backupEpubFile();
				processor.readContainerDotXmlFile();

				ContentValues bookInfo = processor.readOpfFile();
				bookInfo.remove(NCX_PATH);

				mBookId = bookInfo.getAsString(Books.BOOK_ID);

				processor.readNcxFile();

				getContentResolver().insert(Books.BOOKS_URI, bookInfo);
			}
		} catch (UnsupportedFileException e) {
			Intent unsupported = new Intent(ACTION_UNSUPPORTED_FILE);

			unsupported.putExtra(ACTION_UNSUPPORTED_FILE_EXTRA, epub.getName());
			sendBroadcast(unsupported);
		} catch (FileIsNotConstructedException e) {
			e.printStackTrace();
		}
	}

	private void rescanExistingBooks(Intent intent) {
		getContentResolver().delete(Books.BOOKS_URI, null, null);
		getContentResolver().delete(Contents.CONTENTS_URI, null, null);

		File base = new File(BASE);
		File[] epubList = base.listFiles();

		for (File epub : epubList) {
			Processor processor = new Processor(null, epub);

			processor.readContainerDotXmlFile();
			try {
				ContentValues bookInfo = processor.readOpfFile();

				bookInfo.remove(NCX_PATH);

				mBookId = bookInfo.getAsString(Books.BOOK_ID);

				processor.readNcxFile();

				// update book table at last so that contents table is ready to
				// use when the book shows up in book list
				getContentResolver().insert(Books.BOOKS_URI, bookInfo);
			} catch (FileIsNotConstructedException e) {
				e.printStackTrace();
			}
		}
	}
}

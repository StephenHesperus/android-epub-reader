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

public class ProcessEpubFileService extends IntentService {
	public class Processor extends EpubFileProcessor {

		public Processor(File epub, File output) {
			super(epub, output);
		}

		@Override
		public void getContentValues(ContentValues v) {
			getContentResolver().insert(Contents.CONTENTS_URI, v);
		}

	}

	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";
	private static final String EXTRA_EPUB_PATH = "EXTRA_EPUB_PATH";
	private static final String EXTRA_OUTPUT_DIRECTORY = "EXTRA_OUTPUT_DIRECTORY";
	private static final String NCX_PATH = "NCX_PATH";
	private final String mBase = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/epubreader-test/data/";

	public ProcessEpubFileService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
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
		File base = new File(mBase);
		File output = new File(base, epub.getName());

		output.mkdir();

		Processor processor = new Processor(epub, output);

		try {
			boolean success = processor.unZipEpubFile();

			if (success) {
				processor.backupEpubFile();
				processor.readContainerDotXmlFile();

				ContentValues bookInfo = processor.readOpfFile();
				bookInfo.remove(NCX_PATH);
				getContentResolver().insert(Books.BOOKS_URI, bookInfo);

				processor.readNcxFile();
			}
		} catch (UnsupportedFileException e) {
			e.printStackTrace();
		} catch (FileIsNotConstructedException e) {
			e.printStackTrace();
		}
	}
}

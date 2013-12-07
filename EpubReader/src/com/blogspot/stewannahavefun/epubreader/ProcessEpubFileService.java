package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import com.blogspot.stewannahavefun.epubreader.EpubFileProcessor.FileIsNotConstructedException;
import com.blogspot.stewannahavefun.epubreader.EpubFileProcessor.UnsupportedFileException;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

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

	public ProcessEpubFileService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String epubPath = intent.getStringExtra(EXTRA_EPUB_PATH);
		String outputPath = intent.getStringExtra(EXTRA_OUTPUT_DIRECTORY);
		File epub = new File(epubPath);
		File output = new File(outputPath);
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

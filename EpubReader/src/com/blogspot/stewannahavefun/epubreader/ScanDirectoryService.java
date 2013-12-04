package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.io.FilenameFilter;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

public class ScanDirectoryService extends IntentService {
	private static final String TAG = "SCAN_DIRECTORY_SERVICE";

	public ScanDirectoryService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri data = intent.getData();

		String dirPath = data.getPath();
		File dir = new File(dirPath);
		File[] epubFileList = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".epub");
			}
		});

		for (File epub : epubFileList) {
			Intent process = new Intent(this, ProcessEpubFileService.class);
			Uri epubUri = Uri.fromFile(epub);
			
			process.setData(epubUri);
			startService(process);
		}
	}

}
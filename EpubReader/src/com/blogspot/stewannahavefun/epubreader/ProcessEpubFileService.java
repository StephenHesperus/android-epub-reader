package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

public class ProcessEpubFileService extends IntentService {
	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";

	public ProcessEpubFileService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri data = intent.getData();
		File epub = new File(data.getPath());
	}

}

package com.blogspot.stewannahavefun.epubreader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ProcessEpubFileService extends IntentService {
	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";

	public ProcessEpubFileService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Uri data = intent.getData();
		File epub = new File(data.getPath());
		String root = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/epubreader/data/";
		String epubDir = root + epub.getName();
		File output = new File(epubDir);

		unzipEpubFile(epub, output);
	}

	private void unzipEpubFile(File epub, File outputDir) {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			return ;
		
		try {
			FileInputStream is = new FileInputStream(epub);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(is));
			ZipEntry ze;

			while ((ze = zis.getNextEntry()) != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;

				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
				}

				String filename = ze.getName();
				byte[] bytes = baos.toByteArray();
				File file = new File(outputDir + File.separator + filename);
				
				(new File(file.getParent())).mkdirs();
				
				FileOutputStream fos = new FileOutputStream(file);
				
				fos.write(bytes);
				fos.close();
				baos.close();
			}

			zis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

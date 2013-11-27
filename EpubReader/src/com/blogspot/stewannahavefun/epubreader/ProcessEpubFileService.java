package com.blogspot.stewannahavefun.epubreader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ProcessEpubFileService extends IntentService {
	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";
	private static final String MIMETYPE_FILE = "mimetype";
	private static final String MIMETYPE = "application/epub+zip";

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

		boolean success = unzipEpubFile(epub, output);
	}
	
	private boolean checkExternalStorageWritable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
			return true;
		else
			return false;
	}

	private boolean unzipEpubFile(File epub, File outputDir) {
		boolean unzipSuccess = false;
		
		if (!checkExternalStorageWritable())
			return unzipSuccess;

		try {
			FileInputStream is = new FileInputStream(epub);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(is));
			ZipEntry ze;
			boolean mimetypeCorrect = false;

			ze = zis.getNextEntry();

			do {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;

				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
				}

				String filename = ze.getName();
				byte[] bytes = baos.toByteArray();

				if (!mimetypeCorrect) {
					if (filename.equals(MIMETYPE_FILE)
							&& baos.toString().equals(MIMETYPE)) {
						mimetypeCorrect = true;
					} else {
						throw new UnsupportedFileTypeException();
					}
				}

				File file = new File(outputDir + File.separator + filename);

				(new File(file.getParent())).mkdirs();

				FileOutputStream fos = new FileOutputStream(file);

				fos.write(bytes);
				fos.close();
				baos.close();
			} while ((ze = zis.getNextEntry()) != null && mimetypeCorrect);

			zis.close();
			unzipSuccess = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			// TODO: inform UI the file is not supported
			unzipSuccess = false;
		}

		return unzipSuccess;
	}

	private class UnsupportedFileTypeException extends Exception {

	}
}

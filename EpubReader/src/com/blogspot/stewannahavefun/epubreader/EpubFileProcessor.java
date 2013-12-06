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

public class EpubFileProcessor {

	private static final String MIMETYPE = "application/epub+zip";
	private static final Object MIMETYPE_FILE = "mimetype";
	private File mEpub;
	private File mOutput;

	public EpubFileProcessor(File epub, File output) {
		mEpub = epub;
		mOutput = output;
	}

	public boolean unZipEpubFile() throws UnsupportedFileException {
		boolean unzipSuccess = false;

		try {
			FileInputStream is = new FileInputStream(mEpub);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(is));
			ZipEntry ze;
			boolean mimetypeCorrect = false;

			ze = zis.getNextEntry();

			if (ze == null) {
				zis.close();
				throw new UnsupportedFileException("Invalid file "
						+ mEpub.getName());
			}

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
						throw new UnsupportedFileException("Invalid file "
								+ filename);
					}
				}

				File file = new File(mOutput + File.separator + filename);

				(new File(file.getParent())).mkdirs();

				FileOutputStream fos = new FileOutputStream(file);

				fos.write(bytes);
				fos.close();
				baos.close();
			} while ((ze = zis.getNextEntry()) != null && mimetypeCorrect);

			zis.close();
			unzipSuccess = true;
		} catch (FileNotFoundException e) {
			throw new UnsupportedFileException(e.getMessage());
		} catch (IOException e) {
			throw new UnsupportedFileException(e.getMessage());
		} catch (UnsupportedFileException e) {
			throw new UnsupportedFileException(e.getMessage());
		}

		return unzipSuccess;
	}

	public static class UnsupportedFileException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnsupportedFileException(String msg) {
			super(msg);
		}
	}
}

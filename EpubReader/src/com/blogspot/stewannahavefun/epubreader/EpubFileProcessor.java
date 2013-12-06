package com.blogspot.stewannahavefun.epubreader;

import java.io.File;

public class EpubFileProcessor {

	private File mEpub;
	private File mOutput;

	public EpubFileProcessor(File epub, File output) {
		mEpub = epub;
		mOutput = output;
	}

	public static class UnsupportedFileException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnsupportedFileException(String msg) {
			super(msg);
		}
	}
}

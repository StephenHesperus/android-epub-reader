package com.blogspot.stewannahavefun.epubreader;

import java.io.File;
import java.io.FilenameFilter;

public class DirectoryScanner {

	public DirectoryScanner() {
	}

	public static File[] filterEpubFile(File dir) {
		File[] epubFileList = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".epub");
			}
		});
		
		return epubFileList;
	}

}

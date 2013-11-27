package com.blogspot.stewannahavefun.epubreader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class ProcessEpubFileService extends IntentService {
	private static final String TAG = "PROCESS_EPUB_FILE_SERVICE";
	private static final String MIMETYPE_FILE = "mimetype";
	private static final String MIMETYPE = "application/epub+zip";
	private static final String CONTAINER_DOT_XML = "META-INF/container.xml";
	private static final String ROOT;
	private static final String EPUB_DIRECTORY;

	static {
		ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
		EPUB_DIRECTORY = ROOT + File.separator + "epubreader/data";
	}

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

	private void backupEpubFile(File epub, File output) {
		try {
			FileInputStream is = new FileInputStream(epub);
			FileChannel in = is.getChannel();
			FileOutputStream os = new FileOutputStream(output);
			FileChannel out = os.getChannel();

			in.transferTo(0, in.size(), out);
			out.close();
			in.close();
			is.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readContainerDotXmlFile(String epubDir) {
		final String ROOT_FILE = "rootfile";
		final String FULL_PATH = "full-path";
		File container = new File(epubDir, CONTAINER_DOT_XML);
		String fullPath = "";
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(container);
			Element rootfile = (Element) doc.getElementsByTagName(ROOT_FILE)
					.item(0);
			fullPath = rootfile.getAttribute(FULL_PATH);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fullPath;
	}

	private String readOPFFile(File opf) {
		final String SPINE = "spine";
		final String TOC = "toc";
		final String HREF = "href";
		
		String ncxPath = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(opf);
			
			// metadata
			
			
			// mainfest
			
			// spine, look for NCX file path
			Element spine = (Element) doc.getElementsByTagName(SPINE).item(0);
			String ncxId = spine.getAttribute(TOC);
			Element ncx = doc.getElementById(ncxId);
			ncxPath = ncx.getAttribute(HREF);
			
			// guide
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ncxPath;
	}
}

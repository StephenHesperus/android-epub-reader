package com.blogspot.stewannahavefun.epubreader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

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

	private void readNCXFile(File ncx) {
		final String META = "meta";
		final String NAME = "name";
		final String UID = "dtb:uid";
		final String NAVPOINT = "navPoint";
		final String PLAYORDER = "playOrder";
		final String SRC = "src";
		final String TEXT = "text";
		final String CONTENT = "content";

		ArrayList<String> depthList = new ArrayList<String>();
		try {
			// get depth list
			FileInputStream is = new FileInputStream(ncx);
			XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
			XmlPullParser parser = fac.newPullParser();

			fac.setNamespaceAware(true);
			parser.setInput(is, "utf-8");

			depthList.clear();
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& parser.getName().equals(NAVPOINT)) {
					depthList.add(parser.getDepth() + "");
				}

				eventType = parser.next();
			}
			is.close();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(ncx);

			// get book id
			String BOOK_ID = null;
			NodeList metaList = doc.getElementsByTagName(META);
			if (metaList != null && metaList.getLength() > 0) {
				for (int i = 0; i < metaList.getLength(); i++) {
					Element meta = (Element) metaList.item(i);
					if (meta.getAttribute(NAME).equals(UID)) {
						BOOK_ID = meta.getAttribute(CONTENT);
					}
				}
			}

			// get table of contents
			NodeList navPointList = doc.getElementsByTagName(NAVPOINT);
			if (navPointList != null && navPointList.getLength() > 0) {
				for (int i = 0; i < navPointList.getLength(); i++) {
					Element navPoint = (Element) navPointList.item(i);
					Element textE = (Element) navPoint
							.getElementsByTagName(TEXT).item(0);
					Element contentE = (Element) navPoint.getElementsByTagName(
							CONTENT).item(0);
					String playOrder = navPoint.getAttribute(PLAYORDER);
					String text = textE.getTextContent();
					String src = contentE.getAttribute(SRC);

					ContentValues v = new ContentValues();

					v.put(Contents.NAVIGATION_LABEL, text);
					v.put(Contents.NAVIGATION_LINK, src);
					v.put(Contents.NAVIGATION_DEPTH, depthList.get(i));
					v.put(Contents.NAVIGATION_ORDER, playOrder);
					v.put(Contents.BOOK_ID, BOOK_ID);

					getContentResolver().insert(Contents.CONTENTS_URI, v);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}

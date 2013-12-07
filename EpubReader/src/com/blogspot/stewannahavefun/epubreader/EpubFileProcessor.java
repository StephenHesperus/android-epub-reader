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

import android.content.ContentValues;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

public abstract class EpubFileProcessor {

	public class FileIsNotConstructedException extends Exception {
		public FileIsNotConstructedException(String msg) {
			super(msg);
		}

		private static final long serialVersionUID = 3L;

	}

	private static final String MIMETYPE = "application/epub+zip";
	private static final Object MIMETYPE_FILE = "mimetype";
	private static final String CONTAINER_DOT_XML = "META-INF/container.xml";
	private File mEpub;
	private File mOutput;
	private File mOpf;
	private File mNcx;

	private static final String SPINE = "spine";
	private static final String TOC = "toc";
	private static final String HREF = "href";
	private static final String ID = "identifier";
	private static final String TITLE = "title";
	private static final String AUTHOR = "creator";
	private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	private static final String PUBLISHER = "publisher";
	private static final String COVER = "cover";
	private static final String NCX_PATH = "NCX_PATH";

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
		} catch (Exception e) {
			File[] nonSence = mOutput.listFiles();
			for (File file : nonSence) {
				file.delete();
			}
			mOutput.delete();

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

	public String backupEpubFile() {
		File backupOutput = new File(mOutput, mEpub.getName());
		String path = null;
		try {
			FileInputStream is = new FileInputStream(mEpub);
			FileChannel in = is.getChannel();
			FileOutputStream os = new FileOutputStream(backupOutput);
			FileChannel out = os.getChannel();

			in.transferTo(0, in.size(), out);
			out.close();
			in.close();
			is.close();
			os.close();

			path = backupOutput.getAbsolutePath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			path = null;
		} catch (IOException e) {
			e.printStackTrace();
			path = null;
		}

		return path;
	}

	public String readContainerDotXmlFile() {
		final String ROOT_FILE = "rootfile";
		final String FULL_PATH = "full-path";
		File container = new File(mOutput, CONTAINER_DOT_XML);
		String fullPath = null;
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

		mOpf = new File(mOutput, fullPath);

		return fullPath;
	}

	public ContentValues readOpfFile() throws FileIsNotConstructedException {

		ContentValues bookInfo = new ContentValues();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = null;
			if (mOpf != null) {
				doc = builder.parse(mOpf);
			} else {
				throw new FileIsNotConstructedException(
						"You should get the .opf file path first. Use readContainerDotXmlFile().");
			}

			// metadata
			String title = doc.getElementsByTagNameNS(DC_NS, TITLE).item(0)
					.getTextContent();
			String author = doc.getElementsByTagNameNS(DC_NS, AUTHOR).item(0)
					.getTextContent();
			String publisher = doc.getElementsByTagNameNS(DC_NS, PUBLISHER)
					.item(0).getTextContent();
			String cover = doc.getElementById(COVER).getAttribute(HREF);
			String bookId = doc.getElementsByTagNameNS(DC_NS, ID).item(0)
					.getTextContent();

			// .ncx file
			Element spine = (Element) doc.getElementsByTagName(SPINE).item(0);
			String ncxId = spine.getAttribute(TOC);
			Element ncx = doc.getElementById(ncxId);
			String ncxPath = ncx.getAttribute(HREF);

			// manifest file path base segement
			String location = mOpf.getParentFile().getAbsolutePath();

			// construct bookInfo
			bookInfo.put(Books.TITLE, title);
			bookInfo.put(Books.AUTHOR, author);
			bookInfo.put(Books.PUBLISHER, publisher);
			bookInfo.put(Books.COVER, cover);

			bookInfo.put(Books.ADDED_DATE, System.currentTimeMillis());
			bookInfo.put(Books.LAST_READING_DATE, System.currentTimeMillis());

			bookInfo.put(Books.LAST_READING_POINT_NAVIGATION_LINK, "");
			bookInfo.put(Books.LAST_READING_POINT_NAVIGATION_ORDER, 0);
			bookInfo.put(Books.LAST_READING_POINT_PAGE_NUMBER, 0);

			bookInfo.put(Books.BOOK_ID, bookId);
			bookInfo.put(Books.LOCATION, location);

			bookInfo.put(NCX_PATH, ncxPath);

			mNcx = new File(location, ncxPath);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bookInfo;
	}

	public void readNcxFile() throws FileIsNotConstructedException {
		final String META = "meta";
		final String NAME = "name";
		final String UID = "dtb:uid";
		final String NAVPOINT = "navPoint";
		final String PLAYORDER = "playOrder";
		final String SRC = "src";
		final String TEXT = "text";
		final String CONTENT = "content";

		ArrayList<Integer> depthList = new ArrayList<Integer>();
		try {
			// get depth list
			FileInputStream is;
			if (mNcx != null) {
				is = new FileInputStream(mNcx);
			} else {
				String msg = "The .ncx file is not constructed yet. Call readOpfFile() before this to construct it.";
				throw new FileIsNotConstructedException(msg);
			}

			XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
			XmlPullParser parser = fac.newPullParser();

			fac.setNamespaceAware(true);
			parser.setInput(is, "utf-8");

			depthList.clear();
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& parser.getName().equals(NAVPOINT)) {
					depthList.add(parser.getDepth());
				}

				eventType = parser.next();
			}
			is.close();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(mNcx);

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

					getContentValues(v);
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

	public abstract void getContentValues(ContentValues v);
}

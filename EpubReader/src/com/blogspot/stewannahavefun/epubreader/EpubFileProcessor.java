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

import android.content.ContentValues;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;

public class EpubFileProcessor {

	public class OpfFileIsNullException extends Exception {

		private static final long serialVersionUID = -2L;

		public OpfFileIsNullException(String detailMessage) {
			super(detailMessage);
		}

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

	public ContentValues readOpfFile() throws OpfFileIsNullException {

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
				throw new OpfFileIsNullException(
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
}

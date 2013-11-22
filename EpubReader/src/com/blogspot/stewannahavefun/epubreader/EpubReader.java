package com.blogspot.stewannahavefun.epubreader;

import android.net.Uri;
import android.provider.BaseColumns;

public class EpubReader {
	public static final String AUTHORITY = "com.blogspot.stewannahavefun.epubreader.provider";

	private EpubReader() {

	}

	public static final class Books implements BaseColumns {
		private Books() {

		}

		public static final String BOOK_TABLE = "books";
		public static final String CONTENTS_TABLE = "contents";

		public static final String SCHEME = "content://";

		public static final String PATH_BOOKS = "/books";
		public static final String PATH_BOOK_ID = "/books/";
		public static final String PATH_CONTENTS = "/contents";

		public static final int BOOK_ID_PATH_POSITION = 1;

		public static final Uri BOOKS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_BOOKS);
		public static final Uri BOOK_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_BOOK_ID);
		public static final Uri BOOK_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_BOOK_ID + "/#");
		public static final Uri CONTENTS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_CONTENTS);

		public static final String BOOKS_TYPE = "vnd.android.cursor.dir/vnd.stewannahavefun.epub.books";
		public static final String BOOK_ITEM_TYPE = "vnd.android.cursor.item/vnd.stewannahavefun.epub.books";
		public static final String CONTENTS_TYPE = "vnd.android.cursor.dir/vnd.stewannahavefun.epub.contents";

		public static final String BOOK_ID = "book_id";
		public static final String TITLE = "title";
		public static final String AUTHOR = "author";
		public static final String PUBLISHER = "publisher";
		public static final String COVER = "cover";
		public static final String ADDED_DATE = "added_date";
		public static final String LAST_READING_DATE = "last_reading_date";
		public static final String LAST_READING_POINT_NAVIGATION_LINK = "last_reading_point_navigation_link";
		public static final String LAST_READING_POINT_NAVIGATION_ORDER = "last_reading_point_navigation_order";
		public static final String LAST_READING_POINT_PAGE_NUMBER = "last_reading_point_page_number";

		public static final String CONTENTS_ID = "_id";
		public static final String NAVIGATION_LABEL = "navigation_label";
		public static final String NAVIGATION_DEPTH = "navigation_depth";
		public static final String NAVIGATION_ORDER = "navigation_order";
		public static final String NAVIGATION_LINK = "navigation_link";

		public static final String DEFAULT_SORT_ORDER = LAST_READING_DATE
				+ ", " + ADDED_DATE + " DESC";
	}
}

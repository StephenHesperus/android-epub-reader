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

		public static final String TABLE_NAME = "books";

		public static final String SCHEME = "content://";

		public static final String PATH_BOOKS = "/books";
		public static final String PATH_BOOK_ID = "/books/";

		public static final int BOOK_ID_PATH_POSITION = 1;

		public static final Uri BOOKS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_BOOKS);
		public static final Uri BOOK_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_BOOK_ID);
		public static final Uri BOOK_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_BOOK_ID + "/#");

		public static final String CONTENT_TYPE_BOOKS = "vnd.android.cursor.dir/vnd.stewannahavefun.epub.books";
		public static final String CONTENT_TYPE_BOOK_ITEM = "vnd.android.cursor.item/vnd.stewannahavefun.epub.books";

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
		public static final String LOCATION = "location";

		public static final String DEFAULT_SORT_ORDER = LAST_READING_DATE
				+ ", " + ADDED_DATE + " DESC";
	}

	public static final class Contents implements BaseColumns {
		private Contents() {

		}

		public static final String TABLE_NAME = "contents";

		public static final String SCHEME = "content://";

		public static final String PATH_CONTENTS = "/contents";
		public static final String PATH_CONTENTS_ID = "/contents/";

		public static final int CONTENTS_ID_PATH_POSITION = 1;

		public static final Uri CONTENTS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_CONTENTS);
		public static final Uri CONTENTS_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_CONTENTS_ID);
		public static final Uri CONTENTS_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_CONTENTS_ID + "/#");

		public static final String CONTENT_TYPE_CONTENTS = "vnd.android.cursor.dir/vnd.stewannahavefun.epub.contents";
		public static final String CONTENT_TYPE_CONTENTS_ITEM = "vnd.android.cursor.item/vnd.stewannahavefun.epub.contents";

		public static final String NAVIGATION_LABEL = "navigation_label";
		public static final String NAVIGATION_DEPTH = "navigation_depth";
		public static final String NAVIGATION_ORDER = "navigation_order";
		public static final String NAVIGATION_LINK = "navigation_link";
		public static final String BOOK_ID = "book_id";

		public static final String DEFAULT_SORT_ORDER = Contents.NAVIGATION_ORDER
				+ " ASC";
	}

	public static final String[] BOOK_LIST_PROJECTION = new String[] {
			Books._ID, // 0
			Books.TITLE, // 1
			Books.AUTHOR, // 2
			Books.PUBLISHER, // 3
			Books.COVER, // 4
			Books.ADDED_DATE, // 5
			Books.LAST_READING_DATE, // 6
	};
	public static final String[] BOOK_LIST_FROM = new String[] {
			Books.TITLE, // 1
			Books.AUTHOR, // 2
			Books.PUBLISHER, // 3
			Books.COVER, // 4
			Books.ADDED_DATE, // 5
			Books.LAST_READING_DATE, // 6
	};
	public static final String[] READ_BOOK_PROJECTION = new String[] {
			Books._ID,
			Books.TITLE, // 1
			Books.LAST_READING_POINT_NAVIGATION_LINK, // 7
			Books.LAST_READING_POINT_PAGE_NUMBER, // 8
			Books.LAST_READING_POINT_NAVIGATION_ORDER, // 9
			Books.BOOK_ID,
			Books.LOCATION
	};
	public static final String[] CONTENTS_PROJECTION = new String[] {
			Contents._ID, // 0
			Contents.NAVIGATION_LABEL, // 1
			Contents.NAVIGATION_DEPTH, // 4
			Contents.BOOK_ID // 5
	};
	public static final String[] CONTENTS_ITEM_PROJECTION = new String[] {
			Contents._ID, // 0
			Contents.NAVIGATION_LABEL, // 1
			Contents.NAVIGATION_LINK, // 2
			Contents.NAVIGATION_ORDER, // 3
	};
}

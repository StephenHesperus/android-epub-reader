package com.blogspot.stewannahavefun.epubreader;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.blogspot.stewannahavefun.epubreader.EpubReader.Books;
import com.blogspot.stewannahavefun.epubreader.EpubReader.Contents;

public class EpubReaderProvider extends ContentProvider {
	private static final String TAG = "EpubReaderProvider";

	private static final String DATABASE_NAME = "epub_reader.db";
	private static final int DATABASE_VERSION = 1;

	private static HashMap<String, String> sBooksProjectionMap;
	private static HashMap<String, String> sContentsProjectionMap;

	private static final int BOOKS = 1;
	private static final int BOOK_ID = 2;
	private static final int CONTENTS = 3;
	private static final int CONTENTS_ID = 4;

	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(EpubReader.AUTHORITY, "books", BOOKS);
		sUriMatcher.addURI(EpubReader.AUTHORITY, "books/#", BOOK_ID);
		sUriMatcher.addURI(EpubReader.AUTHORITY, "contents", CONTENTS);
		sUriMatcher.addURI(EpubReader.AUTHORITY, "contents/#", CONTENTS_ID);

		sBooksProjectionMap = new HashMap<String, String>();

		sBooksProjectionMap.put(Books._ID, Books._ID);
		sBooksProjectionMap.put(Books.BOOK_ID, Books.BOOK_ID);
		sBooksProjectionMap.put(Books.TITLE, Books.TITLE);
		sBooksProjectionMap.put(Books.AUTHOR, Books.AUTHOR);
		sBooksProjectionMap.put(Books.PUBLISHER, Books.PUBLISHER);
		sBooksProjectionMap.put(Books.COVER, Books.COVER);
		sBooksProjectionMap.put(Books.ADDED_DATE, Books.ADDED_DATE);
		sBooksProjectionMap.put(Books.LAST_READING_DATE,
				Books.LAST_READING_DATE);
		sBooksProjectionMap.put(Books.LAST_READING_POINT_NAVIGATION_LINK,
				Books.LAST_READING_POINT_NAVIGATION_LINK);
		sBooksProjectionMap.put(Books.LAST_READING_POINT_PAGE_NUMBER,
				Books.LAST_READING_POINT_PAGE_NUMBER);
		sBooksProjectionMap.put(Books.LAST_READING_POINT_NAVIGATION_ORDER,
				Books.LAST_READING_POINT_NAVIGATION_ORDER);

		sContentsProjectionMap = new HashMap<String, String>();

		sContentsProjectionMap.put(Contents._ID, Contents.TABLE_NAME + "."
				+ Contents._ID + " AS " + Contents._ID);
		sContentsProjectionMap.put(Contents.NAVIGATION_LABEL,
				Contents.NAVIGATION_LABEL);
		sContentsProjectionMap
				.put(Contents.NAVIGATION_LINK, Contents.NAVIGATION_LINK);
		sContentsProjectionMap.put(Contents.NAVIGATION_DEPTH,
				Contents.NAVIGATION_DEPTH);
		sContentsProjectionMap.put(Contents.NAVIGATION_ORDER,
				Contents.NAVIGATION_ORDER);
		sContentsProjectionMap.put(Contents.BOOK_ID, Contents.BOOK_ID);
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String CREATE_BOOK_TABLE =
				"CREATE TABLE " + Books.TABLE_NAME + " ( "
						+ Books._ID + " INTEGER NOT NULL PRIMARY KEY, "
						+ Books.BOOK_ID + " TEXT NOT NULL, "
						+ Books.TITLE + " TEXT, "
						+ Books.AUTHOR + " TEXT, "
						+ Books.PUBLISHER + " TEXT, "
						+ Books.COVER + " TEXT, "
						+ Books.ADDED_DATE + " INTEGER, "
						+ Books.LAST_READING_DATE + " INTEGER, "
						+ Books.LAST_READING_POINT_NAVIGATION_LINK + " TEXT, "
						+ Books.LAST_READING_POINT_PAGE_NUMBER + " INTEGER, "
						+ Books.LAST_READING_POINT_NAVIGATION_ORDER
						+ " INTEGER"
						+ " );";
		private static final String CREATE_CONTENTS_TABLE =
				"CREATE TABLE " + Contents.TABLE_NAME + " ( "
						+ Contents._ID + " INTEGER NOT NULL PRIMARY KEY, "
						+ Contents.NAVIGATION_LABEL + " TEXT, "
						+ Contents.NAVIGATION_LINK + " TEXT, "
						+ Contents.NAVIGATION_ORDER + " TEXT, "
						+ Contents.NAVIGATION_DEPTH + " TEXT, "
						+ Contents.BOOK_ID + " TEXT NOT NULL REFERENCES "
						+ Books.TABLE_NAME + " ( " + Books.BOOK_ID + " )"
						+ " );";

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_BOOK_TABLE);
			db.execSQL(CREATE_CONTENTS_TABLE);
			Log.d("books table", CREATE_BOOK_TABLE);
			Log.d("contents table", CREATE_CONTENTS_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			// TODO: UPDATE DATABASE INSTEAD OF DESTROYING EXISTING DATA
			db.execSQL("DROP TABLE IF EXISTS " + Books.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Contents.TABLE_NAME);

			onCreate(db);
		}
	}

	private DatabaseHelper mDatabaseHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		String finalWhere;
		int count;

		switch (sUriMatcher.match(uri)) {
		case BOOKS:
			count = db.delete(
					Books.TABLE_NAME,
					selection,
					selectionArgs
					);
			break;

		case BOOK_ID:
			finalWhere = Books._ID + " = "
					+ uri.getPathSegments().get(Books.BOOK_ID_PATH_POSITION);

			if (selection != null) {
				finalWhere += " AND " + selection;
			}

			count = db.delete(
					Books.TABLE_NAME,
					finalWhere,
					selectionArgs
					);
			break;

		case CONTENTS:
			count = db.delete(
					Contents.TABLE_NAME,
					selection,
					selectionArgs
					);
			break;

		case CONTENTS_ID:
			finalWhere = Contents._ID
					+ " = "
					+ uri.getPathSegments().get(
							Contents.CONTENTS_ID_PATH_POSITION);

			if (selection != null) {
				finalWhere += " AND " + selection;
			}

			count = db.delete(
					Contents.TABLE_NAME,
					finalWhere,
					selectionArgs
					);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BOOKS:
			return Books.CONTENT_TYPE_BOOKS;

		case BOOK_ID:
			return Books.CONTENT_TYPE_BOOK_ITEM;

		case CONTENTS:
			return Contents.CONTENT_TYPE_CONTENTS;

		case CONTENTS_ID:
			return Contents.CONTENT_TYPE_CONTENTS_ITEM;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != BOOKS
				&& sUriMatcher.match(uri) != CONTENTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

		long id;
		switch (sUriMatcher.match(uri)) {
		case BOOKS:
			id = db.insert(Books.TABLE_NAME, null, values);
			break;

		case CONTENTS:
			id = db.insert(Contents.TABLE_NAME, null, values);
			break;

		default:
			id = -1;
			break;
		}

		if (id > 0) {
			Uri newItemUri = ContentUris.withAppendedId(uri, id);

			getContext().getContentResolver().notifyChange(uri, null);

			return newItemUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy;

		switch (sUriMatcher.match(uri)) {
		case BOOKS:
			qb.setTables(Books.TABLE_NAME);
			qb.setProjectionMap(sBooksProjectionMap);

			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Books.BOOK_TABLE_DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;

		case BOOK_ID:
			qb.setTables(Books.TABLE_NAME);
			qb.setProjectionMap(sBooksProjectionMap);
			qb.appendWhere(Books._ID + " = "
					+ uri.getPathSegments().get(Books.BOOK_ID_PATH_POSITION)
					);

			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Books.BOOK_TABLE_DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;

		case CONTENTS:
			qb.setTables(Books.TABLE_NAME + " join " + Contents.TABLE_NAME
					+ " using ( " + Books.BOOK_ID + " )");
			qb.setProjectionMap(sContentsProjectionMap);

			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Contents.CONTENTS_TABLE_DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;

		case CONTENTS_ID:
			qb.setTables(Books.TABLE_NAME + " join " + Contents.TABLE_NAME
					+ " using ( " + Books.BOOK_ID + " )");
			qb.setProjectionMap(sContentsProjectionMap);
			qb.appendWhere(Contents._ID
					+ " = "
					+ uri.getPathSegments().get(
							Contents.CONTENTS_ID_PATH_POSITION)
					);

			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Contents.CONTENTS_TABLE_DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

		Cursor c = qb.query(
				db,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				orderBy
				);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		String finalWhere;
		int count;

		switch (sUriMatcher.match(uri)) {
		case BOOKS:
			count = db.update(
					Books.TABLE_NAME,
					values,
					selection,
					selectionArgs
					);
			break;

		case BOOK_ID:
			finalWhere = Books._ID + " = "
					+ uri.getPathSegments().get(Books.BOOK_ID_PATH_POSITION);

			if (selection != null) {
				finalWhere += " AND " + selection;
			}

			count = db.update(
					Books.TABLE_NAME,
					values,
					finalWhere,
					selectionArgs
					);
			break;

		case CONTENTS:
			count = db.update(
					Contents.TABLE_NAME,
					values,
					selection,
					selectionArgs
					);
			break;

		case CONTENTS_ID:
			finalWhere = Contents._ID
					+ " = "
					+ uri.getPathSegments().get(
							Contents.CONTENTS_ID_PATH_POSITION);

			if (selection != null) {
				finalWhere += " AND " + selection;
			}

			count = db.update(
					Contents.TABLE_NAME,
					values,
					finalWhere,
					selectionArgs
					);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

}
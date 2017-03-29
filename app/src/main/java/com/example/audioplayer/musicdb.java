package com.example.audioplayer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class MusicDB extends ContentProvider {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "musicdb";
    private static final String AUTHORITY = "musicdbauth";
    static final int URI_LIST = 1;
    static final String DB_TABLE_NAME = "musiclist";
    private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DB_TABLE_NAME;
    static final String DB_SONG_NAME = "songname";
    static final String DB_SDPATH = "localpath";
    static final String DB_IPATH = "ipath";
    private static final String DB_TABLE_CREATE = "CREATE TABLE " + DB_TABLE_NAME
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + DB_SONG_NAME + " TEXT NOT NULL,"
            + DB_SDPATH + " TEXT,"
            + DB_IPATH + " TEXT);";


    databasehelper dbHelper;
    SQLiteDatabase database;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, DB_TABLE_NAME, URI_LIST);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == URI_LIST)
            return CONTENT_TYPE;
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        database = dbHelper.getWritableDatabase();
        database.insert(DB_TABLE_NAME, null, values);

        return uri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new databasehelper(getContext());
        return dbHelper == null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DB_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class databasehelper extends SQLiteOpenHelper {

        public databasehelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_TABLE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

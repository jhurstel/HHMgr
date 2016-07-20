package com.hurstel.jerome.hhmgr;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class LivePokerNotesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "LivePokerNotesDB";
    private static LivePokerNotesDbHelper sInstance;

    public static synchronized LivePokerNotesDbHelper getInstance(Context context) {
        if (sInstance == null) { sInstance = new LivePokerNotesDbHelper(context); }
        return sInstance;
    }

    public LivePokerNotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d("Helper", "onCreate()");
        db.execSQL(SessionManager.SQL_CREATE_TABLE);
        db.execSQL(HandManager.SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Helper", "onUpgrade()");
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SessionManager.SQL_DELETE_TABLE);
        db.execSQL(HandManager.SQL_DELETE_TABLE);
        onCreate(db);
    }
}

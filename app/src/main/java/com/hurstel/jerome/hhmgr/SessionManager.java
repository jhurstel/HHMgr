package com.hurstel.jerome.hhmgr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Locale;

public class SessionManager {

    public static final String TABLE_NAME = "Sessions";
    public static final String KEY_NAME = "name";
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_SB = "sb";
    public static final String KEY_BB = "bb";
    public static final String KEY_ANTE = "ante";
    public static final String KEY_STACK = "stack";
    public static final String KEY_PLAYERS = "players";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_NAME + " TEXT," +
                    KEY_DATE + " TEXT," +
                    KEY_TYPE + " TEXT," +
                    KEY_SB + " INTEGER," +
                    KEY_BB + " INTEGER," +
                    KEY_ANTE + " INTEGER," +
                    KEY_STACK + " INTEGER," +
                    KEY_PLAYERS + " INTEGER" +
                    ")";
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private LivePokerNotesDbHelper mDbHelper;
    private SQLiteDatabase db;

    // Constructor
    public SessionManager(Context context)
    {
        mDbHelper = mDbHelper.getInstance(context);
    }

    public void open()
    {
        db = mDbHelper.getWritableDatabase();
    }

    public void close()
    {
        db.close();
    }

    public long addSession(Session session) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, session.getName());
        values.put(KEY_DATE, session.getDate());
        values.put(KEY_TYPE, session.getType());
        values.put(KEY_SB, session.getSB());
        values.put(KEY_BB, session.getBB());
        values.put(KEY_ANTE, session.getAnte());
        values.put(KEY_STACK, session.getStack());
        values.put(KEY_PLAYERS, session.getPlayers());

        long newRowId = db.insert(TABLE_NAME, null, values);

        Log.d("SessionManager", String.format(Locale.FRENCH, "insert session %s at row %d", session.toString(), newRowId));
        return newRowId;
    }

    public int modSession(Session session) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, session.getName());
        values.put(KEY_DATE, session.getDate());
        values.put(KEY_TYPE, session.getType());
        values.put(KEY_SB, session.getSB());
        values.put(KEY_BB, session.getBB());
        values.put(KEY_ANTE, session.getAnte());
        values.put(KEY_STACK, session.getStack());
        values.put(KEY_PLAYERS, session.getPlayers());

        String where = KEY_NAME + "=?";
        String[] whereArgs = { session.getName() };

        int nb = db.update(TABLE_NAME, values, where, whereArgs);

        Log.d("SessionManager", String.format(Locale.FRENCH, "modify %d session %s", nb, session.toString()));
        return nb;
    }

    public int delSession(Session session) {
        String where = KEY_NAME + " = ?";
        String[] whereArgs = { session.getName() + "" };

        int nb = db.delete(TABLE_NAME, where, whereArgs);

        Log.d("SessionManager", String.format(Locale.FRENCH, "delete %d session %s", nb, session.toString()));
        return nb;
    }

    public Session getSession(String name) {
        Session s = new Session();

        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_NAME + "='" + name + "'", null);
        if (c.moveToFirst()) {
            s.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            s.setType(c.getString(c.getColumnIndex(KEY_TYPE)));
            s.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
            s.setSB(c.getInt(c.getColumnIndex(KEY_SB)));
            s.setBB(c.getInt(c.getColumnIndex(KEY_BB)));
            s.setAnte(c.getInt(c.getColumnIndex(KEY_ANTE)));
            s.setStack(c.getInt(c.getColumnIndex(KEY_STACK)));
            s.setPlayers(c.getInt(c.getColumnIndex(KEY_PLAYERS)));
            c.close();
        }

        Log.d("SessionManager", String.format(Locale.FRENCH, "read session %s", s.toString()));
        return s;
    }

    public Cursor getSessions() {
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

}


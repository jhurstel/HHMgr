package com.hurstel.jerome.hhmgr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Locale;

public class HandManager {

    public static final String TABLE_NAME = "Hands";
    public static final String KEY_SESSION = "session";
    public static final String KEY_ID = "id";
    public static final String KEY_SB = "sb";
    public static final String KEY_BB = "bb";
    public static final String KEY_ANTE = "ante";
    public static final String KEY_STACK = "stack";
    public static final String KEY_PLAYERS = "players";
    public static final String KEY_POSITION = "position";
    public static final String KEY_FAVORITE = "favorite";
    public static final String KEY_SUMMARY = "summary";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_SESSION + " TEXT," +
                    KEY_ID + " INTEGER," +
                    KEY_SB + " INTEGER," +
                    KEY_BB + " INTEGER," +
                    KEY_ANTE + " INTEGER," +
                    KEY_STACK + " INTEGER," +
                    KEY_PLAYERS + " INTEGER," +
                    KEY_POSITION + " INTEGER," +
                    KEY_FAVORITE + " INTEGER," +
                    KEY_SUMMARY + " TEXT" +
                    ")";
    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private LivePokerNotesDbHelper mDbHelper;
    private SQLiteDatabase db;

    // Constructor
    public HandManager(Context context)
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

    public long addHand(Hand hand) {
        ContentValues values = new ContentValues();
        values.put(KEY_SESSION, hand.getSession());
        values.put(KEY_ID, hand.getId());
        values.put(KEY_SB, hand.getSB());
        values.put(KEY_BB, hand.getBB());
        values.put(KEY_ANTE, hand.getAnte());
        values.put(KEY_STACK, hand.getStack());
        values.put(KEY_PLAYERS, hand.getPlayers());
        values.put(KEY_POSITION, hand.getPosition());
        values.put(KEY_FAVORITE, hand.isFavorite() ? 1 : 0);
        values.put(KEY_SUMMARY, hand.getSummary());

        long newRowId = db.insert(TABLE_NAME, null, values);

        Log.d("HandManager", String.format(Locale.FRENCH, "insert hand %s at row %d", hand.toString(), newRowId));
        return newRowId;
    }

    public int modHand(Hand hand) {
        ContentValues values = new ContentValues();
        values.put(KEY_SESSION, hand.getSession());
        values.put(KEY_ID, hand.getId());
        values.put(KEY_SB, hand.getSB());
        values.put(KEY_BB, hand.getBB());
        values.put(KEY_ANTE, hand.getAnte());
        values.put(KEY_STACK, hand.getStack());
        values.put(KEY_PLAYERS, hand.getPlayers());
        values.put(KEY_POSITION, hand.getPosition());
        values.put(KEY_FAVORITE, hand.isFavorite() ? 1 : 0);
        values.put(KEY_SUMMARY, hand.getSummary());

        String where = KEY_ID + "=?";
        String[] whereArgs = { String.valueOf(hand.getId()) };

        int nb = db.update(TABLE_NAME, values, where, whereArgs);

        Log.d("HandManager", String.format(Locale.FRENCH, "modify %d hand %s", nb, hand.toString()));
        return nb;
    }

    public int delHand(Hand hand) {
        String where = KEY_ID + "=?";
        String[] whereArgs = { String.valueOf(hand.getId()) };

        int nb = db.delete(TABLE_NAME, where, whereArgs);

        Log.d("HandManager", String.format(Locale.FRENCH, "delete %d hand %s", nb, hand.toString()));
        return nb;
    }

    public Hand getHand(int id) {
        Hand h = new Hand();

        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=" + id, null);
        if (c.moveToFirst()) {
            h.setSession(c.getString(c.getColumnIndex(KEY_SESSION)));
            h.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            h.setSB(c.getInt(c.getColumnIndex(KEY_SB)));
            h.setBB(c.getInt(c.getColumnIndex(KEY_BB)));
            h.setAnte(c.getInt(c.getColumnIndex(KEY_ANTE)));
            h.setStack(c.getInt(c.getColumnIndex(KEY_STACK)));
            h.setPlayers(c.getInt(c.getColumnIndex(KEY_PLAYERS)));
            h.setPosition(c.getInt(c.getColumnIndex(KEY_POSITION)));
            h.setFavorite(c.getInt(c.getColumnIndex(KEY_FAVORITE)) != 0 ? true : false);
            h.setSummary(c.getString(c.getColumnIndex(KEY_SUMMARY)));
            c.close();
        }

        Log.d("HandManager", String.format(Locale.FRENCH, "read hand %s", h.toString()));
        return h;
    }

    public Cursor getHands() {
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor getHands(String session) {
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_SESSION + "='" + session + "'", null);
    }

}

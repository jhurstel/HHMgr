package com.hurstel.jerome.hhmgr;

import android.database.Cursor;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Session {

    private String mName;
    private String mDate;
    private String mType;
    private int mSB;
    private int mBB;
    private int mAnte;
    private int mStack;
    private int mPlayers;

    // Constructor
    public Session() {
        mName = "";
        mDate = "";
        mType = "";
        mSB = 10;
        mBB = 20;
        mAnte = 0;
        mStack = 1000;
        mPlayers = 4;
    }

    // Constructor
    public Session(Bundle b) {
        fromBundle(b);
    }

    // Constructor
    public Session(String n, String date, String t, int sb, int bb, int ante, int s, int p) {
        mName = n;
        mDate = date;
        mType = t;
        mSB = sb;
        mBB = bb;
        mAnte = ante;
        mStack = s;
        mPlayers = p;
    }

    // Constructor
    public Session(Cursor c) {
        mName = c.getString(c.getColumnIndexOrThrow(SessionManager.KEY_NAME));
        mDate = c.getString(c.getColumnIndexOrThrow(SessionManager.KEY_DATE));
        mType = c.getString(c.getColumnIndexOrThrow(SessionManager.KEY_TYPE));
        mSB = c.getInt(c.getColumnIndexOrThrow(SessionManager.KEY_SB));
        mBB = c.getInt(c.getColumnIndexOrThrow(SessionManager.KEY_BB));
        mAnte = c.getInt(c.getColumnIndexOrThrow(SessionManager.KEY_ANTE));
        mStack = c.getInt(c.getColumnIndexOrThrow(SessionManager.KEY_STACK));
        mPlayers = c.getInt(c.getColumnIndexOrThrow(SessionManager.KEY_PLAYERS));
    }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public String getType() { return mType; }
    public void setType(String type) { mType = type; }

    public int getSB() { return mSB; }
    public void setSB(int sb) { mSB = sb; }

    public int getBB() { return mBB; }
    public void setBB(int bb) { mBB = bb; }

    public int getAnte() { return mAnte; }
    public void setAnte(int ante) { mAnte = ante; }

    public int getStack() { return mStack; }
    public void setStack(int stack) { mStack = stack; }

    public int getPlayers() { return mPlayers; }
    public void setPlayers(int players) { mPlayers = players; }

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public String toString() {
        return String.format(Locale.FRENCH, "(name=%s,type=%s,sb=%d,bb=%d,ante=%d,stack=%d,players=%d, date=%s)", mName, mType, mSB, mBB, mAnte, mStack, mPlayers, mDate);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("NAME", mName);
        b.putString("TYPE", mType);
        b.putInt("SB", mSB);
        b.putInt("BB", mBB);
        b.putInt("ANTE", mAnte);
        b.putInt("STACK", mStack);
        b.putInt("PLAYERS", mPlayers);
        b.putString("DATE", mDate);
        return b;
    }

    public void fromBundle(Bundle b) {
        mName = b.getString("NAME");
        mType = b.getString("TYPE");
        mSB = b.getInt("SB");
        mBB = b.getInt("BB");
        mAnte = b.getInt("ANTE");
        mStack = b.getInt("STACK");
        mPlayers = b.getInt("PLAYERS");
        mDate = b.getString("DATE");
    }


    static private List<Session> mSessionList = new ArrayList<>();

    static public List<Session> getSessions() { return mSessionList; };

    static public Session getSession(String title) {
        for (int i=0; i<mSessionList.size(); i++) {
            if (mSessionList.get(i).mName.equalsIgnoreCase(title)) {
                return mSessionList.get(i);
            }
        }
        return null;
    }

    static public Session getSession(int position) {
        return mSessionList.get(position);
    }
}

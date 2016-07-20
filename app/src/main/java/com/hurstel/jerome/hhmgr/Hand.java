package com.hurstel.jerome.hhmgr;

import android.database.Cursor;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class Hand {

    private String mSession;
    private int mId;
    private int mSB;
    private int mBB;
    private int mAnte;
    private int mStack;
    private int mPlayers;
    private int mPosition;
    private boolean mFavorite;
    private String mSummary;

    // Constructor
    public Hand() {
        mSession = "";
        mId = 0;
        mSB = 10;
        mBB = 20;
        mAnte = 0;
        mStack = 1000;
        mPlayers = 4;
        mPosition = 2;
        mFavorite = false;
        mSummary = "";
    }

    // Constructor
    public Hand(String str, int n, int sb, int bb, int a, int s, int p, int pos, boolean fav, String sum) {
        mSession = str;
        mId = n;
        mSB = sb;
        mBB = bb;
        mAnte = a;
        mStack = s;
        mPlayers = p;
        mPosition = pos;
        mFavorite = fav;
        mSummary = sum;
    }


    // Constructor
    public Hand(Bundle b) {
        fromBundle(b);
    }

    // Constructor
    public Hand(Cursor c) {
        mSession = c.getString(c.getColumnIndexOrThrow(HandManager.KEY_SESSION));
        mId = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_ID));
        mSB = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_SB));
        mBB = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_BB));
        mAnte = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_ANTE));
        mStack = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_STACK));
        mPlayers = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_PLAYERS));
        mPosition = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_POSITION));
        mFavorite = c.getInt(c.getColumnIndexOrThrow(HandManager.KEY_FAVORITE)) != 0;
        mSummary = c.getString(c.getColumnIndexOrThrow(HandManager.KEY_SUMMARY));
    }

    // Copy constructor
    public Hand(Hand h) {
        mSession = h.getSession();
        mId = h.getId();
        mSB = h.getSB();
        mBB = h.getBB();
        mAnte = h.getAnte();
        mStack = h.getSB();
        mPlayers = h.getPlayers();
        mPosition = h.getPosition();
        mFavorite = h.isFavorite();
        mSummary = h.getSummary();
    }

    public String getSession() { return mSession; }
    public void setSession(String name) { mSession = name; }

    public int getId() { return mId; }
    public void setId(int id) { mId = id; }

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

    public int getPosition() { return mPosition; }
    public void setPosition(int position) { mPosition = position; }

    public boolean isFavorite() { return mFavorite; }
    public void setFavorite(boolean favorite) { mFavorite = favorite; }
    public void toggleFavorite() { mFavorite = !mFavorite; }

    public String getSummary() { return mSummary; }
    public void setSummary(String sum) { mSummary = sum; }
    public void appendSummary(String sum) { mSummary += sum; }

    public String toString() {
        return String.format(Locale.FRENCH, "(session=%s,id=%d,sb=%d,bb=%d,ante=%d,stack=%d,players=%d,position=%d,favorite=%d,\nsummary=%s)", mSession, mId, mSB, mBB, mAnte, mStack, mPlayers, mPosition, mFavorite ? 1 : 0, mSummary);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("SESSION", mSession);
        b.putInt("ID", mId);
        b.putInt("SB", mSB);
        b.putInt("BB", mBB);
        b.putInt("ANTE", mAnte);
        b.putInt("STACK", mStack);
        b.putInt("PLAYERS", mPlayers);
        b.putInt("POSITION", mPosition);
        b.putInt("FAVORITE", mFavorite ? 1 : 0);
        b.putString("SUMMARY", mSummary);
        return b;
    }

    public void fromBundle(Bundle b) {
        mSession = b.getString("SESSION");
        mId = b.getInt("ID");
        mSB = b.getInt("SB");
        mBB = b.getInt("BB");
        mAnte = b.getInt("ANTE");
        mStack = b.getInt("STACK");
        mPlayers = b.getInt("PLAYERS");
        mPosition = b.getInt("POSITION");
        mFavorite = b.getInt("FAVORITE") == 1;
        mSummary = b.getString("SUMMARY");
    }

    static private List<Hand> mHandList = new ArrayList<>();

    static public List<Hand> getHands() { return mHandList; };
}
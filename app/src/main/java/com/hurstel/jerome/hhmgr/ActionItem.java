package com.hurstel.jerome.hhmgr;

import android.os.Parcel;
import android.os.Parcelable;

enum When {
    PREFLOP,
    FLOP,
    TURN,
    RIVER;
}

enum Action {
    FOLD,
    CALL,
    RAISE;
}

class ActionItem implements Parcelable {

    When when;

    int seat;

    Action action;

    int amount;

    // Constructor
    public ActionItem(When w, int s, Action a) {
        when = w;
        seat = s;
        action=a;
    }

    // Constructor
    public ActionItem(When w, int s, Action a, int r) {
        when = w;
        seat = s;
        action=a;
        amount=r;
    }

    protected ActionItem(Parcel in) {
        when = (When) in.readValue(When.class.getClassLoader());
        seat = in.readInt();
        action = (Action) in.readValue(Action.class.getClassLoader());
        amount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(when);
        dest.writeInt(seat);
        dest.writeValue(action);
        dest.writeInt(amount);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ActionItem> CREATOR = new Parcelable.Creator<ActionItem>() {
        @Override
        public ActionItem createFromParcel(Parcel in) {
            return new ActionItem(in);
        }

        @Override
        public ActionItem[] newArray(int size) {
            return new ActionItem[size];
        }
    };
}
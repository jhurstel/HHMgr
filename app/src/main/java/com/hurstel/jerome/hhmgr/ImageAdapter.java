package com.hurstel.jerome.hhmgr;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Integer> mHiddenCards;

    public ImageAdapter(Context c, ArrayList<Integer> l) {
        mContext = c;
        mHiddenCards = l;
    }

    public int getCount() {
        return mVerticalThumbIds.length;
    }

    public Object getItem(int position) {
        return mVerticalThumbNames[position];
    }

    public long getItemId(int position) {
        return mVerticalThumbIds[position];
    }

    public ArrayList<Integer> getList() { return mHiddenCards; }

    static public String convertHand(int id) { return mVerticalThumbNames[id]; }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(180, 180));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        if (mHiddenCards.contains(position)) {
            imageView.setImageResource(R.drawable.back);
        } else {
            imageView.setImageResource(mVerticalThumbIds[position]);
        }
        return imageView;
    }

    // references to our images
    //private Integer[] mHorizontalThumbIds = {
    //    R.drawable._2c, R.drawable._3c, R.drawable._4c, R.drawable._5c, R.drawable._6c, R.drawable._7c, R.drawable._8c, R.drawable._9c, R.drawable._tc, R.drawable._jc, R.drawable._qc, R.drawable._kc, R.drawable._ac,
    //    R.drawable._2d, R.drawable._3d, R.drawable._4d, R.drawable._5d, R.drawable._6d, R.drawable._7d, R.drawable._8d, R.drawable._9d, R.drawable._td, R.drawable._jd, R.drawable._qd, R.drawable._kd, R.drawable._ad,
    //    R.drawable._2h, R.drawable._3h, R.drawable._4h, R.drawable._5h, R.drawable._6h, R.drawable._7h, R.drawable._8h, R.drawable._9h, R.drawable._th, R.drawable._jh, R.drawable._qh, R.drawable._kh, R.drawable._ah,
    //    R.drawable._2s, R.drawable._3s, R.drawable._4s, R.drawable._5s, R.drawable._6s, R.drawable._7s, R.drawable._8s, R.drawable._9s, R.drawable._ts, R.drawable._js, R.drawable._qs, R.drawable._ks, R.drawable._as
    //};
    private Integer[] mVerticalThumbIds = {
            R.drawable._2c, R.drawable._2d, R.drawable._2h, R.drawable._2s,
            R.drawable._3c, R.drawable._3d, R.drawable._3h, R.drawable._3s,
            R.drawable._4c, R.drawable._4d, R.drawable._4h, R.drawable._4s,
            R.drawable._5c, R.drawable._5d, R.drawable._5h, R.drawable._5s,
            R.drawable._6c, R.drawable._6d, R.drawable._6h, R.drawable._6s,
            R.drawable._7c, R.drawable._7d, R.drawable._7h, R.drawable._7s,
            R.drawable._8c, R.drawable._8d, R.drawable._8h, R.drawable._8s,
            R.drawable._9c, R.drawable._9d, R.drawable._9h, R.drawable._9s,
            R.drawable._tc, R.drawable._td, R.drawable._th, R.drawable._ts,
            R.drawable._jc, R.drawable._jd, R.drawable._jh, R.drawable._js,
            R.drawable._qc, R.drawable._qd, R.drawable._qh, R.drawable._qs,
            R.drawable._kc, R.drawable._kd, R.drawable._kh, R.drawable._ks,
            R.drawable._ac, R.drawable._ad, R.drawable._ah, R.drawable._as
    };
    static private String[] mVerticalThumbNames = {
            "2c", "2d", "2h", "2s",
            "3c", "3d", "3h", "3s",
            "4c", "4d", "4h", "4s",
            "5c", "5d", "5h", "5s",
            "6c", "6d", "6h", "6s",
            "7c", "7d", "7h", "7s",
            "8c", "8d", "8h", "8s",
            "9c", "9d", "9h", "9s",
            "Tc", "Td", "Th", "Ts",
            "Jc", "Jd", "Jh", "Js",
            "Qc", "Qd", "Qh", "Qs",
            "Kc", "Kd", "Kh", "Ks",
            "Ac", "Ad", "Ah", "As"
    };
}
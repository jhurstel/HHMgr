package com.hurstel.jerome.hhmgr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class HandActivityFragment extends Fragment implements HandArrayAdapter.OnHandItemClickListener{

    private RecyclerView mRecyclerView;
    private HandArrayAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HandActivityFragment", "onCreate()");

        Hand prefs = new Hand(getArguments());

        // Query Hand DB
        HandManager m = new HandManager(getActivity());
        m.open();
        Cursor c = m.getHands(prefs.getSession());
        if (c.moveToFirst())
        {
            do {
                Hand h = new Hand(c);
                Hand.getHands().add(h);
                Log.d("HandActivityFragment", String.format("read element %s", h.toString()));
            }
            while (c.moveToNext());
        }
        c.close();
        m.close();

        mAdapter = new HandArrayAdapter(Hand.getHands());
        ((HandActivity)getActivity()).setHandAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("HandActivityFragment", "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_hand, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewHands);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnHandItemClickListener(this);

        return rootView;
    }

    public void onFavoriteButtonClick(View v, int position) {
        // Modify hand: toggle favorite
        Hand.getHands().get(position).toggleFavorite();

        // Update Hand DB & notifyDataSetChanged
        HandManager hm = new HandManager(getActivity());
        hm.open();
        hm.modHand(Hand.getHands().get(position));
        hm.close();
        mAdapter.notifyDataSetChanged();
    }
}

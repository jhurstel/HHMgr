package com.hurstel.jerome.hhmgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SessionActivityFragment extends ListFragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("SessionActivityFragment", "onAttach()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("SessionActivityFragment", "onActivityCreated()");

        SessionManager m = new SessionManager(getActivity());
        m.open();
        Cursor c = m.getSessions();
        if (c.moveToFirst())
        {
            do {
                Session s = new Session(c);
                Session.getSessions().add(s);
                Log.d("SessionFragment", String.format("read element %s", s.toString()));
            }
            while (c.moveToNext());
        }
        c.close();
        m.close();

        SessionArrayAdapter adapter = new SessionArrayAdapter(getActivity(), R.layout.session_item, Session.getSessions());
        setListAdapter(adapter);
        ((SessionActivity)getActivity()).setSessionAdapter(adapter);

        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_longclick, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                SessionManager m = new SessionManager(getContext());
                m.open();
                Session s = Session.getSession(info.position);
                Session.getSessions().remove(s);
                m.delSession(s);
                m.close();

                ((SessionActivity)getActivity()).getSessionAdapter().notifyDataSetChanged();
                return true;
            case R.id.action_export:
                // TODO: export all hands
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), HandActivity.class);
        Session s = Session.getSessions().get(position);
        intent.putExtras(s.toBundle());
        startActivity(intent);
    }

}

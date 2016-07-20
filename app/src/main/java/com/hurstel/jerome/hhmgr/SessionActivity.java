package com.hurstel.jerome.hhmgr;

import android.support.v4.app.DialogFragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SessionActivity extends AppCompatActivity {

    // AdapterView
    private SessionArrayAdapter mSessionArrayAdapter;
    public void setSessionAdapter(SessionArrayAdapter a) { mSessionArrayAdapter = a; }
    public SessionArrayAdapter getSessionAdapter() { return mSessionArrayAdapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SessionActivity", "onCreate()");

        setContentView(R.layout.activity_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_session);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_session);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment f = new SessionEntryDialogFragment();
                f.show(getSupportFragmentManager(), "edit_new_session");
            }
        });

        // Clear session list
        Session.getSessions().clear();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        Log.d("SessionActivity", "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first

        Log.d("SessionActivity", "onStop()");
    }

    @Override
    public void onRestart() {
        super.onRestart();  // Always call the superclass method first

        Log.d("SessionActivity", "onRestart()");
    }

    @Override
    public void onStart() {
        super.onStart();  // Always call the superclass method first

        Log.d("SessionActivity", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d("SessionActivity", "onResume()");

        mSessionArrayAdapter.notifyDataSetChanged();
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------ O P T I O N   M E N U ----------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_session_db) {
            // Clear session
            SessionManager m = new SessionManager(this);
            m.open();
            Iterator itr = Session.getSessions().iterator();
            while (itr.hasNext()) {
                Session s = (Session) itr.next();
                Session.getSessions().remove(s);
                m.delSession(s);
            }
            m.close();

            mSessionArrayAdapter.notifyDataSetChanged();

            // Also clear all hands in that session
            /*
            HandManager m = new HandManager(this);
            m.open();
            Iterator itr = Hand.getHands().iterator();
            while (itr.hasNext()) {
                Hand h = (Hand) itr.next();
                Hand.getHands().remove(h);
                long n = m.delHand(h);
                Log.d("DB", String.format(Locale.FRENCH, "delete %d element %s", n, h.toString()));
            }
            m.close();
            */
            return true;
        } else
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------- D I A L O G   F R A G M E N T ------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void doPositiveClick(Session s) {
        Log.d("SessionActivity", "Positive click!");

        Session.getSessions().add(s);

        SessionManager m = new SessionManager(this);
        m.open();
        m.addSession(s);
        m.close();

        mSessionArrayAdapter.notifyDataSetChanged();
    }

}

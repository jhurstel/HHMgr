package com.hurstel.jerome.hhmgr;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class HandActivity extends AppCompatActivity {

    //
    private Hand mPreferences;

    //
    private HandArrayAdapter mHandArrayAdapter;
    public void setHandAdapter(HandArrayAdapter adapter) { mHandArrayAdapter = adapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand);
        Log.d("HandActivity", "onCreate()");

        // Set hand preferences based on session info
        Session s = new Session(getIntent().getExtras());
        mPreferences = new Hand(
                s.getName(),
                0,      // fake id
                s.getSB(),
                s.getBB(),
                s.getAnte(),
                s.getStack(),
                s.getPlayers(),
                0,      // fake position
                false,  // fake favorite
                "NULL"  // fake summary
        );

        // --------
        // TOOLBAR
        // --------

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_form);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        // -----------------------
        // FLOATING ACTION BUTTON
        // -----------------------

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_hand);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = new HandEntryDialogFragment();
                fragment.setArguments(mPreferences.toBundle());
                fragment.show(getSupportFragmentManager(), "edit_new_hand");
            }
        });

        // Clear hand list
        Hand.getHands().clear();

        if (savedInstanceState == null) {
            Log.d("HandActivity", "beginTransaction");

            HandActivityFragment fragment = new HandActivityFragment();
            fragment.setArguments(mPreferences.toBundle());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.hand_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        Log.d("HandActivity", "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first

        Log.d("HandActivity", "onStop()");
    }

    @Override
    public void onRestart() {
        super.onRestart();  // Always call the superclass method first

        Log.d("HandActivity", "onRestart()");
    }

    @Override
    public void onStart() {
        super.onStart();  // Always call the superclass method first

        Log.d("HandActivity", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.d("HandActivity", "onResume()");
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------ O P T I O N   M E N U ----------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hand, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_hand_db) {
            HandManager m = new HandManager(this);
            m.open();
            Iterator itr = Hand.getHands().iterator();
            while (itr.hasNext()) {
                Hand h = (Hand) itr.next();
                Hand.getHands().remove(h);
                m.delHand(h);
            }
            m.close();
            mHandArrayAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------- D I A L O G   F R A G M E N T ------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void doPositiveClick(Hand h) {

        // Read bundle and update Preferences
        mPreferences = h;

        // Add new hand
        Hand.getHands().add(h);

        // Update Hand DB & notifyDataSetChanged
        HandManager hm = new HandManager(this);
        hm.open();
        hm.addHand(h);
        hm.close();
        mHandArrayAdapter.notifyDataSetChanged();

        // Update Session DB, notifyDataSetChanged is called onResume
        Session s = Session.getSession(mPreferences.getSession());
        s.setSB(mPreferences.getSB());
        s.setBB(mPreferences.getBB());
        s.setAnte(mPreferences.getAnte());
        s.setStack(mPreferences.getStack());
        s.setPlayers(mPreferences.getPlayers());
        SessionManager sm = new SessionManager(this);
        sm.open();
        sm.modSession(s);
        sm.close();

        // Update preference for next hand
        mPreferences.setId(Hand.getHands().size()+1);
        mPreferences.setPosition(mPreferences.getPosition() + 1 % mPreferences.getPlayers());
        mPreferences.setSummary("");
    }
}

package com.hurstel.jerome.hhmgr;

import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class HandEntryDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener,
        TextView.OnEditorActionListener, View.OnClickListener, ChooseCardDialogListener {

    private int MY_IMAGE_WIDTH = 100;
    private int MY_IMAGE_HEIGHT = 100;

    enum State {
        INIT,
        PREFLOP_ACTIONS,
        FLOP_1ST_CARD,
        FLOP_2ND_CARD,
        FLOP_3RD_CARD,
        FLOP_ACTIONS,
        TURN_CARD,
        TURN_ACTIONS,
        RIVER_CARD,
        RIVER_ACTIONS,
        SHOWDOWN_HAND_1ST_CARD,
        SHOWDOWN_HAND_2ND_CARD,
        WINNER,
        SHOWDOWN
    }

    class Player {
        int mId;
        String mSeat;
        int mChipsPutInPot;
        int mStack;
        ArrayList<ActionItem> mListOfActions;
        ArrayList<Integer> mHandCards;

        // Constructor
        public Player(int id, String seat) {
            mId = id;
            mSeat = seat;
            mChipsPutInPot = 0;
            mStack = 0;
            mListOfActions = new ArrayList<>();
            mHandCards = new ArrayList<>();
        }

        boolean hasFolded() {
            return mListOfActions.size() > 0 && (mListOfActions.get(mListOfActions.size() - 1).action == Action.FOLD);
        }

        String getSeat(boolean star) {
            CharSequence hero = "";
            if (mId == spinnerPos.getSelectedItemPosition()) {
                hero = star ? "*" : "[Hero]";
            }
            return String.format(Locale.FRENCH, "%s%s", mSeat, hero);
        }

        String getSeat() {
            CharSequence hero = "";
            if (mId == spinnerPos.getSelectedItemPosition()) {
                hero = "[Hero]";
            }
            return String.format(Locale.FRENCH, "%s%s", mSeat, hero);
        }

        String getHand() {
            return String.format(Locale.FRENCH, "%s %s", ImageAdapter.convertHand(mHandCards.get(0)), ImageAdapter.convertHand(mHandCards.get(1)));
        }

        String getStack() {
            return String.format(Locale.FRENCH, "%s", (mStack > 0) ? String.valueOf(mStack) : String.valueOf(100*Integer.parseInt(etBB.getText().toString())));
        }
    }

    class PlayerArrayList extends ArrayList<Player> {

        Player getNextPlayerFrom(int id) {
            Player p = get(id % size());
            for (int i = id; i < id + size(); i++) {
                p = get(i % size());
                if (!p.hasFolded()) {
                    return p;
                }
            }
            return p;
        }
    }

    private State mState = State.INIT;
    private int mCurrPlayer = 0;
    private int mWinPlayer = 0;
    private PlayerArrayList mListOfPlayers = new PlayerArrayList();
    private int mNbOfPlayersRemaining = 0;
    private int mPotSize = 0;
    private int mNbOfCallsAfterRaise = 0;
    private int mLastBet = 0;
    private ArrayList<Integer> mBoardCards = new ArrayList<>();

    // The root view
    View mRootView;
    ScrollView mScroller;

    // The hand
    Hand mHand;

    // General info
    Spinner spinnerPos, spinnerPlayer;
    EditText etSB, etBB, etAnte, etStack;
    TextView tvId, tvM;
    Button bInit;

    // ActionSelection stub layout
    TextView tvActionLabel, tvAction;
    ViewGroup rgActionSelection;

    // ActionItem stub layout
    TextView tvSeat;
    Button rbFold, rbCall, rbRaise;
    EditText etRaise;

    // CardSelection stub layout
    TextView tvTitle;
    ImageView ivCard1, ivCard2, ivCard3;
    Button bCard;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mRootView = getActivity().getLayoutInflater().inflate(R.layout.hand_entry, null);
        assert mRootView != null;

        mScroller = (ScrollView) mRootView.findViewById(R.id.scrollViewHandEntry);
        assert mScroller != null;

        Bundle b = getArguments();
        mHand = new Hand(b);

        tvId = (TextView) mRootView.findViewById(R.id.textViewId);
        assert tvId != null;
        tvId.setText(String.format(Locale.FRENCH, "#%d", mHand.getId()));

        etSB = (EditText) mRootView.findViewById(R.id.editTextSB);
        assert etSB != null;
        etSB.setOnEditorActionListener(this);
        etSB.setText(String.valueOf(mHand.getSB()));

        etBB = (EditText) mRootView.findViewById(R.id.editTextBB);
        assert etBB != null;
        etBB.setOnEditorActionListener(this);
        etBB.setText(String.valueOf(mHand.getBB()));

        etAnte = (EditText) mRootView.findViewById(R.id.editTextAnte);
        assert etAnte != null;
        etAnte.setOnEditorActionListener(this);
        etAnte.setText(String.valueOf(mHand.getAnte()));

        etStack = (EditText) mRootView.findViewById(R.id.editTextStack);
        assert etStack != null;
        etStack.setOnEditorActionListener(this);
        etStack.setText(String.valueOf(mHand.getStack()));
        etStack.requestFocus();

        tvM = (TextView) mRootView.findViewById(R.id.textViewM);
        assert tvM != null;

        spinnerPlayer = (Spinner) mRootView.findViewById(R.id.spinnerPlayer);
        assert spinnerPlayer != null;
        spinnerPlayer.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterPlayer = ArrayAdapter.createFromResource(getActivity(),
                R.array.players_array, android.R.layout.simple_spinner_item);
        adapterPlayer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlayer.setAdapter(adapterPlayer);
        spinnerPlayer.setSelection(mHand.getPlayers() - 2);
        if (mHand.getPlayers() == 2)
            mCurrPlayer = 1;
        else
            mCurrPlayer = 2;

        spinnerPos = (Spinner) mRootView.findViewById(R.id.spinnerPos);
        assert spinnerPos != null;
        spinnerPos.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterPos = ArrayAdapter.createFromResource(getActivity(),
                compute_position_array(Integer.parseInt(spinnerPlayer.getSelectedItem().toString())),
                android.R.layout.simple_spinner_item);
        adapterPos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPos.setAdapter(adapterPos);
        spinnerPos.setSelection(mHand.getPosition());

        bInit = (Button) mRootView.findViewById(R.id.buttonInit);
        assert bInit != null;
        bInit.setOnClickListener(this);

        // Show keyboard
        //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
                .setIcon(R.drawable.ic_icon_suits)
                //.setTitle("Create hand")
                .setView(mRootView)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((HandActivity) getActivity()).doPositiveClick(mHand);
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        return dialog;
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------- E D I T   T E X T ----------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.editTextSB:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (mState == State.INIT) {
                        String strSB = v.getText().toString();
                        if (!strSB.isEmpty()) {
                            // Fill in BB = 2*SB
                            int bb = Integer.parseInt(strSB) * 2;
                            etBB.setText(String.valueOf(bb));

                            // Give focus to next EditText and keep keyboard up
                            etAnte.requestFocus();
                            return true;
                        }
                    }
                }
                break;

            case R.id.editTextBB:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (mState == State.INIT) {
                        String strBB = v.getText().toString();
                        if (!strBB.isEmpty()) {
                            // Fill in SB = BB/2
                            int sb = Integer.parseInt(strBB) / 2;
                            etSB.setText(String.valueOf(sb));

                            // Give focus to next EditText and keep keyboard up
                            etAnte.requestFocus();
                            return true;
                        }
                    }
                }
                break;

            case R.id.editTextAnte:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (mState == State.INIT) {
                        String strAnte = v.getText().toString();
                        if (!strAnte.isEmpty()) {
                            // Give focus to next EditText and keep keyboard up
                            etStack.setText("");
                            etStack.requestFocus();
                            return true;
                        }
                    }
                }
                break;

            case R.id.editTextStack:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mState == State.INIT) {
                        String strStack = v.getText().toString();
                        if (!strStack.isEmpty()) {
                            // DO NOT return true makes keyboard to disappear
                            //return true;
                        }
                    }
                }
                break;

            case R.id.editTextRaise:
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    if (mState == State.PREFLOP_ACTIONS) {
                        String strAmount = v.getText().toString();
                        if (!strAmount.isEmpty()) {

                            do_raise_action(When.PREFLOP, Integer.parseInt(strAmount));

                            if (check_end_condition()) {
                                // SAVE HAND
                                Log.d("E", "Edit amount, end hand solo");
                                prepare_for_showdown_solo();
                            } else if (check_keep_condition()) {
                                // NEXT ACTION
                                Log.d("E", "Edit amount, keep PREFLOP");
                                prepare_for_next_action();
                            } else {
                                // FLOP CARDS
                                Log.d("E", "Edit amount, goto FLOP");
                                prepare_for_flop_selection();
                            }
                            // DO NOT return true makes keyboard to disappear
                            //return true;
                        }
                    } else if (mState == State.FLOP_ACTIONS) {
                        String strAmount = v.getText().toString();
                        if (!strAmount.isEmpty()) {

                            do_raise_action(When.FLOP, Integer.parseInt(strAmount));

                            if (check_end_condition()) {
                                // SAVE HAND
                                Log.d("E", "Edit amount, end hand solo");
                                prepare_for_showdown_solo();
                            } else if (check_keep_condition()) {
                                // NEXT ACTION
                                Log.d("E", "Edit amount, keep FLOP");
                                prepare_for_next_action();
                            } else {
                                // TURN CARD
                                Log.d("E", "Edit amount, goto TURN");
                                prepare_for_turn_selection();
                            }
                            // DO NOT return true makes keyboard to disappear
                            //return true;
                        }
                    } else if (mState == State.TURN_ACTIONS) {
                        String strAmount = v.getText().toString();
                        if (!strAmount.isEmpty()) {

                            do_raise_action(When.TURN, Integer.parseInt(strAmount));

                            if (check_end_condition()) {
                                // SAVE HAND
                                Log.d("E", "Edit amount, end hand solo");
                                prepare_for_showdown_solo();
                            } else if (check_keep_condition()) {
                                // NEXT ACTION
                                Log.d("E", "Edit amount, keep TURN");
                                prepare_for_next_action();
                            } else {
                                // RIVER CARD
                                Log.d("E", "Edit amount, goto RIVER");
                                prepare_for_river_selection();
                            }
                            // DO NOT return true makes keyboard to disappear
                            //return true;
                        }
                    } else if (mState == State.RIVER_ACTIONS) {
                        String strAmount = v.getText().toString();
                        if (!strAmount.isEmpty()) {

                            do_raise_action(When.RIVER, Integer.parseInt(strAmount));

                            if (check_end_condition()) {
                                // SAVE HAND
                                Log.d("E", "Edit amount, end hand solo");
                                prepare_for_showdown_solo();
                            } else if (check_keep_condition()) {
                                // NEXT ACTION
                                Log.d("E", "Edit amount, keep RIVER");
                                prepare_for_next_action();
                            } else {
                                // SAVE HAND
                                Log.d("E", "Edit amount, end hand multi");
                                prepare_for_hand_selection();
                            }
                            // DO NOT return true makes keyboard to disappear
                            //return true;
                        }
                    }
                }
                break;
        }

        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ S P I N N E R ------------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        switch (parent.getId()) {
            case R.id.spinnerPlayer: // "# of players" spinner
                if (mState == State.INIT) {
                    // Init remaining players
                    mNbOfPlayersRemaining = Integer.parseInt(parent.getSelectedItem().toString());

                    // Save old selection
                    int old = spinnerPos.getSelectedItemPosition();

                    // Modify "Current position" spinner drop down list accordingly
                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<CharSequence> newAdapterPos = ArrayAdapter.createFromResource(
                            getActivity(),
                            compute_position_array(mNbOfPlayersRemaining), android.R.layout.simple_spinner_item);
                    // Specify the layout to use when the list of choices appears
                    newAdapterPos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // Apply the adapter to the spinner
                    spinnerPos.setAdapter(newAdapterPos);
                    spinnerPos.setSelection(old % mNbOfPlayersRemaining);
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------- B U T T O N -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonInit:
                if (mState == State.INIT && check_general_info()) {
                    // Hide keyboard (getCurrentFocus should return an EditText)
                    //InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    //inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    mHand.setSB(
                            (etSB.getText().toString().isEmpty()) ? 0 : Integer.parseInt(etSB.getText().toString())
                    );
                    mHand.setBB(
                            (etBB.getText().toString().isEmpty()) ? 0 : Integer.parseInt(etBB.getText().toString())
                    );
                    mHand.setAnte (
                            (etAnte.getText().toString().isEmpty()) ? 0 : Integer.parseInt(etAnte.getText().toString())
                    );
                    mHand.setStack(
                            (etStack.getText().toString().isEmpty()) ? 0 : Integer.parseInt(etStack.getText().toString())
                    );
                    mHand.setPlayers(Integer.parseInt(spinnerPlayer.getSelectedItem().toString()));
                    mHand.setPosition(spinnerPos.getSelectedItemPosition());

                    // Calculate M value
                    tvM.setText(String.format(Locale.FRENCH, "M=%.1f\r\n(%.1f BB)", compute_m(), compute_bb()));

                    // Prepare
                    prepare_for_preflop_actions();
                }
                break;

            case R.id.buttonFold:
                if (mState == State.PREFLOP_ACTIONS) {

                    do_fold_action(When.PREFLOP);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click FOLD, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click FOLD, keep PREFLOP");
                        prepare_for_next_action();
                    } else {
                        // FLOP CARDS
                        Log.d("B", "Click FOLD, goto FLOP");
                        prepare_for_flop_selection();
                    }
                } else if (mState == State.FLOP_ACTIONS) {

                    do_fold_action(When.FLOP);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click FOLD, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click FOLD, keep FLOP");
                        prepare_for_next_action();
                    } else {
                        // TURN CARD
                        Log.d("B", "Click FOLD, goto TURN");
                        prepare_for_turn_selection();
                    }
                } else if (mState == State.TURN_ACTIONS) {

                    do_fold_action(When.TURN);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click FOLD, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click FOLD, keep TURN");
                        prepare_for_next_action();
                    } else {
                        // RIVER CARD
                        Log.d("B", "Click FOLD, goto RIVER");
                        prepare_for_river_selection();
                    }
                } else if (mState == State.RIVER_ACTIONS) {

                    do_fold_action(When.RIVER);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click FOLD, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("E", "Click FOLD, keep RIVER");
                        prepare_for_next_action();
                    } else {
                        // SAVE HAND
                        Log.d("E", "Edit amount, end hand multi");
                        prepare_for_hand_selection();
                    }
                }
                break;

            case R.id.buttonCall:
                if (mState == State.PREFLOP_ACTIONS) {

                    do_call_action(When.PREFLOP);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click CALL, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click CALL, keep PREFLOP");
                        prepare_for_next_action();
                    } else {
                        // FLOP CARDS
                        Log.d("B", "Click CALL, goto FLOP");
                        prepare_for_flop_selection();
                    }
                }
                if (mState == State.FLOP_ACTIONS) {

                    do_call_action(When.FLOP);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click CALL, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click CALL, keep FLOP");
                        prepare_for_next_action();
                    } else {
                        // TURN CARD
                        Log.d("B", "Click CALL, goto TURN");
                        prepare_for_turn_selection();
                    }
                } else if (mState == State.TURN_ACTIONS) {

                    do_call_action(When.TURN);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click CALL, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click CALL, keep TURN");
                        prepare_for_next_action();
                    } else {
                        // RIVER CARD
                        Log.d("B", "Click CALL, goto RIVER");
                        prepare_for_river_selection();
                    }
                } else if (mState == State.RIVER_ACTIONS) {

                    do_call_action(When.RIVER);

                    if (check_end_condition()) {
                        // SAVE HAND
                        Log.d("B", "Click CALL, end hand solo");
                        prepare_for_showdown_solo();
                    } else if (check_keep_condition()) {
                        // NEXT ACTION
                        Log.d("B", "Click CALL, keep RIVER");
                        prepare_for_next_action();
                    } else {
                        // SAVE HAND
                        Log.d("B", "Click CALL, end hand multi");
                        prepare_for_hand_selection();
                    }
                }
                break;

            case R.id.buttonRaise:
                if (mState == State.PREFLOP_ACTIONS
                        || mState == State.FLOP_ACTIONS
                        || mState == State.TURN_ACTIONS
                        || mState == State.RIVER_ACTIONS) {
                    // Give focus to next EditText
                    etRaise.requestFocus();
                    // Show keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    Log.d("B", "Click RAISE, edit amount");
                }
                break;

            case R.id.buttonCard:
                if (mState == State.FLOP_1ST_CARD
                        || mState == State.FLOP_2ND_CARD
                        || mState == State.FLOP_3RD_CARD
                        || mState == State.TURN_CARD
                        || mState == State.RIVER_CARD
                        || mState == State.SHOWDOWN_HAND_1ST_CARD
                        || mState == State.SHOWDOWN_HAND_2ND_CARD) {
                    ChooseCardDialogFragment dialogFragment = new ChooseCardDialogFragment();
                    dialogFragment.setOnDialogCardClickListener(this);
                    Bundle args = new Bundle();
                    ArrayList<Integer> list = new ArrayList<>();
                    list.addAll(mBoardCards);
                    for (int i = 0; i < mListOfPlayers.size(); i++) {
                        list.addAll(mListOfPlayers.get(i).mHandCards);
                    }
                    args.putIntegerArrayList("HIDDEN", list);
                    dialogFragment.setArguments(args);
                    dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog_cards");
                }
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------- C A R D   S E L E C T I O N ------------------------------------
    // ---------------------------------------------------------------------------------------------

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public boolean onDialogCardClick(android.support.v4.app.DialogFragment dialog, Integer position, CharSequence str, Integer drawable) {
        // User clicked a card button
        switch (mState) {
            case FLOP_1ST_CARD:
                // Save card
                mBoardCards.add(position);
                // Display card
                ivCard1.setImageResource(drawable);
                ivCard1.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard1.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Change state
                mState = State.FLOP_2ND_CARD;
                return true;
            case FLOP_2ND_CARD:
                // Save card
                mBoardCards.add(position);
                // Display card
                ivCard2.setImageResource(drawable);
                ivCard2.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard2.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Change state
                mState = State.FLOP_3RD_CARD;
                return true;
            case FLOP_3RD_CARD:
                // Save card
                mBoardCards.add(position);
                // Display card
                ivCard3.setImageResource(drawable);
                ivCard3.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard3.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Prepare
                prepare_for_flop_actions();
                return false;
            case TURN_CARD:
                // Save card
                mBoardCards.add(position);
                // Display card
                ivCard1.setImageResource(drawable);
                ivCard1.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard1.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Prepare
                prepare_for_turn_actions();
                return false;
            case RIVER_CARD:
                // Save card
                mBoardCards.add(position);
                // Display card
                ivCard1.setImageResource(drawable);
                ivCard1.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard1.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Prepare
                prepare_for_river_actions();
                return false;
            case SHOWDOWN_HAND_1ST_CARD:
                // Save card
                mListOfPlayers.get(mCurrPlayer).mHandCards.add(position);
                // Display card
                ivCard1.setImageResource(drawable);
                ivCard1.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard1.getLayoutParams().height = MY_IMAGE_HEIGHT;
                // Change state
                mState = State.SHOWDOWN_HAND_2ND_CARD;
                return true;
            case SHOWDOWN_HAND_2ND_CARD:
                // Save card
                mListOfPlayers.get(mCurrPlayer).mHandCards.add(position);
                // Display card
                ivCard2.setImageResource(drawable);
                ivCard2.getLayoutParams().width = MY_IMAGE_WIDTH;
                ivCard2.getLayoutParams().height = MY_IMAGE_HEIGHT;
                prepare_for_next_hand_selection();
                return false;
        }
        // dismiss dialog
        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------- T O O L S ------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    private float compute_m() {
        float stack = Float.parseFloat(etStack.getText().toString());
        int sb = Integer.parseInt(etSB.getText().toString());
        int bb = Integer.parseInt(etBB.getText().toString());
        int ante = Integer.parseInt(etAnte.getText().toString());
        int players = spinnerPlayer.getSelectedItemPosition() + 2;
        return (stack / (sb + bb + ante * players));
    }

    private float compute_bb() {
        float stack = Float.parseFloat(etStack.getText().toString());
        int bb = Integer.parseInt(etBB.getText().toString());
        return (stack / bb);
    }

    private int compute_position_array(int players) {
        switch (players) {
            case 2:
                return R.array._hu_positions_array;
            case 3:
                return R.array._3max_positions_array;
            case 4:
                return R.array._4max_positions_array;
            case 5:
                return R.array._5max_positions_array;
            case 6:
                return R.array._6max_positions_array;
            case 7:
                return R.array._7max_positions_array;
            case 8:
                return R.array._8max_positions_array;
            case 9:
                return R.array._9max_positions_array;
            case 10:
                return R.array._10max_positions_array;
        }
        return R.array._8max_positions_array; // default
    }

    private boolean check_general_info() {
        float stack = Float.parseFloat(etStack.getText().toString());
        int sb = Integer.parseInt(etSB.getText().toString());
        int bb = Integer.parseInt(etBB.getText().toString());
        return ((sb > 0) && (bb > 0) && (stack > 0));
    }

    private boolean check_end_condition() {
        return (mNbOfPlayersRemaining == 1);
    }

    private boolean check_keep_condition() { return (mNbOfCallsAfterRaise < mNbOfPlayersRemaining); }

    private String compute_prepreflop_text() {
        String txt = "";

        int sb = Integer.parseInt(etSB.getText().toString());
        int bb = Integer.parseInt(etBB.getText().toString());
        int ante = Integer.parseInt(etAnte.getText().toString());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.FRENCH);

        txt = txt.concat(String.format(Locale.FRENCH,
                "Winamax Poker - Tournament \"%s\" buyIn: %s level: %s - HandId: #%s - Holdem no limit (%d/%d/%d) - %s UTC\n" +
                "Table: '%s' %d-max (real money) Seat #%d is the button\n",
                "unknown",                      // Tournament
                "unknown",                      // BuyIn
                "unknown",                      // Level
                "unknown",                      // HandId
                ante,                           // Ante
                sb,                             // SB
                bb,                             // BB
                df.format(calendar.getTime()),  // "2014/05/11 18:14:54 UTC",
                "unknown",                      // Table
                mListOfPlayers.size(),          // Players
                mListOfPlayers.size()           // Button
        ));

        for (int i = 0; i < mListOfPlayers.size(); i++) {
            txt = txt.concat(String.format(Locale.FRENCH, "Seat %d: %s (%s)\n",
                    mListOfPlayers.get(i).mId+1,
                    mListOfPlayers.get(i).getSeat(),
                    mListOfPlayers.get(i).getStack()));
        }

        txt = txt.concat("*** ANTE/BLINDS ***\n");
        if (ante > 0) {
            for (int i = 0; i < mListOfPlayers.size(); i++) {
                txt = txt.concat(String.format(Locale.FRENCH, "%s posts ante %d\n", mListOfPlayers.get(i).getSeat(), ante));
                //p.mChipsPutInPot = ante;
                mPotSize += ante;
            }
        }

        txt = txt.concat(String.format(Locale.FRENCH, "%s posts small blind %d\n", mListOfPlayers.get(0).getSeat(), sb));
        mListOfPlayers.get(0).mChipsPutInPot += sb;
        mPotSize += sb;

        txt = txt.concat(String.format(Locale.FRENCH, "%s posts big blind %d\n", mListOfPlayers.get(1).getSeat(), bb));
        mListOfPlayers.get(1).mChipsPutInPot += bb;
        mPotSize += bb;

        mLastBet = bb;
        txt = txt.concat(String.format(Locale.FRENCH, "Dealt to %s [x x]\n", mListOfPlayers.get(spinnerPos.getSelectedItemPosition()).getSeat()));

        return txt;
    }

    private void scroll_down() {
        mScroller.post(new Runnable() {
            @Override
            public void run() {
                mScroller.smoothScrollTo(0, mScroller.getChildAt(0).getBottom());
            }
        });
    }

    private void prepare_for_preflop_actions() {

        etSB.setEnabled(false);
        etBB.setEnabled(false);
        etAnte.setEnabled(false);
        etStack.setEnabled(false);
        spinnerPlayer.setEnabled(false);
        spinnerPos.setEnabled(false);

        int players = spinnerPlayer.getSelectedItemPosition()+2;
        String seats[] = getResources().getStringArray(compute_position_array(players));
        for (int i=0; i < players; i++) {
            mListOfPlayers.add(new Player(i, seats[i]));
        }

        mHand.appendSummary(compute_prepreflop_text());

        // Enable preflop action selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubPreflop)).inflate();
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_preflop);
        tvActionLabel.setText(R.string.preflop_action);
        tvActionLabel.append(String.format(Locale.FRENCH, "\t(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));
        mHand.appendSummary("*** PRE-FLOP ***\n");

        // Prepare RadioGroup
        prepare_for_next_action();

        // Change state
        mState = State.PREFLOP_ACTIONS;
    }

    private void prepare_for_next_action() {

        // Clear RadioGroup
        rgActionSelection.requestFocus();

        // Fill player name TextView
        tvSeat.setText(String.format(Locale.FRENCH, "%s ?", mListOfPlayers.get(mCurrPlayer).getSeat(true)));

        // Adapt RadioButton labels
        if (mListOfPlayers.get(mCurrPlayer).mChipsPutInPot == mLastBet) {
            rbFold.setEnabled(false);
            rbCall.setText(R.string.check);
            rbRaise.setText(R.string.bet);
        } else {
            rbFold.setEnabled(true);
            rbCall.setText(R.string.call);
            rbRaise.setText(R.string.raise);
        }

        // Clear raise action EditTExt
        etRaise.setText("");
    }

    private void do_fold_action(When when) {

        // Save action
        ActionItem item = new ActionItem(when, mCurrPlayer, Action.FOLD);
        mListOfPlayers.get(mCurrPlayer).mListOfActions.add(item);

        // Write into TextView
        tvAction.append(String.format(Locale.FRENCH, "%s folds\n", mListOfPlayers.get(mCurrPlayer).getSeat()));
        //tvAction.append(String.format(Locale.FRENCH, "\t\t\t\t\t(pot=%d)\n", mPotSize));

        // Update parameters
        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(mCurrPlayer + 1).mId;
        mNbOfPlayersRemaining--;
    }

    private void do_call_action(When when) {

        // Save action
        ActionItem item = new ActionItem(when, mCurrPlayer, Action.CALL);
        mListOfPlayers.get(mCurrPlayer).mListOfActions.add(item);

        // Update pot size
        int diff = mLastBet - mListOfPlayers.get(mCurrPlayer).mChipsPutInPot;
        mPotSize += diff;

        // Write into TextView
        if (diff == 0) {
            tvAction.append(String.format(Locale.FRENCH, "%s checks\n", mListOfPlayers.get(mCurrPlayer).getSeat()));
        } else {
            tvAction.append(String.format(Locale.FRENCH, "%s calls %d\n", mListOfPlayers.get(mCurrPlayer).getSeat(), diff));
        }
        //tvAction.append(String.format(Locale.FRENCH, "\t\t\t\t\t(pot=%d)\n", mPotSize));

        // Update parameters
        mListOfPlayers.get(mCurrPlayer).mChipsPutInPot = mLastBet;
        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(mCurrPlayer + 1).mId;
        mNbOfCallsAfterRaise++;
    }

    private void do_raise_action(When when, int amount) {

        // Save action
        ActionItem item = new ActionItem(when, mCurrPlayer, Action.RAISE, amount);
        mListOfPlayers.get(mCurrPlayer).mListOfActions.add(item);

        // Update pot size
        int diff = amount - mListOfPlayers.get(mCurrPlayer).mChipsPutInPot;
        mPotSize += diff;

        // Write into TextView
        if (mLastBet == 0) {
            tvAction.append(String.format(Locale.FRENCH,"%s bets %d\n", mListOfPlayers.get(mCurrPlayer).getSeat(), amount));
        } else {
            tvAction.append(String.format(Locale.FRENCH,"%s raises %d to %d\n", mListOfPlayers.get(mCurrPlayer).getSeat(), diff, amount));
        }
        //tvAction.append(String.format(Locale.FRENCH, "\b\t\t(pot=%d)\n", mPotSize));

        // Update parameters
        mListOfPlayers.get(mCurrPlayer).mChipsPutInPot += diff;
        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(mCurrPlayer + 1).mId;
        mNbOfCallsAfterRaise = 1;
        mLastBet = amount;
    }

    private void prepare_for_flop_selection() {

        mHand.appendSummary(tvAction.getText().toString());

        // Disable preflop action selection
        rgActionSelection.setVisibility(View.GONE);

        // Enable flop cards selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubFlopSel)).inflate();
        inflate_card_selection_stub(v);
        v.setBackgroundResource(R.drawable.bg_preflop);
        tvTitle.setText(R.string.flop);

        // Select Flop cards
        mState = State.FLOP_1ST_CARD;
    }

    private void prepare_for_flop_actions() {

        // Disable card selection
        bCard.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(0).mId;
        for (int i = 0; i < mListOfPlayers.size(); i++) {
            mListOfPlayers.get(i).mChipsPutInPot = 0;
        }
        mNbOfCallsAfterRaise = 0;
        mLastBet = 0;

        // Enable action selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubFlop)).inflate();
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_flop);
        tvActionLabel.setText(R.string.flop_action);
        tvActionLabel.append(String.format(Locale.FRENCH, "\t(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));
        mHand.appendSummary(String.format(Locale.FRENCH, "*** FLOP *** [%s %s %s]\n",
                ImageAdapter.convertHand(mBoardCards.get(0)),
                ImageAdapter.convertHand(mBoardCards.get(1)),
                ImageAdapter.convertHand(mBoardCards.get(2))));

        // Prepare RadioGroup
        prepare_for_next_action();

        // Change state
        mState = State.FLOP_ACTIONS;
    }

    private void prepare_for_turn_selection() {

        mHand.appendSummary(tvAction.getText().toString());

        // Disable flop action selection
        rgActionSelection.setVisibility(View.GONE);

        // Enable turn cards selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubTurnSel)).inflate();
        inflate_card_selection_stub(v);
        v.setBackgroundResource(R.drawable.bg_flop);
        tvTitle.setText(R.string.turn);

        // Change state
        mState = State.TURN_CARD;
    }

    private void prepare_for_turn_actions() {

        // Disable card selection
        bCard.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(0).mId;
        for (int i = 0; i < spinnerPlayer.getSelectedItemPosition() + 2; i++) {
            mListOfPlayers.get(i).mChipsPutInPot = 0;
        }
        mNbOfCallsAfterRaise = 0;
        mLastBet = 0;

        // Enable action selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubTurn)).inflate();
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_turn);
        tvActionLabel.setText(R.string.turn_action);
        tvActionLabel.append(String.format(Locale.FRENCH, "\t(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));
        mHand.appendSummary(String.format(Locale.FRENCH, "*** TURN *** [%s %s %s][%s]\n",
                ImageAdapter.convertHand(mBoardCards.get(0)),
                ImageAdapter.convertHand(mBoardCards.get(1)),
                ImageAdapter.convertHand(mBoardCards.get(2)),
                ImageAdapter.convertHand(mBoardCards.get(3))));

        // Prepare RadioGroup
        prepare_for_next_action();

        // Change state
        mState = State.TURN_ACTIONS;
    }

    private void prepare_for_river_selection() {

        mHand.appendSummary(tvAction.getText().toString());

        // Disable flop action selection
        rgActionSelection.setVisibility(View.GONE);

        // Enable river cards selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubRiverSel)).inflate();
        inflate_card_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_turn);
        tvTitle.setText(R.string.river);

        // Change state
        mState = State.RIVER_CARD;
    }

    private void prepare_for_river_actions() {

        // Disable card selection
        bCard.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(0).mId;
        for (int i = 0; i < spinnerPlayer.getSelectedItemPosition() + 2; i++) {
            mListOfPlayers.get(i).mChipsPutInPot = 0;
        }
        mNbOfCallsAfterRaise = 0;
        mLastBet = 0;

        // Enable action selection
        View v = ((ViewStub) mRootView.findViewById(R.id.viewStubRiver)).inflate();
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_river);
        tvActionLabel.setText(R.string.river_action);
        tvActionLabel.append(String.format(Locale.FRENCH, "\t(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));
        mHand.appendSummary(String.format(Locale.FRENCH, "*** RIVER *** [%s %s %s ][%s][%s]\n",
                ImageAdapter.convertHand(mBoardCards.get(0)),
                ImageAdapter.convertHand(mBoardCards.get(1)),
                ImageAdapter.convertHand(mBoardCards.get(2)),
                ImageAdapter.convertHand(mBoardCards.get(3)),
                ImageAdapter.convertHand(mBoardCards.get(4))));

        // Prepare RadioGroup
        prepare_for_next_action();

        // Change state
        mState = State.RIVER_ACTIONS;
    }

    private void prepare_for_hand_selection() {

        mHand.appendSummary(tvAction.getText().toString());

        // Disable preflop action selection
        rgActionSelection.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(0).mId;

        // Enable card selection
        LinearLayout item = (LinearLayout) mRootView.findViewById(R.id.LinearLayoutAll);
        View v = getActivity().getLayoutInflater().inflate(R.layout.card_selection, item, false);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(lp);
        item.addView(v);
        inflate_card_selection_stub(v);
        v.setBackgroundResource(R.drawable.bg_river);

        // Write into TextView
        tvTitle.setText(String.format(Locale.FRENCH,"Dealt to %s: ", mListOfPlayers.get(mCurrPlayer).getSeat()));

        // Select hand cards
        mState = State.SHOWDOWN_HAND_1ST_CARD;
    }

    private void prepare_for_next_hand_selection() {

        // Disable card selection
        bCard.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(mCurrPlayer + 1).mId;

        Player p = mListOfPlayers.get(mCurrPlayer);
        Log.d("NEXT_HAND", String.format(Locale.FRENCH, "Current player is %s(%d)", p.getSeat(), mCurrPlayer));
        if (p.mHandCards.isEmpty()) {
            Log.d("NEXT_HAND", String.format(Locale.FRENCH, "New card selection"));

            // Enable new card selection
            LinearLayout item = (LinearLayout) mRootView.findViewById(R.id.LinearLayoutAll);
            View v = getActivity().getLayoutInflater().inflate(R.layout.card_selection, item, false);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            v.setLayoutParams(lp);
            item.addView(v);
            inflate_card_selection_stub(v);
            v.setBackgroundResource(R.drawable.bg_river);

            // Write into TextView
            tvTitle.setText(String.format(Locale.FRENCH,"Dealt to %s: ", mListOfPlayers.get(mCurrPlayer).getSeat()));

            // Change state
            mState = State.SHOWDOWN_HAND_1ST_CARD;
        } else {
            Log.d("NEXT_HAND", String.format(Locale.FRENCH, "Set winner player"));

            LinearLayout item = (LinearLayout) mRootView.findViewById(R.id.LinearLayoutAll);
            final LinearLayout layout = new LinearLayout(getContext());
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(lp);

            TextView tv = new TextView(getContext());
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setText("Who won ?");
            lp.weight = (float) 1.02;
            lp.setMargins(4, 4, 4, 4);
            tv.setLayoutParams(lp);
            layout.addView(tv);

            for (int i=0; i<mListOfPlayers.size(); i++) {
                p = mListOfPlayers.get(i);
                Log.d("NEXT_HAND", String.format(Locale.FRENCH, "Is player %s in game ?", p.getSeat()));
                if (! p.mHandCards.isEmpty()) {
                    Button b = new Button(getContext());
                    lp.weight = 1;
                    lp.setMargins(2, 2, 2, 2);
                    tv.setLayoutParams(lp);
                    b.setText(p.getSeat());
                    b.setTag(p);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.isPressed()) {
                                // Determine who won !
                                mWinPlayer = ((Player) v.getTag()).mId;
                                Log.d("WIN", String.format(Locale.FRENCH, "Winner player is %d", mWinPlayer));

                                layout.setVisibility(View.GONE);

                                prepare_for_showdown_multi();
                            }
                        }
                    });
                    layout.addView(b);
                }
            }

            item.addView(layout);
            layout.setBackgroundResource(R.drawable.bg_river);
            scroll_down();

            // Change state
            mState = State.WINNER;
        }
    }

    private void prepare_for_showdown_solo() {

        mHand.appendSummary(tvAction.getText().toString());

        // Disable preflop action selection
        rgActionSelection.setVisibility(View.GONE);

        mCurrPlayer = mListOfPlayers.getNextPlayerFrom(0).mId;

        // Enable action selection
        LinearLayout item = (LinearLayout) mRootView.findViewById(R.id.LinearLayoutAll);
        View v = getActivity().getLayoutInflater().inflate(R.layout.action_selection, item, false);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(lp);
        item.addView(v);
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_showdown);
        tvActionLabel.setText(R.string.showdown);
        tvActionLabel.append(String.format(Locale.FRENCH, "\t(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));

        mHand.appendSummary("*** SHOW DOWN ***\n");

        // Write into TextView
        tvAction.append(String.format(Locale.FRENCH, "%s collected %d from pot\n", mListOfPlayers.get(mCurrPlayer).getSeat(), mPotSize));

        rgActionSelection.setVisibility(View.GONE);

        // Change state
        mState = State.SHOWDOWN;

        mHand.appendSummary(tvAction.getText().toString());

        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    private void prepare_for_showdown_multi() {

        // Enable action selection
        LinearLayout item = (LinearLayout) mRootView.findViewById(R.id.LinearLayoutAll);
        View v = getActivity().getLayoutInflater().inflate(R.layout.action_selection, item, false);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(lp);
        item.addView(v);
        inflate_action_selection_stub(v);

        v.setBackgroundResource(R.drawable.bg_showdown);
        tvActionLabel.setText(R.string.showdown);
        tvActionLabel.append(String.format(Locale.FRENCH, "(%d players, pot is %d)", mNbOfPlayersRemaining, mPotSize));

        mHand.appendSummary("*** SHOW DOWN ***\n");

        // Write into TextView
        for (int i=0; i<mListOfPlayers.size(); i++) {
            if (! mListOfPlayers.get(i).mHandCards.isEmpty()) {
                tvAction.append(String.format(Locale.FRENCH, "%s shows [%s]\n", mListOfPlayers.get(i).getSeat(), mListOfPlayers.get(i).getHand()));
            }
        }
        tvAction.append(String.format(Locale.FRENCH, "%s collected %d from pot\n", mListOfPlayers.get(mWinPlayer).getSeat(), mPotSize));

        rgActionSelection.setVisibility(View.GONE);

        // Change state
        mState = State.SHOWDOWN;

        mHand.appendSummary(tvAction.getText().toString());

        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    private void inflate_card_selection_stub(View v) {

        tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
        assert tvTitle != null;

        ivCard1 = (ImageView) v.findViewById(R.id.imageViewCard1);
        assert ivCard1!=null;

        ivCard2 = (ImageView) v.findViewById(R.id.imageViewCard2);
        assert ivCard2!=null;

        ivCard3 = (ImageView) v.findViewById(R.id.imageViewCard3);
        assert ivCard3!=null;

        bCard = (Button) v.findViewById(R.id.buttonCard);
        assert bCard!=null;
        bCard.setOnClickListener(this);

        scroll_down();
    }

    private void inflate_action_selection_stub(View v) {

        tvActionLabel = (TextView) v.findViewById(R.id.textViewActionLabel);
        assert tvAction != null;

        tvAction = (TextView) v.findViewById(R.id.textViewAction);
        assert tvAction != null;

        rgActionSelection = (LinearLayout) ((ViewStub) v.findViewById(R.id.viewStubActionSelection)).inflate();
        assert rgActionSelection != null;

        tvSeat = (TextView) rgActionSelection.findViewById(R.id.textViewSeat);
        assert tvSeat != null;

        rbFold = (Button) rgActionSelection.findViewById(R.id.buttonFold);
        assert rbFold != null;
        rbFold.setOnClickListener(this);

        rbCall = (Button) rgActionSelection.findViewById(R.id.buttonCall);
        assert rbCall != null;
        rbCall.setOnClickListener(this);

        rbRaise = (Button) rgActionSelection.findViewById(R.id.buttonRaise);
        assert rbRaise != null;
        rbRaise.setOnClickListener(this);

        etRaise = (EditText) rgActionSelection.findViewById(R.id.editTextRaise);
        assert etRaise != null;
        etRaise.setOnEditorActionListener(this);

        scroll_down();
    }

}

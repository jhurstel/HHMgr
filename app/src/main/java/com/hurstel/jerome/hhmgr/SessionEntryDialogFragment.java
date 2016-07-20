package com.hurstel.jerome.hhmgr;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SessionEntryDialogFragment extends DialogFragment implements TextView.OnEditorActionListener, View.OnClickListener {

    EditText wgName, wgSB, wgBB, wgAnte, wgStack;
    CheckBox wgSelectAnte;
    Spinner wgType, wgPlayers;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.session_entry, null);
        assert v != null;

        ArrayAdapter<CharSequence> adapter;

        wgName = (EditText) v.findViewById(R.id.editTextName);
        assert wgName != null;
        wgName.requestFocus();

        wgType = (Spinner) v.findViewById(R.id.spinnerType);
        assert wgType != null;
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.session_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wgType.setAdapter(adapter);

        wgSB = (EditText) v.findViewById(R.id.editTextSB);
        assert wgSB != null;
        wgSB.setOnEditorActionListener(this);

        wgBB = (EditText) v.findViewById(R.id.editTextBB);
        assert wgBB != null;
        wgBB.setOnEditorActionListener(this);

        wgSelectAnte = (CheckBox) v.findViewById(R.id.checkBoxAnte);
        assert wgSelectAnte != null;
        wgSelectAnte.setOnClickListener(this);

        wgAnte = (EditText) v.findViewById(R.id.editTextAnte);
        assert wgAnte != null;
        wgAnte.setOnEditorActionListener(this);

        wgStack = (EditText) v.findViewById(R.id.editTextStack);
        assert wgStack != null;
        wgStack.setOnEditorActionListener(this);

        wgPlayers = (Spinner) v.findViewById(R.id.spinnerPlayer);
        assert wgPlayers != null;
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.players_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wgPlayers.setAdapter(adapter);

        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_icon_suits)
                .setTitle("Create session")
                .setView(v)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Session s = new Session(
                                        wgName.getText().toString(),
                                        new SimpleDateFormat("yyyy/MM/dd", Locale.FRENCH).format(Calendar.getInstance().getTime()),
                                        wgType.getSelectedItem().toString(),
                                        wgSB.getText().toString().isEmpty() ? 0 : Integer.parseInt(wgSB.getText().toString()),
                                        wgBB.getText().toString().isEmpty() ? 0 : Integer.parseInt(wgBB.getText().toString()),
                                        wgAnte.getText().toString().isEmpty() ? 0 : Integer.parseInt(wgAnte.getText().toString()),
                                        wgStack.getText().toString().isEmpty() ? 0 : Integer.parseInt(wgStack.getText().toString()),
                                        Integer.parseInt(wgPlayers.getSelectedItem().toString())
                                );
                                ((SessionActivity)getActivity()).doPositiveClick(s);
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
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String text;
        switch (v.getId()) {
            case R.id.editTextSB:
                text = wgSB.getText().toString();
                if (text.isEmpty()) {
                    return true;
                } else {
                    int bb = Integer.parseInt(text) * 2;
                    wgBB.setText(String.valueOf(bb));
                    return ! wgSelectAnte.isEnabled();
                }

            case R.id.editTextBB:
                text = wgBB.getText().toString();
                if (text.isEmpty()) {
                    return true;
                } else {
                    int sb = Integer.parseInt(text) / 2;
                    wgSB.setText(String.valueOf(sb));
                    return ! wgSelectAnte.isEnabled();
                }

            case R.id.editTextAnte:
                text = wgAnte.getText().toString();
                return text.isEmpty();

            case R.id.editTextStack:
                text = wgStack.getText().toString();
                return text.isEmpty();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkBoxAnte:
                if (wgSelectAnte.isChecked()) {
                    wgAnte.setEnabled(true);
                    wgAnte.requestFocus();
                    // Show keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else {
                    wgAnte.setText("");
                    wgAnte.setEnabled(false);
                }
                break;
        }
    }

}

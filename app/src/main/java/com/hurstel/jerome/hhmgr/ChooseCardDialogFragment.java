package com.hurstel.jerome.hhmgr;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ChooseCardDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    // Use this instance of the interface to deliver action events
    ChooseCardDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        // Inflate the GridView and set listener
        GridView gridview = (GridView) getActivity().getLayoutInflater().inflate(R.layout.card_grid, null);
        gridview.setAdapter(new ImageAdapter(getContext(), args.getIntegerArrayList("HIDDEN")));
        gridview.setOnItemClickListener(this);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_card)
                .setIcon(R.drawable.ic_icon_suits)
                .setView(gridview);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setOnDialogCardClickListener(ChooseCardDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d("ChooseCard", String.format("position=%d => %s", position, parent.getAdapter().getItem(position)));
        ImageAdapter a = (ImageAdapter) parent.getAdapter();
        if (! a.getList().contains(position)) {
            a.getList().add(position);
            ((ImageView) v).setImageResource(R.drawable.back);
            v.setClickable(false);
            if (! mListener.onDialogCardClick((DialogFragment) getParentFragment(), position, (CharSequence) a.getItem(position), (int) a.getItemId(position))) {
                dismiss();
            }
        }
    }

}

package com.hurstel.jerome.hhmgr;

import android.support.v4.app.DialogFragment;

public interface ChooseCardDialogListener {
    boolean onDialogCardClick(DialogFragment dialog, Integer position, CharSequence str, Integer drawable);
}

package com.app.dlike.widgets;

import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;

/**
 * Created by moses on 8/25/18.
 */

public class BaseDialogFragment extends DialogFragment {
    @Override
    public void onStart() {
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        super.onStart();
    }
}

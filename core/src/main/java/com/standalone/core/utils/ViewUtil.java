package com.standalone.core.utils;

import android.view.ViewParent;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class ViewUtil {
    public static String getText(EditText edt) {
        return edt.getText().toString().trim();
    }

    public static TextInputLayout findTextInputLayout(EditText edt) {
        ViewParent parent = edt.getParent().getParent();
        return (parent instanceof TextInputLayout) ? (TextInputLayout) parent : null;
    }
}

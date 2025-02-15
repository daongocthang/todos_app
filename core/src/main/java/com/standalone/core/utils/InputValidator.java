package com.standalone.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewParent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.standalone.core.App;
import com.standalone.core.R;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class InputValidator {
    @SuppressLint("StaticFieldLeak")
    static InputValidator instance;

    final String EMAIL_PATTERN = "[a-z0-9A-Z._-]+@[a-z]+\\.[a-z]+";
    final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=\\S+$).{6,20}$";

    final Context context;

    EditText editText;
    TextInputLayout layout;

    InputValidator() {
        this.context = App.getContext();
    }

    public static InputValidator getInstance() {
        if (instance == null) instance = new InputValidator();
        return instance;
    }

    public InputValidator validate(EditText edt) {
        this.editText = edt;
        this.layout = getParent();

        if (layout == null) throw new AssertionError("TextInputLayout must not be null");
        return this;
    }


    public InputValidator notEmpty() {
        boolean empty = isEmpty(getText());
        notifyError(empty ? context.getString(R.string.validation_error_not_empty) : null);

        return this;
    }

    public InputValidator email() {
        boolean valid = getText().matches(EMAIL_PATTERN);
        notifyError(valid ? null : context.getString(R.string.validation_error_email));

        return this;
    }

    public InputValidator password() {
        boolean valid = getText().matches(PASSWORD_PATTERN);
        notifyError(valid ? null : context.getString(R.string.validation_error_password));

        return this;
    }

    public InputValidator confirmPassword(String password) {
        boolean valid = getText().equals(password);
        notifyError(valid ? null : context.getString(R.string.validation_error_confirm_password));

        return this;
    }

    public InputValidator url() {
        try {
            new URL(getText()).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            notifyError(context.getString(R.string.validation_error_url));
        }

        return this;
    }

    public void setError(@NonNull CharSequence errorText) {
        layout.setError(errorText);
        throw new ValidationError(errorText.toString());
    }

    public TextInputLayout getParent() {
        ViewParent parent = editText.getParent().getParent();
        return (parent instanceof TextInputLayout) ? (TextInputLayout) parent : null;
    }

    String getText() {
        return editText.getText().toString().trim();
    }

    void notifyError(@Nullable CharSequence message) {
        if (message != null) {
            layout.setError(message);
            throw new ValidationError(message.toString());
        } else if (!isEmpty(layout.getError())) {
            layout.setError(null);
        }
    }

    static boolean isEmpty(CharSequence c) {
        return TextUtils.isEmpty(c);
    }

    public static class ValidationError extends RuntimeException {
        public ValidationError() {
        }

        public ValidationError(String message) {
            super(message);
        }
    }

}
package com.standalone.core.utils;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;


import com.standalone.core.R;

import java.util.concurrent.Executor;

public class BiometricPromptUtil {
    static final String TAG = BiometricPromptUtil.class.getSimpleName();


    public static BiometricPrompt createBiometricPrompt(AppCompatActivity activity, AuthenticationProcessor processor) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                Log.e(TAG, errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication was successful.");

                processor.onSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                Log.w(TAG, "User biometric rejected.");
            }
        };

        return new BiometricPrompt(activity, executor, callback);

    }

    public static BiometricPrompt.PromptInfo createPromptInfo(AppCompatActivity activity) {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.prompt_info_title))
                .setDescription(activity.getString(R.string.prompt_info_description))
                .setConfirmationRequired(false)
                .setNegativeButtonText(activity.getString(R.string.prompt_info_cancel))
                .build();
    }

    public interface AuthenticationProcessor {
        void onSuccess(BiometricPrompt.AuthenticationResult result);
    }
}

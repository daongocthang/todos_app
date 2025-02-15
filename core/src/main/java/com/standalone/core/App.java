package com.standalone.core;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static Properties loadEnv() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = context.getAssets().open("env.properties");

            properties.load(inputStream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

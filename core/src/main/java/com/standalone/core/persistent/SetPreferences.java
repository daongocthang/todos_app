package com.standalone.core.persistent;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SetPreferences {
    private final SharedPreferences preferences;

    public SetPreferences(Context context, String prefsName) {
        preferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    public Set<String> get(String s) {
        return preferences.getStringSet(s, new HashSet<>());
    }

    public void put(String s, Set<String> values) {
        preferences.edit().putStringSet(s, values).apply();
    }

    public void remove(String s) {
        preferences.edit().remove(s).apply();
    }

    public boolean contains(String s) {
        return preferences.contains(s);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}

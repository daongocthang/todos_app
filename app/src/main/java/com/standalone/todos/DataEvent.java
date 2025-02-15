package com.standalone.todos;

import com.standalone.core.App;
import com.standalone.core.persistent.SetPreferences;

import java.util.Set;

public class DataEvent {
    private static final String PREFS_NAME = "event_prefs";
    public static final String CREATED = "event_created";
    public static final String DELETED = "event_deleted";

    private final SetPreferences prefs;

    public DataEvent() {
        this.prefs = new SetPreferences(App.getContext(), PREFS_NAME);
    }

    public Set<String> getAllByTag(String s) {
        return prefs.get(s);
    }

    public void put(String tag, String value) {
        Set<String> dataSet=prefs.get(tag);
        dataSet.add(value);
        prefs.put(tag,dataSet);
    }

    public void dispose() {
        prefs.clear();
    }
}

package com.standalone.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static Date getDateTime(long timestamp) {
        return new Date(timestamp);
    }

    public static Date parseTime(String fmt, String str) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(String fmt, Date dt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
        return sdf.format(dt);
    }

    public static Date now() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }
}

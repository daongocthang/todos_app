package com.standalone.core.utils;

public class NumberUtil {
    public static int toInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int toInt(String s) {
        return toInt(s, 0);
    }
}

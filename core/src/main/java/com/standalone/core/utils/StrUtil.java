package com.standalone.core.utils;

import java.util.Arrays;

public class StrUtil {
    public static boolean empty(String s) {
        return s == null || s.isBlank();
    }

    public static String camelToSnake(String s) {
        StringBuilder builder = new StringBuilder();
        builder.append(Character.toLowerCase(s.charAt(0)));
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append("_").append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String pluralize(String s) {
        String t = s.toLowerCase();

        if (t.equals("child")) return "children";
        if (t.equals("man")) return "men";
        if (t.equals("foot")) return "feet";
        if (t.equals("tooth")) return "teeth";
        if (t.equals("mouse")) return "mice";
        if (t.equals("person")) return "people";

        String[] excludedWords = {"sheep", "deer", "fish", "series", "species"};
        if (Arrays.asList(excludedWords).contains(t)) return s;

        if (t.endsWith("fe"))
            return s.substring(0, s.length() - 2) + "ves";

        if (t.endsWith("f")) return s.substring(0, s.length() - 1) + "ves";

        if (t.endsWith("ty")
                || t.endsWith("dy")
                || t.endsWith("ry")
                || t.endsWith("ny"))
            return s.substring(0, s.length() - 1) + "ies";

        if (t.endsWith("s")
                || t.endsWith("x")
                || t.endsWith("sh")
                || t.endsWith("ch"))
            return s + "es";

        if (t.endsWith("z"))
            return s + "zes";

        return s + "s";
    }
}

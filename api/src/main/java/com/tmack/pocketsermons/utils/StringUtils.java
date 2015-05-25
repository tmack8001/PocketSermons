package com.tmack.pocketsermons.utils;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class StringUtils {

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }
}

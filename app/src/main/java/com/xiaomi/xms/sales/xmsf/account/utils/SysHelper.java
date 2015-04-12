package com.xiaomi.xms.sales.xmsf.account.utils;


/**
 * Wraps varied Context-related helper methods.
 */
public class SysHelper {
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public static boolean checkPasswordPattern(String password) {
        if (password == null) {
            return false;
        }
        int len = password.length();
        if (len < 8 || len > 16) {
            return false;
        }
        boolean hasAlpha = false;
        for (int i = 0; i < len; i++) {
            char ch = password.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                hasAlpha = true;
                break;
            }
        }
        if (!hasAlpha) {
            return false;
        }
        boolean hasDigit = false;
        for (int i = 0; i < len; i++) {
            char ch = password.charAt(i);
            if ((ch >= '0') && ch <= '9') {
                hasDigit = true;
                break;
            }
        }
        return hasDigit;
    }
}

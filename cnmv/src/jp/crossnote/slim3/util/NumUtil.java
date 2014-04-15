/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.math.BigDecimal;

/**
 * NumUtil.
 * 
 * @author kilvistyle
 * @since 2009/12/04
 *
 */
public class NumUtil {
    
    public static final BigDecimal BD_ZERO = new BigDecimal(0);
    
    private NumUtil() {
    }

    /**
     * 引数の文字列をint型に変換する.
     * 数字じゃない場合は 0 を返す.
     * @param targetValue String
     * @return
     */
    public static int toInt(String s) {
        if (!isNumber(s)) {
            return 0;
        }
        return Integer.parseInt(s);
    }
    
    public static long toLong(String s) {
        if (!isNumber(s)) {
            return 0L;
        }
        return Long.parseLong(s);
    }
    
    public static double toDouble(String s) {
        if (!isDecimal(s) && !isNumber(s)) {
            return 0D;
        }
        return Double.parseDouble(s);
    }
    
    public static BigDecimal toBigDecimal(String s) {
        if (!isDecimal(s) && !isNumber(s)) {
            return BD_ZERO;
        }
        return new BigDecimal(s);
    }

    /**
     * 文字列が数値（負数含む）かどうか判定する.
     * @param s 文字列
     * @return 数値の場合<code>true</code>
     */
    public static boolean isNumber(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        if (s.startsWith("-")) {
            s = s.substring(1);
        }
        int size = s.length();
        for (int i = 0; i < size; i++) {
            char chr = s.charAt(i);
            if (chr < '0' || '9' < chr) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 文字列が小数（負数含む）かどうか判定する.
     * @param s 文字列
     * @return 小数の場合<code>true</code>
     */
    public static boolean isDecimal(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        if (s.startsWith("-")) {
            s = s.substring(1);
        }
        int size = s.length();
        if (s.startsWith(".") || s.endsWith(".")) {
            return false;
        }
        boolean hasDecimalPoint = false;
        for (int i = 0; i < size; i++) {
            char chr = s.charAt(i);
            if (chr < '0' || '9' < chr) {
                if (chr == '.' && !hasDecimalPoint) {
                    hasDecimalPoint = true;
                }
                else {
                    return false;
                }
            }
        }
        return hasDecimalPoint;
    }
    
}

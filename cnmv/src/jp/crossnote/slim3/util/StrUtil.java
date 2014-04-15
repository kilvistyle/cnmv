/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.slim3.util.StringUtil;

/**
 * StrUtil.
 * 文字列操作系便利メソッドをまとめたUtilクラス.
 * {@link StringUtil}も参考にして下さい.
 * 
 * @author kilvistyle
 * @since 2009/03/26
 *
 */
public class StrUtil {
	
	/** このシステム環境の改行コード */
	public static final String LS = System.getProperty("line.separator");

	/** このシステム環境のファイルセパレータ */
	public static final String FS = System.getProperty("file.separator");

    /**
     * インスタンスを隠蔽
     */
	private StrUtil() {
	}

    /**
     * 文字列同士が等しいかどうか返します。どちらもnullの場合は、<code>true</code>を返します。
     * 
     * @param target1 文字列1
     * @param target2 文字列2
     * @return 文字列同士が等しいかどうか
     */
    public static boolean equals(final String target1, final String target2) {
        return (target1 == null) ? (target2 == null) : target1.equals(target2);
    }

	/**
	 * 引数の文字列がnull、空文字、または 0 であるか判定する.
	 * @param targetValue String チェック対象
	 * @return boolean true= null or "" or 0
	 */
	public static boolean isEmptyZero(String targeString) {
		return StringUtil.isEmpty(targeString) || "0".equals(targeString);
	}

    /**
     * 文字列に、数値を16進数に変換した文字列を追加します。
     * 
     * @param buf
     *            追加先の文字列
     * @param i
     *            数値
     */
    public static void appendHex(final StringBuffer buf, final byte i) {
        buf.append(Character.forDigit((i & 0xf0) >> 4, 16));
        buf.append(Character.forDigit((i & 0x0f), 16));
    }

    /**
     * 16進数の文字列に変換します。
     * 
     * @param bytes
     *            バイトの配列
     * @return 16進数の文字列
     */
    public static String toHex(final byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; ++i) {
            appendHex(sb, bytes[i]);
        }
        return sb.toString();
    }

	public static String toNotNullString(String targetValue) {
		if (targetValue == null) {
			return "";
		}
		return targetValue;
	}

	/**
     * 指定文字列を指定文字で左埋めする。
     * 
     * @param val      文字列
     * @param c        左埋めする文字
     * @param digit    左埋め後の桁数
     * @return         val.length >= digit であれば val の文字列表現
     *                 val.length <  digit であれば左埋めした文字列
     */
    public static String paddingChar(String val, char c, int digit) {
        
        String str = val != null ? val : "";
        int length = digit - str.length();
        if (length <= 0) return str;
        
        char[] ch = new char[digit];
        Arrays.fill(ch, c);
        System.arraycopy(str.toCharArray(), 0, ch, length, str.length());
        
        return new String(ch);
    }
    
    public static String toSanitizingFileName(String source) {
    	// 引数の文字列からファイル名として利用できない記号（）を取り除く
    	if (StringUtil.isEmpty(source)) {
    		return source;
    	}
    	// \, /, :, *, ?, ", <, >, | を @ に変換する
    	return source
    		.replace('\\', '@')
    		.replace('/',  '@')
    		.replace(':',  '@')
    		.replace('*',  '@')
    		.replace('?',  '@')
    		.replace('"',  '@')
    		.replace('<',  '@')
    		.replace('>',  '@')
    		.replace('|',  '@');
    }
    
    /**
     * 文字列を置き換えます。
     * 
     * @param text
     *            テキスト
     * @param fromText
     *            置き換え対象のテキスト
     * @param toText
     *            置き換えるテキスト
     * @return 結果
     */
    public static final String replace(final String text,
            final String fromText, final String toText) {

        if (text == null || fromText == null || toText == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(100);
        int pos = 0;
        int pos2 = 0;
        while (true) {
            pos = text.indexOf(fromText, pos2);
            if (pos == 0) {
                buf.append(toText);
                pos2 = fromText.length();
            } else if (pos > 0) {
                buf.append(text.substring(pos2, pos));
                buf.append(toText);
                pos2 = pos + fromText.length();
            } else {
                buf.append(text.substring(pos2));
                break;
            }
        }
        return buf.toString();
    }

	/**
	 * 引数の文字列から改行を取り除いて返却する.
	 * @param targetString
	 * @return
	 */
	public static String toRemoveLineBreak(String targetString) {
		// 最初にCR+LFの改行コードを除去
		String newString = replace(targetString, "\r\n", "");
		// CRの改行コードを除去
		newString = replace(newString, "\r", "");
		// LFの改行コードを除去
		return replace(newString, "\n", "");
	}

    /**
     * Splits the text by the delimiter.
     * 
     * @param text
     *            the text
     * @param delim
     *            the delimiter
     * @return the array of strings
     */
    public static String[] splittrim(String text, String delim) {
        if (StringUtil.isEmpty(text)) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(text, delim);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken().trim());
        }
        return list.toArray(new String[list.size()]);
    }

	/**
	 * 引数の文字列から改行ポイントで分割し、文字列配列として返却する.
	 * @param targetString
	 * @return
	 */
	public static String[] splitLineBreak(String targetString) {
		// 最初にCR+LFの改行コードをLFに変換
		String newString = replace(targetString, "\r\n", "\n");
		// CRの改行コードをLFに変換
		newString = replace(newString, "\r", "\n");
		// LFの改行コードで分割
		return newString.split("\n");
	}
	
	public static String toSizeLabel(long byteSize) {
		String[] bt = {"byte","KB","MB","GB","TB"};
		int unit = 0;
		long calc = byteSize;
		while(1000 <= calc && unit < bt.length) {
			calc = new BigDecimal(calc).movePointLeft(3).longValue();
			++unit;
		}
		return calc+bt[unit];
	}
	
	public static String arrayToString(String separator, Object...array) {
	    if (separator == null || array == null) {
	        return null;
	    }
	    StringBuffer sb = new StringBuffer();
	    for (Object obj : array) {
	        if (sb.length() != 0) {
	            sb.append(separator);
	        }
	        sb.append(obj.toString());
	    }
	    return sb.toString();
	}

}

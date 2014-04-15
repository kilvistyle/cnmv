/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.Calendar;
import java.util.Date;

import org.slim3.util.LocaleLocator;
import org.slim3.util.StringUtil;
import org.slim3.util.TimeZoneLocator;

/**
 * DateUtil.
 * 
 * @author kilvistyle
 * @since 2009/03/29
 *
 */
public class DateUtil {
	
	/**
	 * システム日付を取得する.
	 * @return
	 */
	public static final Date getSystemDate() {
		return org.slim3.util.DateUtil.toCalendar(new Date()).getTime();
	}

	/**
	 * DATE_PATTERNのシステム日付文字列を取得する.
	 * @param pattern DATE_PATTERN パターン
	 * @return String 
	 */
	public static final String getFormattedSystemDate(DATE_PATTERN pattern) {
		return format(getSystemDate(), pattern);
	}
	
	/**
	 * 指定されたフォーマットのシステム日付文字列を取得する.
	 * DATE_PATTERNに無い独自のフォーマットで取得したい場合に利用してください。
	 * @param pattern String SimpleDateFormatのパターン
	 * @return String システム日付文字列
	 */
	public static final String getFormattedSystemDate(String pattern) {
		return format(getSystemDate(), pattern);
	}
	
	/**
	 * String型の年、月、日からDate型オブジェクトへ変換する.<br/>
	 * 変換できない文字列の場合はnullを返却する.
	 * @param year String 西暦の年（例：2009）
	 * @param month String 月（0～11の範囲で指定）
	 * @param date String 日
	 * @return Date 変換したDateオブジェクト
	 */
	public static final Date convertDate(String year, String month, String date) {
		if (!isValid(year, month, date)) { return null; }
		return convertDate(
				Integer.parseInt(year),
				Integer.parseInt(month),
				Integer.parseInt(date));
	}

	/**
	 * int型の年、月、日からDate型オブジェクトへ変換する.<br/>
	 * 変換できない文字列の場合はnullを返却する.
	 * @param year int 西暦の年（例：2009）
	 * @param month int 月（0～11の範囲で指定）
	 * @param date int 日
	 * @return Date 変換したDateオブジェクト
	 */
	public static final Date convertDate(int year, int month, int date) {
		if (!isValid(year, month, date)) { return null; }
        Calendar cal =
            Calendar.getInstance(TimeZoneLocator.get(), LocaleLocator.get());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, date);
		return cal.getTime();
	}

	/**
	 * 引数に指定された年月日が、日付として打倒であるかを検証する.
	 * 引数の文字列が数値に変換できない場合もfalseとする.
	 * @param year String 西暦の年（例：2009）
	 * @param month String 月（0～11の範囲で指定）
	 * @param date String 日
	 * @return boolean
	 */
	public static boolean isValid(String year, String month, String date) {
		if (NumUtil.isNumber(year)
			&& NumUtil.isNumber(month)
			&& NumUtil.isNumber(date)) {
			return isValid(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date));
		}
		else {
			return false;
		}
	}
	
	/**
	 * 引数に指定された年月日が、日付として打倒であるかを検証する.
	 * @param year int 西暦の年（例：2009）
	 * @param month int 月（0～11の範囲で指定）
	 * @param date int 日
	 * @return boolean
	 */
	public static boolean isValid(int year, int month, int date) {
        Calendar cal =
            Calendar.getInstance(TimeZoneLocator.get(), LocaleLocator.get());
    	cal.set(year, month, date);
    	if (year == cal.get(Calendar.YEAR)
    		&& month == cal.get(Calendar.MONTH)
    		&& date == cal.get(Calendar.DATE)) {
    		return true;
    	}
    	else {
    		return false;
    	}
	}
	
	public static String format(Date date, DATE_PATTERN pattern) {
	    return format(date, pattern.getPattern());
	}

    public static String format(Date date, String pattern) {
        if (date == null || pattern == null) return null;
        return org.slim3.util.DateUtil.toString(date, pattern);
    }
    
	public static Date parse(String dateString, DATE_PATTERN pattern) {
	    return parse(dateString, pattern.getPattern());
	}
	
	public static Date parse(String dateString, String pattern) {
        if (StringUtil.isEmpty(dateString) || pattern == null) return null;
        return org.slim3.util.DateUtil.toDate(dateString, pattern);
	}
	
	/**
	 * カレンダの規則に基づいて、引数の日付から、指定された時間量を指定されたカレンダフィールドに加算または減算します。
	 * このメソッドはCalendar#addを簡単に使えるようにしただけです。
	 * @param date 基準日
	 * @param field カレンダフィールドの指定
	 * @param amount 時間量
	 * @return 基準日から指定時間量加算or減算された新しいDateオブジェクト
	 * @see java.util.Calendar
	 */
	public static Date add(Date date, FIELD field, int amount) {
        Calendar cal =
            Calendar.getInstance(TimeZoneLocator.get(), LocaleLocator.get());
		cal.setTime(date);
		// それぞれのフィールド毎の演算結果を返却
		switch (field) {
		case YEAR:
			cal.add(Calendar.YEAR, amount);
			break;
		case MONTH:
			cal.add(Calendar.MONTH, amount);
			break;
		case DATE:
			cal.add(Calendar.DATE, amount);
			break;
		case HOUR:
			cal.add(Calendar.HOUR, amount);
			break;
		case MINUTE:
			cal.add(Calendar.MINUTE, amount);
			break;
		case SECOND:
			cal.add(Calendar.SECOND, amount);
			break;
		case MILLISECOND:
			cal.add(Calendar.MILLISECOND, amount);
			break;
		default:
			break;
		}
		return cal.getTime();
	}
	
	/**
	 * DATE_PATTERN.
	 * SimpleDateFormatのパターンに準拠した日付の基本フォーマットの列挙型。
	 * 
	 * @author kilvistyle
	 * @since 2009/03/29
	 *
	 */
	public enum DATE_PATTERN {
		YYYYMMDD {
			public String getPattern() {
				return "yyyyMMdd";
			}
		},
		YYYYMMDDHHMM {
			public String getPattern() {
				return "yyyyMMddHHmm";
			}
		},
		YYYYMMDDHHMMSS {
			public String getPattern() {
				return "yyyyMMddHHmmss";
			}
		},
		YYYYMMDDHHMMSSMS {
			public String getPattern() {
				return "yyyyMMddHHmmssSS";
			}
		},
		YYYY_MM_DD {
			public String getPattern() {
				return "yyyy/MM/dd";
			}
		},
		YYYY_MM_DD_HH_MM {
			public String getPattern() {
				return "yyyy/MM/dd HH:mm";
			}
		},
		YYYY_MM_DD_HH_MM_SS {
			public String getPattern() {
				return "yyyy/MM/dd HH:mm:ss";
			}
		},
		YYYY_MM_DD_HH_MM_SS_MS {
			public String getPattern() {
				return "yyyy/MM/dd HH:mm:ss.SS";
			}
		},
		YYMMDD {
			public String getPattern() {
				return "yyMMdd";
			}
		},
		YY_MM_DD {
			public String getPattern() {
				return "yy/MM/dd";
			}
		};
		public abstract String getPattern();
	}
	
	public enum FIELD {
		YEAR,
		MONTH,
		DATE,
		HOUR,
		MINUTE,
		SECOND,
		MILLISECOND;
	}
}

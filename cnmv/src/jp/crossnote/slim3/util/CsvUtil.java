/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slim3.util.StringUtil;

/**
 * CsvUtil.
 * 
 * @author kilvistyle
 * @since 2009/12/07
 *
 */
public class CsvUtil {
	private CsvUtil() {
	}

    public static String escapeCsvElement(String source) {
        if (StringUtil.isEmpty(source)) {
            return "";
        }
        // ダブルクォーテーションをエスケープ「"」 -> 「""」
        String s = StrUtil.replace(source, "\"", "\"\"");
        // 数値の場合
        if (NumUtil.isNumber(s) || NumUtil.isDecimal(s)) {
            return s;
        }
        // 文字列の場合クォーテーション
        return "\""+s+"\"";
    }
    
    public static String[][] readCsv(String csv) {
        if (csv == null) {
            return null;
        }
        // 最初にCR+LF、CRの改行コードをLFに変換
        csv = StrUtil.replace(csv, "\r\n", "\n");
        csv = StrUtil.replace(csv, "\r", "\n");
        
        // 行列リストを生成
        List<List<String>> rowList = new ArrayList<List<String>>();
        {
            List<String> elmList = new ArrayList<String>();
            int size = csv.length();
            StringBuffer element = new StringBuffer();
            boolean quote = false;
            for (int i = 0; i < size; i++) {
                char chr = csv.charAt(i);
                if (chr == '\"') {
                    if (quote) {
                		// エスケープされたクォーテーションの場合
                    	if (i+1 < size && csv.charAt(i+1)=='\"') {
                    		element.append(chr);
                    		i++;
                    	}
                    	else {
                            quote = false;
                    	}
                    }
                    else {
                        quote = true;
                    }
                }
                else if (!quote && chr == ',') {
                    //　ここまでの文字列を追加
                    elmList.add(element.toString());
                    element = new StringBuffer();
                }
                else if (!quote && chr == '\n') {
                    //　ここまでの文字列を追加
                    elmList.add(element.toString());
                    element = new StringBuffer();
                    // 次の列へ
                    rowList.add(elmList);
                    elmList = new ArrayList<String>();
                }
                else {
                    // 要素に文字を追加
                    element.append(chr);
                }
            }
            if (!elmList.isEmpty() ||
            	!StringUtil.isEmpty(element.toString())) {
                elmList.add(element.toString());
                rowList.add(elmList);
            }
        }
        // Stringの二次元配列に変換
        String[][] csvTable = new String[rowList.size()][];
        for (int row = 0; row < rowList.size(); row++) {
            List<String> elmList = rowList.get(row);
            csvTable[row] = elmList.toArray(new String[elmList.size()]);
        }
        return csvTable;
    }
    
    public static List<Map<String, String>> csvToElementMaps(String csv) {
        // CSV読み込み
        String[][] csvTable = readCsv(csv);
        if (csvTable == null) {
            return null;
        }
        List<Map<String, String>> elementMapList = new ArrayList<Map<String,String>>();
        String[] keyNames = null;
        int row = 0;
        for (String[] elements : csvTable) {
            if (keyNames == null) {
                // 一行目はKeyNamesとして保持
                keyNames = elements;
            }
            else if (keyNames.length == elements.length){
                // 二行目以降をKey=Value形式でMapに登録
                Map<String, String> elementMap = new LinkedHashMap<String, String>();
                for (int i = 0; i < keyNames.length; i++) {
                    elementMap.put(keyNames[i], elements[i]);
                }
                elementMapList.add(elementMap);
            }
            else {
                // 一行目の項目数と、要素数が不一致の場合はCSVフォーマットエラー
                throw new IllegalArgumentException("The size of elements is different from the headers at row "+row+".");
            }
            row++;
        }
        return elementMapList;
    }
	
}

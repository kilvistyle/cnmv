/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.ResourceBundle;

import org.slim3.util.StringUtil;

/**
 * AppProperties.
 * 
 * @author kilvistyle
 * @since 2009/11/22
 *
 */
public class AppProperties {

    private static ResourceBundle bundle = null;

    static {
        bundle = ResourceBundle.getBundle("cnapp"); 
    }
    
    /**
     * アプリのデフォルト日付表示パターン
     */
    public static final String CNS3_DATE_PATTERN = bundle.getString("cns3.format.datepattern");
    
    /**
     * CVSファイルのエンコーディング.
     */
    public static final String CNS3_ENCODING_CSV = bundle.getString("cns3.encoding.cvs");
    
    /**
     * CNMVでの一覧表示件数
     */
    public static final int CNS3_VIEWER_LISTCOUNT = Integer.parseInt(bundle.getString("cns3.viewer.listcount"));
    
    /**
     * CNMVでの表示・編集可能なコレクション要素数
     */
    public static final int CNS3_VIEWER_ELEMENT_LIMIT =
        Integer.parseInt(bundle.getString("cns3.viewer.elemlimit"));
    
    /**
     * CNMVで扱う外部ライブラリに存在するModelクラスのフルネーム配列を取得する.
     * @return String[] モデル名の文字配列
     */
    public static final String[] getExternalModelNames() {
    	return StringUtil.split(bundle.getString("cns3.viewer.externalModelNames"), ",");
    }
    
//    /**
//     * CNMVでのビューアで参照したモデルの履歴一覧を利用するか否か
//     */
//    public static final boolean CNS3_VIEWER_USE_HISTORY =
//    	BooleanUtil.toPrimitiveBoolean(bundle.getString("cns3.viewer.usehistory"));
}

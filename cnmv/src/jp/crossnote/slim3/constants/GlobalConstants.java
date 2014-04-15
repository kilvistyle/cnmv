/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.constants;

/**
 * GlobalConstants.
 * 
 * @author kilvistyle
 * @since 2009/11/15
 *
 */
public class GlobalConstants {
    private GlobalConstants() {
    }
    
    /**
     * root package.
     */
    public static final String ROOT_PACKAGE = "jp.crossnote.slim3";
    
    /**
     * CNアプリケーションメッセージバンドル
     */
    public static final String CN_MESSAGE_BUNDLE = "cn_application";

    /**
     * The name of cn unique package.
     */
    public static final String Package = "crossnote.slim3.";
    
    /** The key of message */
    public static final String MESSAGE_KEY = Package+"MESSAGE";
    
    /** MemcacheParameterKey : CNMVのバックグラウンドタスクの一時停止用キー */
    public static final String CNMV_TASK_PAUSE_KEY = Package+"TASK_PAUSE:";
    /** MemcacheParameterKey : CNMVのページングカーソル用キー */
    public static final String CNMV_PAGING_CURSOR_KEY = Package+"CNMV_PAGING_CURSOR:";
}

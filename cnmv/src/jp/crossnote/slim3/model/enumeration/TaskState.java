/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.model.enumeration;

/**
 * TaskState.
 * 
 * @author kilvistyle
 * @since 2010/06/03
 *
 */
public enum TaskState {
	/** 処理中 */
	WORKING,
	/** 一時停止 */
	STOPPED,
	/** 失敗 */
	FAILED,
	/** 成功 */
	SUCCEEDED;
}

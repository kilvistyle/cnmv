/**
 * Copyright (c) 2009 kilvistyle
 */

package jp.crossnote.slim3.controller;

import java.io.Serializable;

import jp.crossnote.slim3.util.NumUtil;

/**
 * Paging.<BR>
 * 現在のページ番号、ページ内の表示件数、最大要素数から、
 * 前後ページの遷移可否や前後ページ番号をJSP上で扱いやすくします。<br/>
 * セッションスコープのアクションフォーム内でPagingオブジェクトを持ちまわす場合は、
 * 必ずActionForm#reset()メソッド内にて、Paging#reset()を呼び出し初期化してください。
 * 次ページ番号などを問い合わせる際には、その直前でPaging#set()メソッドにて、
 * 必要な情報を都度設定してください。
 * 
 * @author kilvistyle.
 * @since 2009/05/28
 */
public class Paging implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int currentPage;
	private int viewLimit;
	private int maxSize;
	
	public Paging() {
		this.reset();
	}
	
	public Paging(int currentPage, int viewLimit, int maxSize) {
		this.set(currentPage, viewLimit, maxSize);
	}
	
	public Paging(String currentPage, String viewLimit, String maxSize) {
	    this.reset();
	    this.setPage(currentPage);
	    this.setLimit(viewLimit);
	    this.setMaxSize(maxSize);
	}
	
	public Paging(String currentPage, int viewLimit, int maxSize) {
        this.reset();
	    this.setPage(currentPage);
	    this.viewLimit = viewLimit;
	    this.maxSize = maxSize;
	}
	
	public void reset() {
		currentPage = 0;
		viewLimit = 0;
		maxSize = 0;
	}
	
	public void set(int currentPage, int viewLimit, int maxSize) {
		this.currentPage = currentPage;
		this.viewLimit = viewLimit;
		this.maxSize = maxSize;
	}
	
	public int getPageInt() {
		return currentPage;
	}
	
	/**
	 * 現在のページ番号を取得する
	 * @return
	 */
	public String getPage() {
		return Integer.toString(currentPage);
	}
	
	/**
	 * 現在のページ番号をセットする
	 * @param currentPage
	 */
	public void setPage(String currentPage) {
		if (NumUtil.isNumber(currentPage)) {
			this.currentPage = NumUtil.toInt(currentPage);
		}
	}
	
	public int getLimitInt() {
		return viewLimit;
	}
	
	/**
	 * 一ページ内の項目表示上限数を取得する
	 * @return
	 */
	public String getLimit() {
		return Integer.toString(viewLimit);
	}
	/**
	 * 一ページ内の項目表示上限数をセットする
	 * @param viewLimit
	 */
	public void setLimit(String viewLimit) {
		if (NumUtil.isNumber(viewLimit)) {
			this.viewLimit = NumUtil.toInt(viewLimit);
		}
	}

	public int getMaxSizeInt() {
		return maxSize;
	}

	/**
	 * @return maxSize
	 */
	public String getMaxSize() {
		return Integer.toString(maxSize);
	}

	/**
	 * @param maxSize セットする maxSize
	 */
	public void setMaxSize(String maxSize) {
		if (NumUtil.isNumber(maxSize)) {
			this.maxSize = NumUtil.toInt(maxSize);
		}
	}
	
	/**
	 * 現在のオフセットを取得する
	 * @return
	 */
	public int getOffset() {
		return currentPage * viewLimit;
	}

	/**
	 * 前のページが存在するか判定する.
	 * @return
	 */
	public boolean isBackPage() {
		return 0 < currentPage;
	}
	
	/**
	 * 前のページ番号を取得する.
	 * @return
	 */
	public int getBack() {
		if (this.isBackPage()) {
			int lastPage = getLast();
			return lastPage < currentPage? lastPage : currentPage -1;
		}
		return 0;
	}
	
	/**
	 * 次のページがあるか判定する.
	 * @return
	 */
	public boolean isNextPage() {
		return (currentPage+1) * viewLimit < maxSize;
	}
	
	/**
	 * 次のページ番号を取得する
	 * @return
	 */
	public int getNext() {
		if (this.isNextPage()) {
			return currentPage +1;
		}
		return 0;
	}
	
	/**
	 * 最初のページ番号を取得する
	 * @return
	 */
	public int getFirst() {
		return 0;
	}
	
	/**
	 * 最終ページ番号を取得する
	 * @return
	 */
	public int getLast() {
		int lastPage = maxSize / viewLimit;
		if (0 < maxSize % viewLimit) {
			return lastPage;
		}
		else {
			return lastPage-1;
		}
	}
	
	/**
	 * ページ番号の配列を取得する
	 * @return
	 */
	public int[] getArrayPageIndex() {
		if (maxSize == 0 || viewLimit == 0) {
			return new int[]{0};
		}
		int lastPageIndex = getLast();
		int[] arrPageIndex = new int[lastPageIndex+1];
		for (int i = 0; i < arrPageIndex.length; i++) {
			arrPageIndex[i] = i;
		}
		return arrPageIndex;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Paging)) return false;
		return this.currentPage == ((Paging)obj).currentPage
			&& this.maxSize == ((Paging)obj).maxSize
			&& this.viewLimit == ((Paging)obj).viewLimit;
	}
	
	
}

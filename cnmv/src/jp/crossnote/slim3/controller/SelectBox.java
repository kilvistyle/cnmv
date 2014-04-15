/**
 * Copyright (c) 2009 kilvistyle
 */

package jp.crossnote.slim3.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SelectBox.<BR>
 * セレクトボックスを表すクラス.<BR>
 * コンストラクタの引数にてさまざまな形式からセレクトボックスを生成します。<BR>
 * 生成したセレクトボックスオブジェクトは、JSPにて以下のように記述することで、簡単にセレクトボックスを作成することができます。<BR>
 * <BR>
 * 以下の例では、ActionFormクラスのプロパティに、<BR>
 * <pre>
 * // 選択された色（セレクトボックスで選択されたパラメータがバインドされるプロパティ）
 * public String color;
 * // セレクトボックス（JSPに配置するセレクトボックス）
 * public SelectBox colorSelectBox;
 * </pre>
 * として定義しています。<BR>
 * このcolorSelectBoxをJSPでセレクトボックスとして利用するには以下のように記述します。<BR>
 * <pre>
 * &lt;html:select property="color"&gt;
 *     &lt;c:forEach var="color" items="${colorSelectBox.elements}"&gt;
 *         &lt;html:option value="${color.value}"&gt;${f:h(color.label)}&lt;/html:option&gt;
 *     &lt;/c:forEach&gt;
 * &lt;/html:select&gt;
 * </pre>
 * @author kilvistyle.
 * @since 2009/03/27
 */
public class SelectBox implements Serializable {
    
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** セレクトボックス要素からvalueプロパティ値を取得するためのキー */
    public static final String KEY_VALUE = "value";
    /** セレクトボックス要素からlabel（表示名）を取得するためのキー */
    public static final String KEY_LABEL = "label";
    /**
     * セレクトボックス要素リスト
     */
    private List<Map<String, String>> elements;
    
    /**
     * Enum#toString()を各要素として表示するセレクトボックスを生成する.<br/>
     * セレクトボックスのvalue値には Enum#name() の値が設定されます。<br/>
     * @param labels Enum[] toString()を実装したEnum型配列
     */
    @SuppressWarnings("unchecked")
	public SelectBox(Enum[] labels) {
        String[] arrValues = new String[labels.length];
        String[] arrLabels = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
        	arrValues[i] = labels[i].name();
        	arrLabels[i] = labels[i].toString();
        }
        // セレクトボックスの要素を初期化
        this.elements = this.createSelectBoxElements(arrValues, arrLabels);
    }
    
    /**
     * カンマ（,）で区切られた文字列を各要素として表示するセレクトボックスを生成する.<BR>
     * セレクトボックスのvalue値には 0 から始まる連番が自動的に設定されます。<BR>
     * @param labels String カンマ区切りの表示用ラベル文字列
     */
    public SelectBox(String labels) {
        String[] arrLabels = labels.split(",");
        // arrValuesは0からの項番を設定
        String[] arrValues = new String[arrLabels.length];
        for (int i = 0; i < arrLabels.length; i++) {
        	arrValues[i] = Integer.toString(i);
        }
        // セレクトボックスの要素を初期化
        this.elements = this.createSelectBoxElements(arrValues, arrLabels);
    }
    
    /**
     * カンマ（,）で区切られた文字列を各要素として表示するセレクトボックスを生成する.<BR>
     * 第一引数は value(値) として、第二引数は label(表示名) として設定されます。<BR>
     * ※valuesとlabelsの要素数は同じである必要があります。<BR>
     * @param values String value（値）をカンマ（,）で区切った文字列
     * @param labels String label（表示名）をカンマ（,）で区切った文字列
     */
    public SelectBox(String values, String labels) {
        String[] arrValues = values.split(",");
        String[] arrLabels = labels.split(",");
        // セレクトボックスの要素を初期化
        this.elements = this.createSelectBoxElements(arrValues, arrLabels);
    }

    /**
     * 第一引数は value(値) として、第二引数は label(表示名) としたセレクトボックスを生成する.<BR>
     * ※valuesとlabelsの要素数は同じである必要があります。<BR>
     * @param values String value（値）配列
     * @param labels String label（表示名）配列
     */
    public SelectBox(String[] values, String[] labels) {
        // セレクトボックスの要素を初期化
        this.elements = this.createSelectBoxElements(values, labels);
    }
    
    /**
     * 引数のMapのkey:valueを、セレクトボックスのvalue:labelとして生成する.<BR>
     * 生成されるセレクトボックス要素の順序は、引数のMapの#entrySet()で返されるビューの順序に従います。<BR>
     * @param elementCollection Map<String, String>
     */
    public SelectBox(Map<String, String> elementCollection) {
    	this.elements = new ArrayList<Map<String,String>>();
    	for (Entry<String, String> entry : elementCollection.entrySet()) {
            Map<String, String> element = new HashMap<String, String>();
            // valueとlabelを設定
            element.put(KEY_VALUE, entry.getKey());
            element.put(KEY_LABEL, entry.getValue());
            // 要素リストに追加
            this.elements.add(element);
    	}
    }
    
    /**
     * セレクトボックス用の要素リストを生成する.
     * @param arrValues String[] value値リスト
     * @param arrLabels String[] labelリスト
     * @return List<Map<String, String>>
     */
    private List<Map<String, String>> createSelectBoxElements(
        String[] arrValues, String[] arrLabels){
        if (arrValues != null
            && 0 < arrValues.length
            && arrValues.length != arrLabels.length) {
            throw new IllegalArgumentException("SelectBox要素の生成に失敗しました。\n" +
                "コンストラクタで指定された values と labels の要素数が違います。");
        }
        // セレクトボックス要素リストを生成
        List<Map<String, String>> lst = new ArrayList<Map<String,String>>();
        for (int i = 0; i < arrValues.length; i++) {
            Map<String, String> element = new HashMap<String, String>();
            // valueとlabelを設定
            element.put(KEY_VALUE, arrValues[i]);
            element.put(KEY_LABEL, arrLabels[i]);
            // 要素リストに追加
            lst.add(element);
        }
        // 生成した要素リストを返却
        return lst;
    }
    
    /**
     * セレクトボックス用の要素を追加する.
     * @param value　String value
     * @param label String label
     * @return このオブジェクト自身
     */
    public SelectBox addElement(String value, String label) {
    	if (elements == null) {
    		elements = new ArrayList<Map<String,String>>();
    	}
        Map<String, String> element = new HashMap<String, String>();
        // valueとlabelを設定
        element.put(KEY_VALUE, value);
        element.put(KEY_LABEL, label);
        // 要素リストに追加
        elements.add(element);
        // このオブジェクト自身を返却
        return this;
    }

    /**
     * index番目にセレクトボックス用の要素を追加する.
     * @param index int 追加するindex
     * @param value　String value
     * @param label String label
     * @return このオブジェクト自身
     */
    public SelectBox addElement(int index, String value, String label) {
    	if (elements == null) {
    		elements = new ArrayList<Map<String,String>>();
    	}
        Map<String, String> element = new HashMap<String, String>();
        // valueとlabelを設定
        element.put(KEY_VALUE, value);
        element.put(KEY_LABEL, label);
        // 要素リストに追加
        elements.add(index, element);
        // このオブジェクト自身を返却
        return this;
    }

    /**
     * SelectBoxのelementsを取得する.
     * @return elements
     */
    public List<Map<String, String>> getElements() {
        return elements;
    }

    /**
     * valueプロパティ値からlabel（表示名）を取得する.
     * @param value String セレクトボックスのvalueプロパティ値
     * @return String label（表示名）
     */
    public String getLabel(String value) {
        for (Map<String, String> element : this.elements) {
            if (element.get(KEY_VALUE).equals(value)){ return element.get(KEY_LABEL); }
        }
        return null;
    }
    
    public List<String> valueList() {
        List<String> idList = new ArrayList<String>(this.elements.size());
        for (Map<String, String> element : this.elements) {
            idList.add(element.get(KEY_VALUE));
        }
        return idList;
    }
    
    public List<String> labelList() {
        List<String> nameList = new ArrayList<String>(this.elements.size());
        for (Map<String, String> element : this.elements) {
            nameList.add(element.get(KEY_LABEL));
        }
        return nameList;
    }

}

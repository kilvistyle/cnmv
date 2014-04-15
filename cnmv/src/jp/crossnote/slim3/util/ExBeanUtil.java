/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slim3.util.BeanUtil;
import org.slim3.util.CopyOptions;
import org.slim3.util.RequestMap;
import org.slim3.util.StringUtil;

/**
 * ExBeanUtil.
 * 
 * @author kilvistyle
 * @since 2009/11/21
 *
 */
public class ExBeanUtil {

    private static final CopyOptions DEFAULT_OPTIONS = new CopyOptions();

    private ExBeanUtil() {
    }

    /**
     * HttpServletRequestのパラメータの値をModelリストにコピーする.
     * @param src
     * @param destModelList
     */
    @SuppressWarnings("unchecked")
    public static void copyList(HttpServletRequest src, List destModelList) {
        copyList(src, "", destModelList, DEFAULT_OPTIONS);
    }

    /**
     * HttpServletRequestのパラメータの値をModelリストにコピーする.
     * @param src
     * @param destModelList
     * @param options
     */
    @SuppressWarnings("unchecked")
    public static void copyList(HttpServletRequest src, List destModelList, CopyOptions options) {
        copyList(src, "", destModelList, options);
    }
    
    /**
     * HttpServletRequestのパラメータの値をModelリストにコピーする.
     * @param src
     * @param modelName
     * @param destModelList
     */
    @SuppressWarnings("unchecked")
    public static void copyList(HttpServletRequest src, String modelName, List destModelList) {
        copyList(src, modelName, destModelList, DEFAULT_OPTIONS);
    }

    /**
     * HttpServletRequestのパラメータの値をModelリストにコピーする.
     * @param src
     * @param modelName
     * @param destModelList
     * @param options
     */
    @SuppressWarnings("unchecked")
    public static void copyList(HttpServletRequest src, String modelName, List destModelList, CopyOptions options) {
        if (src == null) {
            throw new NullPointerException("The src parameter is null.");
        }
        if (modelName == null) {
            throw new NullPointerException("The modelName parameter is null.");
        }
        if (destModelList == null) {
            throw new NullPointerException("The destModelList parameter is null.");
        }
        if (options == null) {
            throw new NullPointerException("The options parameter is null.");
        }
        Map<String, Object> srcMap = new RequestMap(src);
        Map<String, Map<String, Object>> indexMap = new HashMap<String, Map<String,Object>>();
        for (Map.Entry<String, Object> entry : srcMap.entrySet()) {
            // 配列プロパティ名に含まれる要素番号を取得
            String index = getIndex(entry.getKey(), modelName);
            if (index == null) {
                continue;
            }
            // 要素番号のMapを取得
            Map<String, Object> modelMap = indexMap.get(index);
            if (modelMap == null) {
                modelMap = new HashMap<String, Object>();
                indexMap.put(index, modelMap);
            }
            // プロパティ名を取得
            modelMap.put(getRealPropertyName(entry.getKey()), entry.getValue());
        }
        // 配列の要素番号に紐付けたmodelMapの内容をdestModelにコピーする
        int size = destModelList.size();
        for (int i = 0; i < size; i++) {
            BeanUtil.copy(indexMap.get(Integer.toString(i)), destModelList.get(i), options);
        }
    }
    
    private static String getIndex(String propertyName, String modelName) {
        if (!StringUtil.isEmpty(modelName)) {
            if (!propertyName.startsWith(modelName)) {
                return null;
            }
            propertyName = propertyName.substring(modelName.length());
        }
        try {
            if (propertyName.startsWith("[")) {
                return propertyName.substring(1, propertyName.indexOf("]"));
            }
        }
        catch (Exception e) {
        }
        return null;
    }

    private static String getRealPropertyName(String propertyName) {
        return propertyName.substring(propertyName.lastIndexOf("]")+1);
    }

    /**
     * Modelリストのパラメータの値をHttpServletRequestにコピーする.
     * @param srcModelList
     * @param dest
     */
    @SuppressWarnings("unchecked")
    public static void copyList(List srcModelList, HttpServletRequest dest) {
        copyList("", srcModelList, dest, DEFAULT_OPTIONS);
    }

    /**
     * Modelリストのパラメータの値をHttpServletRequestにコピーする.
     * @param srcModelList
     * @param dest
     * @param options
     */
    @SuppressWarnings("unchecked")
    public static void copyList(List srcModelList, HttpServletRequest dest, CopyOptions options) {
        copyList("", srcModelList, dest, options);
    }

    /**
     * Modelリストのパラメータの値をHttpServletRequestにコピーする.
     * @param modelName
     * @param srcModelList
     * @param dest
     */
    @SuppressWarnings("unchecked")
    public static void copyList(String modelName, List srcModelList, HttpServletRequest dest) {
        copyList(modelName, srcModelList, dest, DEFAULT_OPTIONS);
    }
    
    /**
     * Modelリストのパラメータの値をHttpServletRequestにコピーする.
     * @param modelName
     * @param srcModelList
     * @param dest
     * @param options
     */
    @SuppressWarnings("unchecked")
    public static void copyList(String modelName, List srcModelList, HttpServletRequest dest, CopyOptions options) {
        if (modelName == null) {
            throw new NullPointerException("The modelName parameter is null.");
        }
        if (srcModelList == null) {
            throw new NullPointerException("The srcModelList parameter is null.");
        }
        if (dest == null) {
            throw new NullPointerException("The dest parameter is null.");
        }
        if (options == null) {
            throw new NullPointerException("The options parameter is null.");
        }
        modelName = StringUtil.isEmpty(modelName) ? "" : modelName;
        Map<String, Object> destMap = new RequestMap(dest);
        int size = srcModelList.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> modelMap = new HashMap<String, Object>();
            BeanUtil.copy(srcModelList.get(i), modelMap, options);
            for (Map.Entry<String, Object> entry : modelMap.entrySet()) {
                destMap.put(modelName+"["+i+"]"+entry.getKey(), entry.getValue());
            }
        }
    }
}

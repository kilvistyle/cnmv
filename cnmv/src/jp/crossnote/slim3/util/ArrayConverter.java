/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;

/**
 * ArrayConverter.
 * Beanの配列型プロパティのコンバータクラス。
 * 
 * @author kilvistyle
 * @since 2009/11/26
 *
 */
public class ArrayConverter implements Converter<Object> {
    
    private Class<?> arrType = null;
    
    public ArrayConverter(Class<?> arrType) {
        if (arrType == null) {
            throw new NullPointerException("the arrType parameter is null.");
        }
        this.arrType = arrType;
    }

    @Override
    public Object getAsObject(String value) {
        return ModelUtil.stringToArrayValue(arrType, value);
    }

    @Override
    public String getAsString(Object value) {
        return ModelUtil.valueToString(value);
    }

    @Override
    public boolean isTarget(Class<?> clazz) {
        if (clazz.isArray()) {
            if (arrType.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

}

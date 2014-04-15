/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.Collection;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

/**
 * CollectionConverter.
 * BeanのCollectionプロパティのコンバータクラス。
 * 現時点でサポートしているCollection型は、
 * java.util.List, java.util.Set, java.util.SortedSet
 * のみ。
 * 
 * @author kilvistyle
 * @since 2009/11/28
 *
 */
public class CollectionConverter implements Converter<Collection<?>> {

    private Class<? extends Collection<?>> collectionType = null;
    private Class<?> elementType = null;
    
    public CollectionConverter(Class<? extends Collection<?>> collectionType, Class<?> elementType) {
        if (collectionType == null) {
            throw new NullPointerException("the collectionType parameter is null.");
        }
        if (elementType == null) {
            throw new NullPointerException("the elementType parameter is null.");
        }
        this.collectionType = collectionType;
        this.elementType = elementType;
    }
    
    @Override
    public Collection<?> getAsObject(String value) {
        if (value == null) return null;
        String[] arrStr = StringUtil.split(value, ",");
        return ModelUtil.stringToCollectionValue(collectionType, elementType, arrStr);
    }

    @Override
    public String getAsString(Object value) {
        return ModelUtil.valueToString(value);
    }

    @Override
    public boolean isTarget(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

}

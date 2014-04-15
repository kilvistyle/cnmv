/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.validator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.crossnote.slim3.util.ModelUtil;

import org.slim3.controller.validator.AbstractValidator;
import org.slim3.util.ApplicationMessage;
import org.slim3.util.StringUtil;

/**
 * CollectionValidator.
 * 
 * @author kilvistyle
 * @since 2009/11/29
 *
 */
public class CollectionValidator extends AbstractValidator {

    private Class<? extends Collection<?>> collectionType = null;
    private Class<?> elementType = null;
    
    public CollectionValidator(Class<? extends Collection<?>> collectionType, Class<?> elementType) {
        if (collectionType == null) {
            throw new NullPointerException("the collectionType parameter is null.");
        }
        if (elementType == null) {
            throw new NullPointerException("the elementType parameter is null.");
        }
        this.collectionType = collectionType;
        this.elementType = elementType;
    }

    public CollectionValidator(
        Class<? extends Collection<?>> collectionType,
        Class<?> elementType,
        String message) {
        super(message);
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
    public String validate(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || "".equals(value)) {
            return null;
        }
        // a Collection (java.util.List, java.util.Set and java.util.SortedSet) of a core datastore type
        if (value instanceof List<?>) {
            return null;
        }
        if (value instanceof Set<?>) {
            return null;
        }
        // 以下は通常の文字列入力のバリデーション
        if (value instanceof String) {
            String[] arrStr = StringUtil.split((String)value, ",");
            try {
                if (ModelUtil.stringToCollectionValue(collectionType, elementType, arrStr) != null) {
                    // 正常に変換できた場合
                    return null;
                }
            }
            catch (Exception e) {
            }
        }
        // CollectionTypeの変換に失敗した場合
        return ApplicationMessage.get(
        	getMessageKey(),
            getLabel(name), collectionType.getSimpleName(), elementType.getSimpleName());
    }

	@Override
	protected String getMessageKey() {
		return "validator.collection";
	}

}

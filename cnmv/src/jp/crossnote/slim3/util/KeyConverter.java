/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.datastore.Datastore;
import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;

/**
 * KeyConverter.
 * 
 * @author kilvistyle
 * @since 2010/06/29
 *
 */
public class KeyConverter implements Converter<Key> {

	private Class<?> modelClass = null;
    private Policy policy = Policy.STRING_TO_KEY;
    
    public KeyConverter(Class<?> modelClass, Policy policy) {
    	if (modelClass == null) {
    		throw new NullPointerException("the modelClass parameter is null.");
    	}
    	if (policy == null) {
    		throw new NullPointerException("the policy parameter is null.");
    	}
    	this.modelClass = modelClass;
    	this.policy = policy;
    }
    
	@Override
	public Key getAsObject(String value) {
        if (StringUtil.isEmpty(value)) return null;
		switch (policy) {
		case STRING_TO_KEY:
			return Datastore.stringToKey(value);
		case ID_OF_KEY:
			return Datastore.createKey(modelClass, Long.parseLong(value));
		default:
			return Datastore.createKey(modelClass, value);
		}
	}

	@Override
	public String getAsString(Object value) {
        if (value == null) return null;
		switch (policy) {
		case STRING_TO_KEY:
			return Datastore.keyToString((Key)value);
		case ID_OF_KEY:
			return Long.toString(((Key)value).getId());
		default:
			return ((Key)value).getName();
		}
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Key.class.isAssignableFrom(clazz);
	}

	public enum Policy {
		STRING_TO_KEY,
		ID_OF_KEY,
		NAME_OF_KEY;
	}
}

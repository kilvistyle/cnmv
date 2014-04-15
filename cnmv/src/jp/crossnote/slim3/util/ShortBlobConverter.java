/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.ShortBlob;

/**
 * ShortBlobConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/17
 *
 */
public class ShortBlobConverter implements Converter<ShortBlob> {
	
	private static ShortBlobConverter instance = null;
	
	private ShortBlobConverter() {
	}
	
	public static ShortBlobConverter getInstance() {
		if (instance == null) {
			instance = new ShortBlobConverter();
		}
		return instance;
	}
	
	@Override
	public ShortBlob getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
        byte[] bytes = ModelUtil.stringToArrayValue(byte[].class, value);
        return new ShortBlob(bytes);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ModelUtil.valueToString(((ShortBlob)value).getBytes());
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return ShortBlob.class.isAssignableFrom(clazz);
	}

}

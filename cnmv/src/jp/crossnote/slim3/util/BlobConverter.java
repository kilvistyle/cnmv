/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Blob;

/**
 * BlobConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class BlobConverter implements Converter<Blob> {
	
	private static BlobConverter instance = null;
	
	private BlobConverter() {
	}
	
	public static BlobConverter getInstance() {
		if (instance == null) {
			instance = new BlobConverter();
		}
		return instance;
	}

	@Override
	public Blob getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
        byte[] bytes = ModelUtil.stringToArrayValue(byte[].class, value);
        return new Blob(bytes);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ModelUtil.valueToString(((Blob)value).getBytes());
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Blob.class.isAssignableFrom(clazz);
	}

}

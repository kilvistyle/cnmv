/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.PostalAddress;

/**
 * PostalAddressConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class PostalAddressConverter implements Converter<PostalAddress> {
	
	private static PostalAddressConverter instance = null;
	
	private PostalAddressConverter() {
	}
	
	public static PostalAddressConverter getInstance() {
		if (instance == null) {
			instance = new PostalAddressConverter();
		}
		return instance;
	}

	@Override
	public PostalAddress getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
		return new PostalAddress(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((PostalAddress)value).getAddress();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return PostalAddress.class.isAssignableFrom(clazz);
	}

}

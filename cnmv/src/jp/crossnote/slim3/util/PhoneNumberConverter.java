/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.PhoneNumber;

/**
 * PhoneNumberConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class PhoneNumberConverter implements Converter<PhoneNumber> {
	
	private static PhoneNumberConverter instance = null;
	
	private PhoneNumberConverter() {
	}
	
	public static PhoneNumberConverter getInstance() {
		if (instance == null) {
			instance = new PhoneNumberConverter();
		}
		return instance;
	}

	@Override
	public PhoneNumber getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
		return new PhoneNumber(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((PhoneNumber)value).getNumber();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return PhoneNumber.class.isAssignableFrom(clazz);
	}

}

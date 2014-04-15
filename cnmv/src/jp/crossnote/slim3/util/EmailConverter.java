/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Email;

/**
 * EmailConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class EmailConverter implements Converter<Email> {
	
	private static EmailConverter instance = null;
	
	private EmailConverter() {
	}
	
	public static EmailConverter getInstance() {
		if (instance == null) {
			instance = new EmailConverter();
		}
		return instance;
	}

	@Override
	public Email getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
		return new Email(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((Email)value).getEmail();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Email.class.isAssignableFrom(clazz);
	}

}

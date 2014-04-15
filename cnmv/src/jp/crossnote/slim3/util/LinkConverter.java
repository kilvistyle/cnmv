/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Link;

/**
 * LinkConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class LinkConverter implements Converter<Link> {
	
	private static LinkConverter instance = null;
	
	private LinkConverter() {
	}
	
	public static LinkConverter getInstance() {
		if (instance == null) {
			instance = new LinkConverter();
		}
		return instance;
	}

	@Override
	public Link getAsObject(String value) {
        if (StringUtil.isEmpty(value)) return null;
        return new Link(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((Link)value).getValue();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Link.class.isAssignableFrom(clazz);
	}
	

}

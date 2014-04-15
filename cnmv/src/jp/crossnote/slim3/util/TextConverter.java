/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Text;

/**
 * TextConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class TextConverter implements Converter<Text> {
	
	private static TextConverter instance = null;
	
	private TextConverter() {
	}
	
	public static TextConverter getInstance() {
		if (instance == null) {
			instance = new TextConverter();
		}
		return instance;
	}

	@Override
	public Text getAsObject(String value) {
        if (StringUtil.isEmpty(value)) return null;
        return new Text(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((Text)value).getValue();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Text.class.isAssignableFrom(clazz);
	}

}

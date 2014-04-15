/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Category;

/**
 * CategoryConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class CategoryConverter implements Converter<Category> {
	
	private static CategoryConverter instance = null;
	
	private CategoryConverter() {
	}
	
	public static CategoryConverter getInstance() {
		if (instance == null) {
			instance = new CategoryConverter();
		}
		return instance;
	}

	@Override
	public Category getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
		return new Category(value);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return ((Category)value).getCategory();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Category.class.isAssignableFrom(clazz);
	}

}

/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Rating;

/**
 * RatingConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class RatingConverter implements Converter<Rating> {
	
	private static RatingConverter instance = null;
	
	private RatingConverter() {
	}
	
	public static RatingConverter getInstance() {
		if (instance == null) {
			instance = new RatingConverter();
		}
		return instance;
	}

	@Override
	public Rating getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
		return new Rating(Integer.parseInt(value));
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		return Integer.toString(((Rating)value).getRating());
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return Rating.class.isAssignableFrom(clazz);
	}

}

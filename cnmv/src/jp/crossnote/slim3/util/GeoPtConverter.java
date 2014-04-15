/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.GeoPt;

/**
 * GeoPtConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class GeoPtConverter implements Converter<GeoPt> {
	
	private static GeoPtConverter instance = null;
	
	private GeoPtConverter() {
	}
	
	public static GeoPtConverter getInstance() {
		if (instance == null) {
			instance = new GeoPtConverter();
		}
		return instance;
	}

	@Override
	public GeoPt getAsObject(String value) {
        if (StringUtil.isEmpty(value)) return null;
        String[] arrGeoPts = CsvUtil.readCsv(value)[0];
        // [0] GeoPt.latitude (required)
        // [1] GeoPt.longitude (required)
        if (arrGeoPts.length < 2
        	|| StringUtil.isEmpty(arrGeoPts[0])
            || StringUtil.isEmpty(arrGeoPts[1])) {
            throw new IllegalArgumentException("GeoPt.latitude or GeoPt.longitude is empty.");
        }
        return new GeoPt(Float.parseFloat(arrGeoPts[0]), Float.parseFloat(arrGeoPts[1]));
	}

	@Override
	public String getAsString(Object value) {
        if (value == null) return null;
        GeoPt geoPt = (GeoPt)value;
        return geoPt.getLatitude()+","+geoPt.getLongitude();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return GeoPt.class.isAssignableFrom(clazz);
	}

}

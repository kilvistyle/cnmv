/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.util.Date;

import org.slim3.util.ConversionUtil;
import org.slim3.util.DateUtil;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

/**
 * ExConversionUtil.
 * Wraped to org.slim3.util.ConversionUtil.
 * 
 * @author higa (original org.slim3.util.ConversionUtil.)
 * @author kilvistyle
 * @since 2009/11/29
 *
 */
public class ExConversionUtil {

    /**
     * Converts the value into the destination class.
     * Wraped to org.slim3.util.ConversionUtil.convert().
     * 1. Parse date pattern changed.
     * 
     * @param <T>
     *            the type
     * @param value
     *            the value
     * @param destinationClass
     *            the destination class
     * @return a converted value
     * @see {@link org.slim3.util.ConversionUtil}
     * @author higa
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> destinationClass) {
    	// string to value
    	if (value instanceof String) {
    		String str = (String)value;
            if (Date.class.isAssignableFrom(destinationClass)) {
                return (T)DateUtil.toDate(str, AppProperties.CNS3_DATE_PATTERN);
            }
            if (ShortBlob.class.isAssignableFrom(destinationClass)) {
            	return (T)ShortBlobConverter.getInstance().getAsObject(str);
            }
            if (Blob.class.isAssignableFrom(destinationClass)) {
            	return (T)BlobConverter.getInstance().getAsObject(str);
            }
            if (Category.class.isAssignableFrom(destinationClass)) {
            	return (T)CategoryConverter.getInstance().getAsObject(str);
            }
            if (Email.class.isAssignableFrom(destinationClass)) {
            	return (T)EmailConverter.getInstance().getAsObject(str);
            }
            if (GeoPt.class.isAssignableFrom(destinationClass)) {
            	return (T)GeoPtConverter.getInstance().getAsObject(str);
            }
            if (Link.class.isAssignableFrom(destinationClass)) {
            	return (T)LinkConverter.getInstance().getAsObject(str);
            }
            if (PhoneNumber.class.isAssignableFrom(destinationClass)) {
            	return (T)PhoneNumberConverter.getInstance().getAsObject(str);
            }
            if (PostalAddress.class.isAssignableFrom(destinationClass)) {
            	return (T)PostalAddressConverter.getInstance().getAsObject(str);
            }
            if (Rating.class.isAssignableFrom(destinationClass)) {
            	return (T)RatingConverter.getInstance().getAsObject(str);
            }
            if (Text.class.isAssignableFrom(destinationClass)) {
            	return (T)TextConverter.getInstance().getAsObject(str);
            }
            if (User.class.isAssignableFrom(destinationClass)) {
            	return (T)UserConverter.getInstance().getAsObject(str);
            }
            try {
                return ConversionUtil.convert(value, destinationClass);
            }
            catch (IllegalArgumentException e) {
                return (T)SerializableConverter.getInstance().getAsObject(str);
			}
    	}
        // それ以外の場合はslim3標準の変換
        return ConversionUtil.convert(value, destinationClass);
    }
}

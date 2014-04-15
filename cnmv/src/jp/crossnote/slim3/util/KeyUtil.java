/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * KeyUtil.
 * 
 * @author kilvistyle
 * @since 2009/11/28
 *
 */
public class KeyUtil {
    private KeyUtil() {
    }
    
    public static Key stringToKey(String s) {
        if (StringUtil.isEmpty(s)) return null;
        try {
            return KeyFactory.stringToKey(s);
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static String keyToString(Key key) {
        if (key == null) return null;
        return KeyFactory.keyToString(key);
    }
}

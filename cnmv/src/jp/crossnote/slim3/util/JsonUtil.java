/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;


import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * JsonUtil.
 * 
 * @author kilvistyle
 * @since 2010/01/21
 *
 */
public class JsonUtil {
    private JsonUtil() {
    }

    public static String param(String name, Object val) {
        if (val == null) {
            val = "";
        }
        return encode(name, val);
    }
    
    public static String paramExcludeNull(String name, Object val) {
        if (val == null) {
            return null;
        }
        return encode(name, val);
    }

    public static String paramExcludeFalse(String name, boolean val) {
        if (!val) {
            return null;
        }
        else {
           return "\""+name+"\":\"true\"";
        }
    }

    @SuppressWarnings("unchecked")
    private static String encode(String name, Object val) {
        if (name == null || val == null) {
            return null;
        }
        if (val instanceof IJsonable) {
            return "\""+name+"\":"+((IJsonable)val).toJSON();
        }
        if (val instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Collection c = (Collection)val;
            for (Object o : c) {
                if (o == null) {
                    continue;
                }
                if (1 < sb.length()) {
                    sb.append(",");
                }
                if (o instanceof IJsonable) {
                    sb.append(((IJsonable)o).toJSON());
                }
                else if (o instanceof Key) {
                    sb.append("\""+KeyFactory.keyToString((Key)o)+"\"");
                }
                else if (NumUtil.isNumber(o.toString()) || NumUtil.isDecimal(o.toString())) {
                    sb.append(o.toString());
                }
                else {
                    sb.append("\""+escape(o.toString())+"\"");
                }
            }
            sb.append("]");
            return "\""+name+"\":"+sb.toString();
        }
        if (val instanceof Map) {
        	StringBuilder sb = new StringBuilder();
        	Map<Object, Object> map = (Map)val;
        	sb.append("{");
        	for (Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                if (1 < sb.length()) {
                	sb.append(",");
                }
                sb.append("\""+entry.getKey().toString()+"\":");
        		Object o = entry.getValue();
                if (o instanceof IJsonable) {
                    sb.append(((IJsonable)o).toJSON());
                }
                else if (o instanceof Key) {
                    sb.append("\""+KeyFactory.keyToString((Key)o)+"\"");
                }
                else if (NumUtil.isNumber(o.toString()) || NumUtil.isDecimal(o.toString())) {
                    sb.append(o.toString());
                }
                else {
                    sb.append("\""+escape(o.toString())+"\"");
                }
        	}
        	sb.append("}");
            return "\""+name+"\":"+sb.toString();
        }
        if (val instanceof Key) {
            return "\""+name+"\":\""+KeyFactory.keyToString((Key)val)+"\"";
        }
        if (NumUtil.isNumber(val.toString()) || NumUtil.isDecimal(val.toString())) {
            return "\""+name+"\":"+val.toString();
        }
        else {
           return "\""+name+"\":\""+escape(val.toString())+"\"";
        }
    }
    
    private static String escape(String val) {
    	// 最初に\をエスケープ
    	val = StrUtil.replace(val, "\\", "\\\\");
		// CR+LFの改行コードをエスケープ
    	val = StrUtil.replace(val, "\r\n", "\\n");
		// LFの改行コードをエスケープ
		val = StrUtil.replace(val, "\n", "\\n");
		// CRの改行コードをエスケープ
    	val = StrUtil.replace(val, "\r", "\\n");
		// ダブルクォートをエスケープ
		val = StrUtil.replace(val, "\"", "\\\"");
		return val;
    }
    
    public static String model(String...params) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String param : params) {
            if (StringUtil.isEmpty(param)) {
                continue;
            }
            if (1 < sb.length()) {
                sb.append(",");
            }
            sb.append(param);
        }
        sb.append("}");
        return sb.toString();
    }
    
}

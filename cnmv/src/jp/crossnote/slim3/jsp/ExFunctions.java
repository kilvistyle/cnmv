/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.jsp;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jp.crossnote.slim3.constants.GlobalConstants;
import jp.crossnote.slim3.util.ModelUtil;

import org.slim3.jsp.Functions;
import org.slim3.util.BooleanUtil;
import org.slim3.util.DateUtil;
import org.slim3.util.RequestLocator;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;

/**
 * ExFunctions.
 * the extension Functions on slim3.
 * 
 * @author kilvistyle
 * @since 2009/11/20
 *
 */
public class ExFunctions {
	
	private ExFunctions() {
	}
    
    @SuppressWarnings("unchecked")
    public static boolean isEmpty(Object value) {
        if (value == null || "".equals(value)) return true;
        if (value instanceof Collection) return ((Collection)value).isEmpty();
        return false;
    }
    
    public static String toString(Object value) {
    	if (value == null) return "null";
    	return value.toString();
    }
    
    /**
     * Returns messages iterator
     * @return Iterator<String>
     */
    @SuppressWarnings("unchecked")
    public static Iterator<String> msgs() {
        HttpServletRequest request = RequestLocator.get();
        Map<String, String> msgs =
            (Map<String, String>) request
                .getAttribute(GlobalConstants.MESSAGE_KEY);
        if (msgs != null) {
            return msgs.values().iterator();
        }
        msgs = (Map<String, String>)request.getSession()
                .getAttribute(GlobalConstants.MESSAGE_KEY);
        if (msgs != null) {
            // セッションからメッセージを取得した場合は削除する
            request.getSession().removeAttribute(GlobalConstants.MESSAGE_KEY);
            return msgs.values().iterator();
        }
        return null;
    }
    
    /**
     * Returns the string value from converted object with HTML escape.
     * @param input
     * @return
     */
    public static String h(Object input) {
        return Functions.h(ModelUtil.valueToString(input));
    }

    /**
     * Returns the value at attribute on request.
     * 
     * @param input
     *            the input value
     * @return the escaped value
     */
    public static Object v(String propertyName) {
        return RequestLocator.get().getAttribute(propertyName);
    }

    /**
     * Returns the request value at index.
     * 
     * @param input
     *            the input value
     * @return the escaped value
     */
    public static Object vi(int index, String propertyName) {
        return vim(index, "", propertyName);
    }

    /**
     * Returns the request value at modelName and index.
     * 
     * @param input
     *            the input value
     * @return the escaped value
     */
    public static Object vim(int index, String modelName, String propertyName) {
        HttpServletRequest request = RequestLocator.get();
        String name = modelName+"["+index+"]"+propertyName;
        return request.getAttribute(name);
    }
    
    /**
     * Returns the text tag representation at index.
     * 
     * @param index  The index of model List.
     * @param propertyName The name of property on model.
     * @return the text tag representation
     */
    public static String texti(int index, String propertyName) {
        return textim(index, "", propertyName);
    }
    
    /**
     * Returns the text tag representation at modelName and index.
     * 
     * @param index  The index of model List.
     * @param modelName The name of model in List.
     * @param propertyName The name of property on model.
     * @return the text tag representation
     */
    public static String textim(int index, String modelName, String propertyName) {
        HttpServletRequest request = RequestLocator.get();
        String name = modelName+"["+index+"]"+propertyName;
        return "name=\""
            + name
            + "\" value=\""
            + Functions.h(request.getAttribute(name))
            + "\"";
    }

    public static String selecti(int index, String propertyName) {
        return selectim(index, "", propertyName);
    }
    
    public static String selectim(int index, String modelName, String propertyName) {
        String name = modelName+"["+index+"]"+propertyName;
        return "name=\""
            + name
            + "\"";
    }
    
    public static String optioni(int index, String propertyName, String value) {
        return optionim(index, "", propertyName, value);
    }
    
    public static String optionim(int index, String modelName, String propertyName, String value) {
        HttpServletRequest request = RequestLocator.get();
        String name = modelName+"["+index+"]"+propertyName;
        String s = StringUtil.toString(request.getAttribute(name));
        return "value=\""
            + Functions.h(value)
            + "\""
            + (value == null && s == null || value != null && value.equals(s)
                ? " selected=\"selected\""
                : "");
    }
    
    public static String radioi(int index, String propertyName, String value) {
        return radioim(index, "", propertyName, value);
    }

    public static String radioim(int index, String modelName, String propertyName, String value)
            throws IllegalArgumentException {
        HttpServletRequest request = RequestLocator.get();
        String name = modelName+"["+index+"]"+propertyName;
        String s = StringUtil.toString(request.getAttribute(name));
        return "name=\""
            + propertyName
            + "\" value=\""
            + Functions.h(value)
            + "\""
            + (value == null && s == null || value != null && value.equals(s)
                ? " checked=\"checked\""
                : "");
    }

    public static String checkboxi(int index, String propertyName) {
        return checkboxim(index, "", propertyName);
    }
    
    public static String checkboxim(int index, String modelName, String propertyName) {
        String name = modelName+"["+index+"]"+propertyName;
        HttpServletRequest request = RequestLocator.get();
        return "name=\""
            + name
            + "\""
            + (BooleanUtil.toPrimitiveBoolean(request.getAttribute(name))
                ? " checked=\"checked\""
                : "");
    }
    
    public static Key key(Object value) {
        if (value instanceof Key) {
        	return (Key)value;
        }
        if (value instanceof String) {
        	return ModelUtil.stringToValue(Key.class, (String)value);
        }
        return null;
    }

    public static String date(Date date, String pattern) {
        if (date == null || pattern == null) return null;
        return DateUtil.toString(date, pattern);
    }

    public static int size(String propertyName) {
        HttpServletRequest request = RequestLocator.get();
        Object value = request.getAttribute(propertyName);
        return ModelUtil.valueSize(value);
    }
    
    public static int sizei(int index, String propertyName) {
        return sizeim(index, "", propertyName);
    }
    
    public static int sizeim(int index, String modelName, String propertyName) {
        HttpServletRequest request = RequestLocator.get();
        String name = modelName+"["+index+"]"+propertyName;
        Object value = request.getAttribute(name);
        return ModelUtil.valueSize(value);
    }
    
    public static String tagDiv(Object input, String classValue, String idValue) {
    	if (input == null) {
    		return "";
    	}
    	String value = null;
    	if (input instanceof String) {
    		value = (String)input;
    	}
    	else {
    		value = input.toString();
    	}
    	return "<div"
    		+ (StringUtil.isEmpty(classValue)?"":" class=\""+classValue+"\"")
    		+ (StringUtil.isEmpty(idValue)?"":" id=\""+idValue+"\"")
    		+ ">" + value +"</div>";
    }

    public static String tagSpan(Object input, String classValue, String idValue) {
    	if (input == null) {
    		return "";
    	}
    	String value = null;
    	if (input instanceof String) {
    		value = (String)input;
    	}
    	else {
    		value = input.toString();
    	}
    	return "<span"
    		+ (StringUtil.isEmpty(classValue)?"":" class=\""+classValue+"\"")
    		+ (StringUtil.isEmpty(idValue)?"":" id=\""+idValue+"\"")
    		+ ">" + value +"</span>";
    }
}

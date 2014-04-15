/**
 * Copyright (c) 2009 kilvistyle (@kilvistyle on twitter)
 */
package jp.crossnote.slim3.controller.cn;

import java.io.IOException;
import java.io.PrintWriter;

import jp.crossnote.slim3.constants.GlobalConstants;
//import jp.crossnote.slim3.controller.helper.TokenHelper;

import org.slim3.controller.Controller;
import org.slim3.util.ApplicationMessage;
import org.slim3.util.ArrayMap;
import org.slim3.util.IntegerUtil;
import org.slim3.util.LocaleLocator;
import org.slim3.util.ThrowableUtil;

/**
 * AbstractBaseController.
 * 
 * @author kilvistyle (@kilvistyle on twitter)
 * @since 2009/12/05
 *
 */
public abstract class AbstractBaseController extends Controller {

    private ArrayMap<String, String> requestMsg = null;

    public AbstractBaseController() {
    	super();
    	// set message resource bundle for cn
    	ApplicationMessage.setBundle(GlobalConstants.CN_MESSAGE_BUNDLE, LocaleLocator.get());
    }

    /**
     * nameに関連付けられたオブジェクトがrequest,session内に存在するか判定する.
     * @param name
     * @return
     */
    protected boolean has(CharSequence name) {
    	if (name == null) return false;
    	return requestScope(name) != null;
    }
    
    /**
     * requestスコープにメッセージを登録する.
     * @param key
     * @param message
     */
    protected void addMsgOnRequest(String key, String message) {
    	requestMsg = requestMsg != null ? requestMsg : new ArrayMap<String, String>(17);
        if (requestScope(GlobalConstants.MESSAGE_KEY) == null) {
            requestScope(GlobalConstants.MESSAGE_KEY, requestMsg);
        }
        requestMsg.put(key, message);
    }
    /**
     * requestスコープのメッセージをクリアする.
     */
    protected void clearMsgOnRequest() {
    	requestMsg = requestMsg != null ? requestMsg : new ArrayMap<String, String>(17);
        requestMsg.clear();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T extends Enum> T asEnum(Class<T> enumClass, String name) {
        Object value = request.getAttribute(name);
        if (value == null) return null;
        if (value.getClass() == String.class) {
            return (T)Enum.valueOf(enumClass, (String) value);
        }
        if (value instanceof Number) {
            int ordinal = IntegerUtil.toPrimitiveInt(value);
            return (T)enumClass.getEnumConstants()[ordinal];
        }
        return (T)value;
    }

    protected Boolean asBoolean(CharSequence name) {
    	Boolean b = super.asBoolean(name);
    	return b == null ? false : b;
    }
    
    protected void download(String fileName, String text, String encoding) {
        try {
            response.setContentType("application/octet-stream; charset="+encoding);
            response.setHeader("Content-Disposition","attachment; filename="+fileName);
            PrintWriter out = response.getWriter();
            try {
                out.print(text);
                out.flush();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            ThrowableUtil.wrapAndThrow(e);
        }
    }
}

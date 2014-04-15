/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.validator;

import java.util.Map;

import org.slim3.controller.validator.AbstractValidator;
import org.slim3.controller.validator.LongTypeValidator;
import org.slim3.util.ApplicationMessage;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * KeyValidator.
 * 
 * @author kilvistyle
 * @since 2009/11/08
 *
 */
public class KeyValidator extends AbstractValidator {

    public static KeyValidator INSTANCE = new KeyValidator();
    
    private Policy policy = Policy.STRING_TO_KEY;
    
    /**
     * 
     */
    public KeyValidator() {
        super();
    }

    /**
     * @param message
     */
    public KeyValidator(String message) {
        super(message);
    }
    
    public KeyValidator(Policy policy) {
		if (policy == null) {
			throw new NullPointerException("the policy parameter is null.");
		}
		this.policy = policy;
    }
    
    public KeyValidator(Policy policy, String message) {
        super(message);
		if (policy == null) {
			throw new NullPointerException("the policy parameter is null.");
		}
		this.policy = policy;
    }

    @Override
    public String validate(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || "".equals(value)) {
            return null;
        }
        String keyString = (String)value;
        try {
        	switch (policy) {
				case STRING_TO_KEY:
					// KeyToStringでの検証を実施
		        	Key key = KeyFactory.stringToKey(keyString);
		            if (key != null) {
		                // 正常にKeyに変換できた場合
		                return null;
		            }
		            break;
				case ID_OF_KEY:
					// ID（long値）での検証を実施
					return LongTypeValidator.INSTANCE.validate(parameters, name);
			    default:
					// Nameでの検証はどの値でもOK
			    	return null;
			}
        }
        catch (Exception e) {
        }
        // Keyの変換に失敗した場合
        return ApplicationMessage.get(
        	getMessageKey(),
            getLabel(name));
        
    }

	@Override
	protected String getMessageKey() {
		return "validator.key";
	}
	
	public enum Policy {
		STRING_TO_KEY,
		ID_OF_KEY,
		NAME_OF_KEY;
	}
}

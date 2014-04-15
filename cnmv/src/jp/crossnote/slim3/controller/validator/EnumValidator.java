/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.validator;

import java.util.Map;

import org.slim3.controller.validator.AbstractValidator;
import org.slim3.util.ApplicationMessage;

/**
 * EnumValidator.
 * 
 * @author kilvistyle
 * @since 2009/11/18
 *
 */
public class EnumValidator extends AbstractValidator {

    private Class<?> enumClass;
    
    /**
     * 
     */
    public EnumValidator(Class<?> enumClass) {
        if (enumClass == null) {
            throw new NullPointerException("The enumClass is null.");
        }
        this.enumClass = enumClass;
    }

    /**
     * @param message
     */
    public EnumValidator(Class<?> enumClass, String message) {
        super(message);
        if (enumClass == null) {
            throw new NullPointerException("The enumeration is null.");
        }
        this.enumClass = enumClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String validate(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || "".equals(value)) {
            return null;
        }
        if (value instanceof String) {
            String enumName = (String)value;
            try {
                Enum<?> enumeration = Enum.valueOf((Class)enumClass,enumName);
                if (enumeration != null) {
                    return null;
                }
            }
            catch (Exception e) {
            }
        }
        else if (enumClass.isAssignableFrom(value.getClass())) {
            // Enumクラスの値の場合
            return null;
        }
        // Keyの変換に失敗した場合
        return ApplicationMessage.get(
        	getMessageKey(),
            getLabel(name), enumClass.getName());
        
    }

	@Override
	protected String getMessageKey() {
		return "validator.enum";
	}

}

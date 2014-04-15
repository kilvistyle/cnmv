/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.validator;

import java.util.Map;

import jp.crossnote.slim3.util.ModelUtil;

import org.slim3.controller.validator.AbstractValidator;
import org.slim3.util.ApplicationMessage;

/**
 * ArrayValidator.
 * 
 * @author kilvistyle
 * @since 2009/11/26
 *
 */
public class ArrayValidator extends AbstractValidator {
    
    private Class<? extends Object[]> arrType;
    
    public ArrayValidator(Class<? extends Object[]> arrType) {
        if (arrType == null) {
            throw new NullPointerException("the arrType is null.");
        }
        this.arrType = arrType;
    }

    public ArrayValidator(Class<? extends Object[]> arrType, String message) {
        super(message);
        if (arrType == null) {
            throw new NullPointerException("the arrType is null.");
        }
        this.arrType = arrType;
    }

    @Override
    public String validate(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || "".equals(value)) {
            return null;
        }
        if (arrType.isAssignableFrom(value.getClass())) {
            return null;
        }
        try {
            if (ModelUtil.stringToArrayValue(arrType, value.toString()) != null) {
                // 正常に変換できた場合
                return null;
            }
        }
        catch (Exception e) {
        }
        // ArrayTypeの変換に失敗した場合
        return ApplicationMessage.get(
            getMessageKey(),
            getLabel(name), arrType.getComponentType().getSimpleName());
    }

	@Override
	protected String getMessageKey() {
		return "validator.array";
	}

}

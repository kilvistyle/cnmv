package jp.crossnote.slim3.controller.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;

import jp.crossnote.slim3.controller.validator.ContentValidator.Type;
import jp.crossnote.slim3.controller.validator.KeyValidator.Policy;

import org.slim3.controller.validator.Validator;
import org.slim3.controller.validator.Validators;
import org.slim3.util.ApplicationMessage;
import org.slim3.util.StringUtil;

/**
 * ExValidators.
 * extension Validators on slim3.
 * 
 * @author kilvistyle
 * @since 2009/11/20
 *
 */
public class ExValidators extends Validators {
    
    private Map<String, String> labelMap = new HashMap<String, String>();
    private static final String LABEL_ROWNUMBER = "validator.rownumber";

    /**
     * @param request
     */
    public ExValidators(HttpServletRequest request) {
        super(request);
    }

    /**
     * @param parameters
     * @throws NullPointerException
     * @throws IllegalStateException
     */
    public ExValidators(Map<String, Object> parameters)
            throws NullPointerException, IllegalStateException {
        super(parameters);
    }

    /**
     * Adds the validators for Iterational parameters.
     * 
     * @param num  The number of iteration
     * @param parameterName The name of parameter 
     * @param validators The validators
     * @return Validators this instance
     * @throws NullPointerException
     */
    public Validators add(int num, String parameterName, Validator... validators)
            throws NullPointerException  {
        return add(num, "", parameterName, validators);
    }

    /**
     * Adds the validators for Iterational parameters (with model name).
     * 
     * @param num The number of iteration
     * @param modelName The name of model
     * @param parameterName The name of parameter
     * @param validators The validators
     * @return Validators this instance
     * @throws NullPointerException
     */
    public Validators add(int num, String modelName, String parameterName, Validator... validators)
            throws NullPointerException {
        if (modelName == null) {
            throw new NullPointerException("The modelName parameter is null.");
        }
        if (parameterName == null) {
            throw new NullPointerException("The parameterName parameter is null.");
        }
        String name = modelName+"["+num+"]"+parameterName;
        validatorsMap.put(name, validators);
        StringBuffer sbLabel = new StringBuffer();
        if (!StringUtil.isEmpty(modelName)) {
            sbLabel.append(getLabel(modelName)).append(" ");
        }
        labelMap.put(name,
            sbLabel
                .append(getMsg(LABEL_ROWNUMBER, ""+num+1))
                .append(getLabel(parameterName))
                .toString());
        return this;
    }

    /**
     * Validates input values.
     * 
     * @return whether input values are valid. Returns true if input values are
     *         valid.
     */
    public boolean validate() {
        boolean valid = true;
        for (int i = 0; i < validatorsMap.size(); i++) {
            String name = validatorsMap.getKey(i);
            for (Validator v : validatorsMap.get(i)) {
                String message = v.validate(parameters, name);
                if (message != null) {
                	valid = false;
                    if (labelMap.containsKey(name)) {
                        errors.put(name, replace(message, name, labelMap.get(name)));
                    }
                    else{
                        errors.put(name, message);
                    }
                    break;
                }
            }
        }
        return valid;
    }

    public KeyValidator key() {
        return KeyValidator.INSTANCE;
    }
    
    public KeyValidator key(Policy policy) {
    	return new KeyValidator(policy);
    }
    
    public KeyValidator key(String message) {
        return new KeyValidator(message);
    }

    public ContentValidator content(Type...contentTypes) {
        return new ContentValidator(contentTypes);
    }
    
    public ContentValidator content(String message, Type...contTypes) {
        return new ContentValidator(message, contTypes);
    }
    
    public EnumValidator enumType(Class<?> enumClass) {
        return new EnumValidator(enumClass);
    }
    
    public EnumValidator enumType(Class<?> enumClass, String message) {
        return new EnumValidator(enumClass, message);
    }
    
    public ArrayValidator arrayType(Class<? extends Object[]> arrType) {
        return new ArrayValidator(arrType);
    }
    
    public ArrayValidator arrayType(Class<? extends Object[]> arrType, String message) {
        return new ArrayValidator(arrType, message);
    }
    
    public CollectionValidator collectionType(
            Class<? extends Collection<?>> collectionType, Class<?> elementType) {
        return new CollectionValidator(collectionType, elementType);
    }
    
    public CollectionValidator collectionType(
            Class<? extends Collection<?>> collectionType,
            Class<?> elementType,
            String message) {
        return new CollectionValidator(collectionType, elementType, message);
    }

    /**
     * Returns the label.
     * @param name
     * @return
     */
    protected String getLabel(String name) {
    	return getMsg("label."+name);
    }
    
    /**
     * Returns the message.
     * @param name
     * @return
     */
    protected String getMsg(String name, String...val) {
        try {
            String label = ApplicationMessage.get(name, (Object[])val);
            if (label != null) {
                return label;
            }
        } catch (MissingResourceException ignore) {
        }
        return name;
    }

    protected String replace(final String text,
            final String fromText, final String toText) {

        if (text == null || fromText == null || toText == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(100);
        int pos = 0;
        int pos2 = 0;
        while (true) {
            pos = text.indexOf(fromText, pos2);
            if (pos == 0) {
                buf.append(toText);
                pos2 = fromText.length();
            } else if (pos > 0) {
                buf.append(text.substring(pos2, pos));
                buf.append(toText);
                pos2 = pos + fromText.length();
            } else {
                buf.append(text.substring(pos2));
                break;
            }
        }
        return buf.toString();
    }

}

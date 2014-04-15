/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.validator;

import java.util.Arrays;
import java.util.Map;

import jp.crossnote.slim3.util.StrUtil;

import org.slim3.controller.upload.FileItem;
import org.slim3.controller.validator.AbstractValidator;
import org.slim3.util.ApplicationMessage;
import org.slim3.util.StringUtil;

/**
 * ContentValidator.
 * 
 * @author kilvistyle
 * @since 2009/11/17
 *
 */
public class ContentValidator extends AbstractValidator {
    
    private Type[] contentTypes;
    
    /**
     * 
     */
    public ContentValidator(Type...contentTypes) {
        if (contentTypes == null || contentTypes.length == 0) {
            throw new IllegalArgumentException("The contentTypes is null or empty.");
        }
        this.contentTypes = contentTypes;
    }

    /**
     * @param message
     */
    public ContentValidator(String message, Type...contentTypes) {
        super(message);
        if (contentTypes == null || contentTypes.length == 0) {
            throw new IllegalArgumentException("The contentTypes is null or empty.");
        }
        this.contentTypes = contentTypes;
    }

    @Override
    public String validate(Map<String, Object> parameters, String name) {
        Object value = parameters.get(name);
        if (value == null || "".equals(value)) {
            return null;
        }
        String contentType = null;
        try {
            FileItem file = (FileItem)value;
            // TODO GoogleChromeだとcontentTypeが取得できない為、nullはスルーする
            contentType = file.getContentType();
            if (StringUtil.isEmpty(contentType)) return null;
            for (Type type : contentTypes) {
                if (type.matchContentType(contentType)) {
                    // 検証対象のコンテンツタイプに合致する場合
                    return null;
                }
            }
        }
        catch (Exception e) {
        }
        // コンテンツのマッチングに失敗した場合
        return ApplicationMessage.get(
        	getMessageKey(),
        	getLabel(name),
        	StrUtil.arrayToString(", ", (Object[])contentTypes),
        	contentType);
        
    }
    
    /**
     * Type.
     * 
     * @author kilvistyle
     * @since 2009/11/17
     *
     */
    public enum Type {
        JPG {
            @Override
            public boolean matchContentType(String contentType) {
                return Arrays.asList("image/jpeg","image/pjpeg")
                .contains(contentType);
            }
        },
        GIF {
            @Override
            public boolean matchContentType(String contentType) {
                return Arrays.asList("image/gif")
                .contains(contentType);
            }
        },
        PNG {
            @Override
            public boolean matchContentType(String contentType) {
                return Arrays.asList("image/x-png")
                .contains(contentType);
            }
        },
        CSV {
            @Override
            public boolean matchContentType(String contentType) {
                return Arrays.asList("text/plain","text/csv","application/octet-stream","application/vnd.ms-excel")
                .contains(contentType);
            }
        }
        ;
        abstract public boolean matchContentType(String contentType);
    }

	@Override
	protected String getMessageKey() {
		return "validator.content";
	}
}

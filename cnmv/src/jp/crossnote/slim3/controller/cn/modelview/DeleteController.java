/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.logging.Level;
import java.util.logging.Logger;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.service.ModelService;

import org.slim3.controller.Navigation;

/**
 * DeleteController.
 * 
 * @author kilvistyle
 * @since 2009/11/27
 *
 */
public class DeleteController extends AbstractBaseController {
	
	private static final Logger logger =
		Logger.getLogger(DeleteController.class.getName());

    private ModelService service = ModelService.getInstance();

    @Override
    protected Navigation run() {
        if (!validate()) {
            return redirect(basePath);
        }
        // ModelClassを取得
        Class<?> modelClass = service.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            errors.put("_modelname_", "the model is not found.");
            return forward(basePath);
        }
        try {
            service.delete(
            	modelClass,
            	asKey("_key_"),
            	(asInteger("_page_") * asInteger("_limit_")));
        }
        catch (Exception e) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, e.getMessage());
        	}
            errors.put("err", "Delete failed. (message : "+e.getMessage()+")");
            return forward(basePath);
        }
        // リダイレクト
        return redirectWith();
    }

    private Navigation redirectWith() {
    	return redirect(basePath+"?_modelname_="+asString("_modelname_")
    			+"&_page_="+asString("_page_")
    			+"&_limit_="+asString("_limit_")
    			+"&d=");
	}

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        v.add("_key_", v.key());
        return v.validate();
    }

}

/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.ArrayList;
import java.util.List;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.ExBeanUtil;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;

import org.slim3.controller.Navigation;
import org.slim3.datastore.AbstractModelRef;
import org.slim3.datastore.EntityNotFoundRuntimeException;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.InverseModelRef;
import org.slim3.datastore.ModelRef;
import org.slim3.util.BeanUtil;
import org.slim3.util.PropertyDesc;

/**
 * RefViewController.
 * 
 * @author kilvistyle
 * @since 2010/09/03
 *
 */
public class RefViewController extends AbstractBaseController {

    private ModelService service = ModelService.getInstance();

	/* (非 Javadoc)
	 * @see org.slim3.controller.Controller#run()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Navigation run() throws Exception {
        if (!validate()) {
            return redirect(basePath);
        }
        // ModelClassを取得
        Class<?> modelClass = service.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            errors.put("_modelname_", "the model is not found.");
            return forward(basePath);
        }
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo != null) {
        	// タスク実行中画面へリダイレクト
        	return redirectWith("working");
        }
        Object model = service.findByKey(modelClass, asKey("_key_"));
        if (model == null) {
            errors.put("_key_", "the key is not found. (key = "+asKey("_key_")+")");
            return forward(basePath);
        }
        // ModelRefオブジェクトを取得する
        PropertyDesc propDesc = BeanUtil.getBeanDesc(modelClass).getPropertyDesc(asString("_ref_"));
        if (propDesc == null) {
            errors.put("_ref_", "the parameter is not found. (name = "+asString("_ref_")+")");
            return forward(basePath);
        }
        Object refObject = propDesc.getValue(model);
        // 関連先のモデルデータのロード
        List modelList = null;
        // ModelRefの場合
        if (refObject instanceof ModelRef) {
        	modelList = new ArrayList();
        	Object refModel = null;
        	try {
        		refModel = ((ModelRef)refObject).getModel();
        	}
        	catch (EntityNotFoundRuntimeException e) {
        		// 参照先が存在しない場合
			}
        	if (refModel != null) {
            	modelList.add(refModel);
        	}
        }
        // InverseModelRefの場合
        else if (refObject instanceof InverseModelRef) {
        	modelList = new ArrayList();
        	Object refModel = ((InverseModelRef)refObject).getModel();
        	if (refModel != null) {
            	modelList.add(refModel);
        	}
        }
        // InverseModelListRefの場合
        else if (refObject instanceof InverseModelListRef) {
        	modelList = ((InverseModelListRef)refObject).getModelList();
        }
        
        // 関連先のモデルのプロパティリストを取得
        Class modelRefClass = ((AbstractModelRef)refObject).getModelClass();
        List<PropertyType> propertyList =
            ModelUtil.getPropertyTypes(modelRefClass);
        
        requestScope("_propertyList_", propertyList);
        requestScope("_refModelName_", modelRefClass.getName());
        requestScope("_key_", asKey("_key_"));
        requestScope("modelList", modelList);
        ExBeanUtil.copyList(modelList, request);
        // メッセージがある場合は表示
        this.showMsg();
        
        return forward("ref.jsp");
	}

    private void showMsg() {
        if (has("i")) {
        	if (asBoolean("i")) {
                addMsgOnRequest("msg", "Insert succeeded.");
        	}
        	else {
                addMsgOnRequest("msg", "Update succeeded.");
        	}
        }
        else if (has("u")) {
            addMsgOnRequest("msg", "Upload succeeded. "+asString("u")+" rows included.");
        }
        else if (has("d")) {
            addMsgOnRequest("msg", "Delete succeeded.");
        }
	}

    private Navigation redirectWith(String toUri) {
    	return redirect(toUri+"?_modelname_="+asString("_modelname_")
    			+"&_page_="+asString("_page_")
    			+"&_limit_="+asString("_limit_"));
	}

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        v.add("_key_", v.key());
        v.add("_ref_", v.required());
        return v.validate();
    }

}

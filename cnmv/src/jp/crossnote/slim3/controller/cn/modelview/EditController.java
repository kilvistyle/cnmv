/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.List;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;

import org.slim3.controller.Navigation;
import org.slim3.util.BeanUtil;
import org.slim3.util.CopyOptions;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;

/**
 * EditController.
 * 
 * @author kilvistyle
 * @since 2009/11/26
 *
 */
public class EditController extends AbstractBaseController {

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
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo != null) {
        	// タスク実行中画面へリダイレクト
        	return redirect("working?_modelname_="+modelClass.getName());
        }
        // Modelを取得
        Object model = null;
        if (StringUtil.isEmpty(asString("_key_"))) {
            model = service.newModel(asString("_modelname_"));
        }
        else {
            model = service.findByKey(modelClass, asKey("_key_"));
        }
        if (model == null) {
            errors.put("_modelname_", "the model is not found. please try again.");
            return forward(basePath);
        }
        // このモデルのプロパティリストを取得
        List<PropertyType> propertyList =
            ModelUtil.getPropertyTypes(model.getClass());
        requestScope("_propertyList_", propertyList);
        // 構造体のプロパティをリクエストスコープに展開
        putStructuralParameter(propertyList, model);
        // プライマリキーの生成ポリシーデフォルト値を設定
        if (!has("_pk_policy_")) requestScope("_pk_policy_", "stk");
        // modelのプロパティ内容をrequestスコープに設定
        BeanUtil.copy(model, request, new CopyOptions().excludeEmptyString());
        return forward("edit.jsp");
    }
    
    private void putStructuralParameter(List<PropertyType> propertyList, Object model) {
    	for (PropertyType pt : propertyList) {
    		// User型の場合
    		if (pt.isUser()) {
    			User user = (User)ModelUtil.getValue(model, pt.getName());
    			if (user != null) {
        			requestScope(pt.getUserEmailKey(), user.getEmail());
        			requestScope(pt.getUserAuthDomainKey(), user.getAuthDomain());
        			requestScope(pt.getUserUserIdKey(), user.getUserId());
        			requestScope(pt.getUserFederatedIdentityKey(), user.getFederatedIdentity());
    			}
    		}
    		// GeoPt型の場合
    		if (pt.isGeoPt()) {
    			GeoPt geoPt = (GeoPt)ModelUtil.getValue(model, pt.getName());
    			if (geoPt != null) {
    				requestScope(pt.getGeoPtLatitudeKey(), geoPt.getLatitude());
    				requestScope(pt.getGeoPtLongitudeKey(), geoPt.getLongitude());
    			}
    		}
    	}
	}

	private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        if (!StringUtil.isEmpty(asString("_key_"))) {
            v.add("_key_", v.key());
        }
        return v.validate();
    }

}

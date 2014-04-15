/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.List;

import jp.crossnote.slim3.controller.Paging;
import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.AppProperties;
import jp.crossnote.slim3.util.ExBeanUtil;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;
import jp.crossnote.slim3.util.S3ModelLoader;

import org.slim3.controller.Navigation;
import org.slim3.util.AppEngineUtil;
import org.slim3.util.BeanUtil;
import org.slim3.util.StringUtil;

import com.google.appengine.api.users.UserServiceFactory;

/**
 * IndexController.
 * 
 * @author kilvistyle
 * @since 2009/11/25
 *
 */
public class IndexController extends AbstractBaseController {
    
    private ModelService service = ModelService.getInstance();

    @SuppressWarnings("unchecked")
    @Override
    protected Navigation run() {
        // ローカル環境判定を設定
        requestScope("isDev", AppEngineUtil.isDevelopment());
        // モデルが指定されていない場合はトップページを表示
        if (StringUtil.isEmpty(asString("_modelname_"))) {
            // モデルクラスの取得
            List<Class<?>> modelClassList = getS3ModelClassList();
            requestScope("modelClassList", modelClassList);
            requestScope("_logoutURL_", UserServiceFactory.getUserService().createLogoutURL(getRequestRootURL()));
            return forward("index.jsp");
        }
        // ModelClassを取得
        Class<?> modelClass = service.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            errors.put("_modelname_", "the model is not found.");
            return forward("index.jsp");
        }
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo != null) {
            // タスク実行中画面へリダイレクト
            return redirectWith("working");
        }
        // このモデルのプロパティリストを取得
        List<PropertyType> propertyList =
            ModelUtil.getPropertyTypes(modelClass);
        // ページングを取得
        Paging paging = getPaging(service.countAt(modelClass));
        // このモデルのデータを取得
        List modelList =
            service.findAll(
                modelClass,
                paging.getOffset(),
                paging.getLimitInt());
        requestScope("_propertyList_", propertyList);
        requestScope("_page_", paging.getPage());
        requestScope("_limit_", paging.getLimit());
        requestScope("modelList", modelList);
        BeanUtil.copy(paging, request);
        ExBeanUtil.copyList(modelList, request);
        // メッセージがある場合は表示
        this.showMsg();
        
        return forward("index.jsp");
    }

    private String getRequestRootURL() {
        StringBuffer sb = request.getRequestURL();
        int uriLength = request.getRequestURI().length();
        return sb.delete(sb.length()-uriLength, sb.length()).toString();
    }
    
    private List<Class<?>> getS3ModelClassList() {
        boolean useCache = asBoolean("reload") == null ? true : !asBoolean("reload");
        List<Class<?>> models = S3ModelLoader.load(useCache);
        String[] extraModelNames = AppProperties.getExternalModelNames();
        if (extraModelNames != null && 0 < extraModelNames.length) {
            for (String name : extraModelNames) {
                Class<?> model = service.getModelClass(name);
                if (model != null && !models.contains(model)) {
                    models.add(model);
                }
            }
        }
        return models;
    }

    private Navigation redirectWith(String toUri) {
        return redirect(toUri+"?_modelname_="+asString("_modelname_")
                +"&_page_="+asString("_page_")
                +"&_limit_="+asString("_limit_"));
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

    private Paging getPaging(int size) {
        // Pagingを初期化
        Paging paging = new Paging(0, AppProperties.CNS3_VIEWER_LISTCOUNT, size);
        // Pagingに現在のパラメータをコピー
        if (!StringUtil.isEmpty(asString("_page_"))) paging.setPage(asString("_page_"));
        if (!StringUtil.isEmpty(asString("_limit_"))) paging.setLimit(asString("_limit_"));
        return paging;
    }

}

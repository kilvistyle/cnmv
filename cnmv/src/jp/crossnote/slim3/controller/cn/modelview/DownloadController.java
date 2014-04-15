/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.List;

import jp.crossnote.slim3.controller.Paging;
import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.AppProperties;
import jp.crossnote.slim3.util.CsvUtil;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;
import jp.crossnote.slim3.util.StrUtil;

import org.slim3.controller.Navigation;
import org.slim3.util.StringUtil;

/**
 * DownloadController.
 * 
 * @author kilvistyle
 * @since 2009/11/30
 *
 */
public class DownloadController extends AbstractBaseController {

    private ModelService service = ModelService.getInstance();

    @SuppressWarnings("unchecked")
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
        // CSV出力用StringBuffer生成
        StringBuffer csv = new StringBuffer();
        // ヘッダ出力
        csv.append(writeCsvHeader(propertyList)).append(StrUtil.LS);
        // モデル出力
        for (Object model : modelList) {
            csv.append(writeCsvModel(propertyList, model)).append(StrUtil.LS);
        }
        // ダウンロード実行
        download(asString("_modelname_")+".csv", csv.toString(), AppProperties.CNS3_ENCODING_CSV);
        return null;
    }

    private Paging getPaging(int size) {
    	// Pagingを初期化
    	Paging paging = new Paging(0, AppProperties.CNS3_VIEWER_LISTCOUNT, size);
        // Pagingに現在のパラメータをコピー
    	if (!StringUtil.isEmpty(asString("_page_"))) paging.setPage(asString("_page_"));
    	if (!StringUtil.isEmpty(asString("_limit_"))) paging.setLimit(asString("_limit_"));
    	return paging;
    }

    /**
     * modelのプロパティ名称をCSV形式の文字列で取得する.
     * @param propertyList
     * @return
     */
    private String writeCsvHeader(List<PropertyType> propertyList) {
        StringBuffer sb = new StringBuffer();
        for (PropertyType pt : propertyList) {
        	if (pt.isUnsupported()) {
        		continue;
        	}
            if (0 < sb.length()) {
                sb.append(",");
            }
            sb.append(CsvUtil.escapeCsvElement(pt.getName()));
        }
        return sb.toString();
    }
    
    /**
     * modelのプロパティ値をCSV形式の文字列で取得する.
     * @param propertyList
     * @param model
     * @return
     */
	private String writeCsvModel(List<PropertyType> propertyList, Object model) {
        StringBuffer sb = new StringBuffer();
        for (PropertyType pt : propertyList) {
        	if (pt.isUnsupported()) {
        		continue;
        	}
            if (0 < sb.length()) {
                sb.append(",");
            }
            Object value = ModelUtil.getValue(model, pt.getName());
            if (value == null || "".equals(value)) {
                continue;
            }
            sb.append(CsvUtil.escapeCsvElement(ModelUtil.valueToString(value)));
        }
        return sb.toString();
    }
    
    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        return v.validate();
    }
}

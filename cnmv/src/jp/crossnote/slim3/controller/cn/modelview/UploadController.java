/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.List;
import java.util.Map.Entry;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.CnmvFileData;
import jp.crossnote.slim3.model.enumeration.CnmvTask;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.FileService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;

import org.slim3.controller.Navigation;
import org.slim3.controller.upload.FileItem;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * UploadController.
 * 
 * @author kilvistyle
 * @since 2009/12/01
 *
 */
public class UploadController extends AbstractBaseController {

    @Override
    protected Navigation run() {
        if (!validate()) {
            return redirect(basePath);
        }
        // ModelClassを取得
        ModelService service = ModelService.getInstance();
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
        	return redirectWith();
        }
        // アップロードファイルが指定されている場合
        if (isSubmit() && validateUploadFile()) {
            // ファイル取得
            FileItem csvFile = requestScope("csvFile");
    		// CSVファイルをDatastoreに保存して、FileDataを取得
    		CnmvFileData data = FileService.getInstance().upload(csvFile);
    		// アップロードタスク情報を生成
            CnmvTaskInfo newTaskInfo = createTaskInfo(modelClass, data);
            // タスク情報を保存
            taskInfoService.insertTaskInfo(newTaskInfo);
            // レコード保存用のTQを準備
            this.addTaskQueue(newTaskInfo);
        	// このモデルのカーソルを削除
        	service.removeCursor(modelClass, 0);
        	// タスク実行中画面へリダイレクト
        	return redirectWith();
        }
        // アップロードファイルが指定されていない場合はアップロード画面へ
        else {
            // このモデルのプロパティリストを取得
            List<PropertyType> propertyList =
                ModelUtil.getPropertyTypes(modelClass);
            requestScope("_propertyList_", propertyList);
            return forward("upload.jsp");
        }
    }

    private Navigation redirectWith() {
    	return redirect("working?_modelname_="+asString("_modelname_")
    			+"&_page_="+asString("_page_")
    			+"&_limit_="+asString("_limit_"));
	}

	private void addTaskQueue(CnmvTaskInfo taskInfo) {
    	// 次のTQを登録
        TaskOptions taskOptions =
        	TaskOptions.Builder.withUrl("/cn/modelview/task/uploadTask");
        for (Entry<String, String> entry : taskInfo.getParams().entrySet()) {
            taskOptions.param(entry.getKey(), entry.getValue());
        }
        QueueFactory.getDefaultQueue().add(taskOptions);
	}
	
    @SuppressWarnings("unchecked")
	private CnmvTaskInfo createTaskInfo(Class modelClass, CnmvFileData file) {
		// CSVアップロードタスク情報を生成して返却
		CnmvTaskInfo taskInfo = new CnmvTaskInfo();
		taskInfo.setTaskName(CnmvTask.TASK_UPLOAD);
		taskInfo.setTargetModelName(modelClass.getName());
		taskInfo.setFileKey(file.getKey());
		taskInfo.setMessage("Upload task started... Please wait until ending.");
		taskInfo.getParams().put("_modelname_", asString("_modelname_"));
		taskInfo.getParams().put("policy", asString("policy"));
		taskInfo.getParams().put("startIndex", ""+0);
		taskInfo.getParams().put("skipCount", ""+0);
		return taskInfo;
	}

	private boolean isSubmit() {
    	// upキーがある場合はサブミットボタンが押下されていると判断
    	return isPost() && has("up");
    }

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        return v.validate();
    }
    
    private boolean validateUploadFile() {
        ExValidators v = new ExValidators(request);
        v.add("policy", v.required());
        v.add("csvFile", v.required());
//        v.add("csvFile", v.required(), v.content(Type.CSV));
        return v.validate();
    }
    
}

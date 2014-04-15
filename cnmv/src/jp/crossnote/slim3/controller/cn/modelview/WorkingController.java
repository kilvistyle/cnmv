/**
 * Copyright (c) 2010 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.Map.Entry;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.enumeration.CnmvTask;
import jp.crossnote.slim3.model.enumeration.TaskState;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.FileService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.DateUtil;

import org.slim3.controller.Navigation;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * WorkingController.
 * 
 * @author kilvistyle
 * @since 2010/06/03
 *
 */
public class WorkingController extends AbstractBaseController {

	private static final int EXPIRE_SECONDS = 3 * 60 * 1000;
	
	/* (非 Javadoc)
	 * @see org.slim3.controller.Controller#run()
	 */
	@Override
	protected Navigation run() throws Exception {
        if (!validate()) {
            return redirect(basePath);
        }
        // ModelClassを取得
        Class<?> modelClass = ModelService.getInstance().getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            errors.put("_modelname_", "the model is not found.");
            return forward(basePath);
        }
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo == null) {
            return redirectWith("index");
        }
        // リスタート指示の場合
        if (has("restart") && asBoolean("restart")) {
        	// タスクが停止中の場合
        	if (taskInfo.isStopped()) {
        		// アップロードタスクを再開
        		if (CnmvTask.TASK_UPLOAD.equals(taskInfo.getTaskName())) {
        			// タスク状態を再び処理中へ
        			taskInfo.setState(TaskState.WORKING);
        			taskInfoService.registTaskInfo(taskInfo);
                    // レコード保存用のTQを準備
                    TaskOptions taskOptions =
                    	TaskOptions.Builder.withUrl("/cn/modelview/task/uploadTask");
                    for (Entry<String, String> entry : taskInfo.getParams().entrySet()) {
                    	taskOptions.param(entry.getKey(), entry.getValue());
                    }
                    QueueFactory.getDefaultQueue().add(taskOptions);
                    // リダイレクトして終了
                	return redirectWith("working");
        		}
        		else if (CnmvTask.TASK_DELETE_ALL.equals(taskInfo.getTaskName())) {
        			// タスク状態を再び処理中へ
        			taskInfo.setState(TaskState.WORKING);
        			taskInfoService.registTaskInfo(taskInfo);
        	        // レコード全削除用のTQを準備
        	        TaskOptions taskOptions =
        	        	TaskOptions.Builder.withUrl("/cn/modelview/task/deleteAllTask");        	        for (Entry<String, String> entry : taskInfo.getParams().entrySet()) {
        	        	taskOptions.param(entry.getKey(), entry.getValue());
        	        }
        	        QueueFactory.getDefaultQueue().add(taskOptions);
                    // リダイレクトして終了
                	return redirectWith("working");
        		}
        	}
        }
        // タスク実行中で最終実行時間が期限切れの場合
        if (taskInfo.isWorking() &&
        	DateUtil.getSystemDate().getTime() > taskInfo.getUpdateDate().getTime()+EXPIRE_SECONDS) {
    		// 処理をエラーにする
    		taskInfo.setState(TaskState.FAILED);
    		taskInfo.setMessage("the task is time out.");
			taskInfoService.registTaskInfo(taskInfo);
        	taskInfoService.cancelStopTask(modelClass);
            // リダイレクトして終了
        	return redirectWith("working");
        }
        // タスク終了指示の場合
        else if (has("finish") && asBoolean("finish")) {
        	// ファイルがある場合はファイルを削除
        	if (taskInfo.getFileKey() != null) {
        		FileService.getInstance().delete(taskInfo.getFileKey());
        	}
        	// タスク情報を削除
        	taskInfoService.delete(taskInfo);
        	taskInfoService.cancelStopTask(modelClass);
        	// リダイレクト
            return redirectWith("index");
        }
        // ポーズ指示の場合
        else if (has("pause") && asBoolean("pause")) {
        	// 一時停止指示
        	taskInfoService.stopTask(modelClass);
        	// リダイレクト
        	return redirectWith("working");
        }
        
		return forward("working.jsp");
	}

    private Navigation redirectWith(String toUri) {
    	return redirect(toUri+"?_modelname_="+asString("_modelname_")
    			+"&_page_="+asString("_page_")
    			+"&_limit_="+asString("_limit_"));
	}

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        return v.validate();
    }
    
}

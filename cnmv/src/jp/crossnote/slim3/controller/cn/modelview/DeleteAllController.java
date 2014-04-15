/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.Map.Entry;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.enumeration.CnmvTask;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;

import org.slim3.controller.Navigation;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * DeleteAllController.
 * 
 * @author kilvistyle
 * @since 2010/06/02
 *
 */
public class DeleteAllController extends AbstractBaseController {

	/* (非 Javadoc)
	 * @see org.slim3.controller.Controller#run()
	 */
	@Override
	protected Navigation run() throws Exception {
        if (!validate()) {
            return redirect(basePath);
        }
        ModelService service = ModelService.getInstance();
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
		// 全レコード削除タスク情報を生成
        CnmvTaskInfo newTaskInfo = createTaskInfo(modelClass);
        // タスク情報を保存
        taskInfoService.insertTaskInfo(newTaskInfo);
        // レコード全削除用のTQを準備
        TaskOptions taskOptions =
        	TaskOptions.Builder.withUrl("/cn/modelview/task/deleteAllTask");
        for (Entry<String, String> entry : newTaskInfo.getParams().entrySet()) {
        	taskOptions.param(entry.getKey(), entry.getValue());
        }
        QueueFactory.getDefaultQueue().add(taskOptions);
    	// このモデルのカーソルを削除
    	service.removeCursor(modelClass, 0);
    	// タスク実行中画面へリダイレクト
    	return redirectWith();
	}

    private Navigation redirectWith() {
    	return redirect("working?_modelname_="+asString("_modelname_")
    			+"&_page_="+asString("_page_")
    			+"&_limit_="+asString("_limit_"));
	}

    @SuppressWarnings("unchecked")
	private CnmvTaskInfo createTaskInfo(Class modelClass) {
		// CSVアップロードタスク情報を生成して返却
		CnmvTaskInfo taskInfo = new CnmvTaskInfo();
		taskInfo.setTaskName(CnmvTask.TASK_DELETE_ALL);
		taskInfo.setTargetModelName(modelClass.getName());
		taskInfo.setMessage("Delete all task started... Please wait until ending.");
		taskInfo.getParams().put("_modelname_", asString("_modelname_"));
		taskInfo.getParams().put("startIndex", ""+0);
		return taskInfo;
	}

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        return v.validate();
    }

}

/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview.ajax;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.enumeration.TaskState;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.DateUtil;
import jp.crossnote.slim3.util.JsonUtil;

import org.slim3.controller.Navigation;

/**
 * TaskStateController.
 * 
 * @author kilvistyle
 * @since 2010/06/21
 *
 */
public class TaskStateController extends AbstractBaseController {

	private static final int EXPIRE_SECONDS = 3 * 60 * 1000;
	
	/* (非 Javadoc)
	 * @see org.slim3.controller.Controller#run()
	 */
	@Override
	protected Navigation run() throws Exception {
        if (!validate()) {
            return null;
        }
        // ModelClassを取得
        Class<?> modelClass = ModelService.getInstance().getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            return null;
        }
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo == null) {
            return null;
        }
        // タスク実行中で最終実行時間が期限切れの場合
        if (taskInfo.isWorking() &&
        	DateUtil.getSystemDate().getTime() > taskInfo.getUpdateDate().getTime()+EXPIRE_SECONDS) {
    		// 処理をエラーにする
    		taskInfo.setState(TaskState.FAILED);
    		taskInfo.setMessage("the task is time out.");
			taskInfoService.registTaskInfo(taskInfo);
        	taskInfoService.cancelStopTask(modelClass);
        }
        // JSON形式の戻り値を生成
        String json =
        	JsonUtil.model(
    	        JsonUtil.param("state", taskInfo.getState().toString()),
    	        JsonUtil.param("stopFlg", taskInfoService.isStopTask(modelClass)),
    	        JsonUtil.param("message", taskInfo.getMessage())
    	    );
        // タスク状態をJSON形式で返却
		download("task", json, "UTF-8");
		return null;
	}
	
    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        return v.validate();
    }
    
}

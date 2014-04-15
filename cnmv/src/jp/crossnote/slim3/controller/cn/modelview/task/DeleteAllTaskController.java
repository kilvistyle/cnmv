/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview.task;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.enumeration.TaskState;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;

import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.apphosting.api.DeadlineExceededException;

/**
 * DeleteAllTaskController.
 * 
 * @author kilvistyle
 * @since 2010/06/02
 *
 */
public class DeleteAllTaskController extends AbstractBaseController {
	
	private static final Logger logger =
		Logger.getLogger(DeleteAllTaskController.class.getName());
	
	private static final int DATASTORE_LOAD_LIMIT = 499;

	// DEEをなるべく発生させないため余裕を持って最大10secずつ実行する
	private static final long DEADLINE_SEC = 10 * 1000;
	private static final long TIMEOUT_DELAY_MS = 10 * 1000;
	
	/* (非 Javadoc)
	 * @see org.slim3.controller.Controller#run()
	 */
	@Override
	protected Navigation run() throws Exception {
		long deadlineTime = System.currentTimeMillis() + DEADLINE_SEC;
        if (!validate()) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv delete all task failed. the modelName is required.");
        	}
        	return null;
        }
        ModelService service = ModelService.getInstance();
        Class<?> modelClass = service.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv delete all task failed. the modelName \"" +
        				asString("_modelname_") +"\" is not found.");
        	}
        	return null;
        }
        // タスク情報を取得
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (!this.isValidTask(taskInfo)) {
        	// 有効な状態ではないので終了
        	return null;
        }
        // 削除するカレント行番号を更新
        int startIndex = asInteger("startIndex");
    	taskInfo.getParams().put("startIndex", ""+startIndex);
        // 一時停止指示の場合
        if (taskInfoService.isStopTask(modelClass)) {
        	// タスク状態を一時停止に変更
        	taskInfo.setState(TaskState.STOPPED);
        	taskInfo.setMessage("Delete task stopped. but already "
        			+startIndex+" entities deleted.");
        	taskInfoService.registTaskInfo(taskInfo);
        	// 一時停止指示を受け付けたのでフラグを削除
        	taskInfoService.cancelStopTask(modelClass);
        	if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "Delete task stopped. ("+ asString("_modelname_")+")");
        	}
        	return null;
        }
        // 実行中の場合
        else {
        	if (0 < startIndex) {
            	taskInfo.setMessage(startIndex+" entities deleted... Please wait until ending.");
        	}
    		taskInfoService.registTaskInfo(taskInfo);
        }
        // 処理上限（DATASTORE_LOAD_LIMIT）＋１件を読み込んでおく
        List<Key> keys =
        	service.findKeyList(modelClass, DATASTORE_LOAD_LIMIT + 1);
        int index = 0;
        // 処理上限、あるいは取得件数の小さい方をlimitとする
        int limit = DATASTORE_LOAD_LIMIT < keys.size() ? DATASTORE_LOAD_LIMIT : keys.size();
        // 読み込んだ分、または制限時間まで削除ループ
        try {
            for (; index < limit && System.currentTimeMillis() < deadlineTime; index++, startIndex++) {
            	try {
                	// エンティティを削除
                	Datastore.delete(keys.get(index));
            	}
            	catch (Exception e) {
            		if (e instanceof DeadlineExceededException) throw e;
            		if (e instanceof DatastoreTimeoutException) throw e;
                	if (logger.isLoggable(Level.WARNING)) {
                		logger.log(Level.WARNING, e.getMessage());
                	}
            		// 削除に失敗した場合
            		this.setErrorState(taskInfo,
            			"but already deleted "+startIndex+" entities. " +
            		    "Check error message : "+e.toString());
            		return null;
    			}
            }
            // 全てのレコード削除が完了していない場合
            if (index < keys.size()) {
            	// 次のTQを登録
                this.addNextTaskQueue(startIndex, taskInfo, 0);
                if (logger.isLoggable(Level.INFO)) {
            		logger.log(Level.INFO, "CNMV executing delete all task."+
        				"It already deleted "+startIndex+" entities on "+
        				asString("_modelname_") +", and It will execute next sequence...");
            	}
            }
            else {
            	// タスク情報を更新
            	taskInfo.setState(TaskState.SUCCEEDED);
            	taskInfo.getParams().put("startIndex", ""+startIndex);
            	taskInfo.setMessage("delete all task succeeded. "+startIndex+" entities deleted.");
            	// 上書き更新
            	taskInfoService.registTaskInfo(taskInfo);
                if (logger.isLoggable(Level.INFO)) {
            		logger.log(Level.INFO, "CNMV Delete all task succeeded!! " +
        				startIndex+" entities deleted.");
            	}
            }
        }
        catch (DeadlineExceededException e) {
        	if (logger.isLoggable(Level.SEVERE)) {
        		logger.log(Level.SEVERE, e.getMessage());
        	}
        	// DEEの場合は再実行するためのTQを登録
            this.addNextTaskQueue(startIndex, taskInfo, 0);
            if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "CNMV DeleteAllTask caught DeadlineExceededException."+
        				"It already deleted "+startIndex+" entities on "+
        				asString("_modelname_") +", and It will execute next sequence...");
        	}
		}
        catch (DatastoreTimeoutException e) {
        	if (logger.isLoggable(Level.SEVERE)) {
        		logger.log(Level.SEVERE, e.getMessage());
        	}
        	// DTEの場合は少し間を置いてからTQを実行
            this.addNextTaskQueue(startIndex, taskInfo, TIMEOUT_DELAY_MS);
            if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "CNMV DeleteAllTask caught DatastoreTimeoutException."+
        				"It already deleted "+startIndex+" entities on "+
        				asString("_modelname_") +", and It will execute next sequence after "
        				+TIMEOUT_DELAY_MS+"ms...");
        	}
		}
		return null;
	}

	private void addNextTaskQueue(int index, CnmvTaskInfo taskInfo, long countdownMillis) {
    	// 次のTQを登録
        TaskOptions taskOptions =
        	TaskOptions.Builder.withUrl("/cn/modelview/task/deleteAllTask");
        for (Entry<String, String> entry : taskInfo.getParams().entrySet()) {
        	if ("startIndex".equals(entry.getKey())) {
            	taskOptions.param("startIndex", ""+index);
        	}
        	else {
            	taskOptions.param(entry.getKey(), entry.getValue());
        	}
        }
        if (0 < countdownMillis) {
        	taskOptions.countdownMillis(countdownMillis);
        }
        QueueFactory.getDefaultQueue().add(taskOptions);
	}
	
    private boolean isValidTask(CnmvTaskInfo taskInfo) {
        // タスク情報がない場合
    	if (taskInfo == null) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv delete all task failed. task info not found.");
        	}
        	return false;
        }
    	// タスクが実行中じゃない場合
    	if (!TaskState.WORKING.equals(taskInfo.getState())) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv delete all task failed. task state is not \"WORKING\".");
        	}
    		return false;
    	}
		return true;
	}

    private void setErrorState(CnmvTaskInfo taskInfo, String message) throws Exception {
    	if (logger.isLoggable(Level.WARNING)) {
    		logger.log(Level.WARNING, "Delete all task failed. "+message);
    	}
    	taskInfo.setState(TaskState.FAILED);
    	taskInfo.setMessage("Delete all task failed. "+message);
    	CnmvTaskInfoService.getInstance().registTaskInfo(taskInfo);
	}

	private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        v.add("startIndex", v.required(), v.integerType());
        return v.validate();
    }

}

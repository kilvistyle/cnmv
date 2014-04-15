/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.model.enumeration.TaskState;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.FileService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.AppProperties;
import jp.crossnote.slim3.util.ArrayConverter;
import jp.crossnote.slim3.util.BlobConverter;
import jp.crossnote.slim3.util.CategoryConverter;
import jp.crossnote.slim3.util.CollectionConverter;
import jp.crossnote.slim3.util.CsvUtil;
import jp.crossnote.slim3.util.EmailConverter;
import jp.crossnote.slim3.util.EntityExistsException;
import jp.crossnote.slim3.util.GeoPtConverter;
import jp.crossnote.slim3.util.LinkConverter;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PhoneNumberConverter;
import jp.crossnote.slim3.util.PostalAddressConverter;
import jp.crossnote.slim3.util.PropertyType;
import jp.crossnote.slim3.util.RatingConverter;
import jp.crossnote.slim3.util.SerializableConverter;
import jp.crossnote.slim3.util.ShortBlobConverter;
import jp.crossnote.slim3.util.TextConverter;
import jp.crossnote.slim3.util.UserConverter;

import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.ModelRef;
import org.slim3.util.BeanUtil;
import org.slim3.util.CopyOptions;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.apphosting.api.DeadlineExceededException;

/**
 * UploadTaskController.
 * 
 * @author kilvistyle
 * @since 2010/06/03
 *
 */
public class UploadTaskController extends AbstractBaseController {

	private static final Logger logger =
		Logger.getLogger(UploadTaskController.class.getName());

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
        		logger.log(Level.WARNING, "UploadTask failed. the parameters invalid. "
        			+this.getValidatorErrorMessages());
        	}
            return null;
        }
        ModelService modelService = ModelService.getInstance();
        // ModelClassを取得
        Class<?> modelClass = modelService.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "UploadTask failed. \""+ asString("_modelname_"+"\" is not found."));
        	}
            return null;
        }
        // アップロードされたファイルを取得
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (!this.isValidTask(taskInfo)) {
        	// 有効な状態ではないので終了
        	return null;
        }
        // 取り込むカレント行番号を更新
        int index = asInteger("startIndex");
        int skipCount = asInteger("skipCount");
    	taskInfo.getParams().put("startIndex", ""+index);
    	taskInfo.getParams().put("skipCount", ""+skipCount);
        // 一時停止指示の場合
        if (taskInfoService.isStopTask(modelClass)) {
        	// タスク状態を一時停止に変更
        	taskInfo.setState(TaskState.STOPPED);
        	taskInfo.setMessage("Upload task stopped. already "+index+" rows uploaded."+addSkipMsg(skipCount));
        	taskInfoService.registTaskInfo(taskInfo);
        	// 一時停止指示を受け付けたのでフラグを削除
        	taskInfoService.cancelStopTask(modelClass);
        	if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "Upload task stopped. ("+ asString("_modelname_")+")");
        	}
        	return null;
        }
        // 実行中の場合
        else {
        	if (0 < index) {
            	taskInfo.setMessage(index+" rows uploaded..."+addSkipMsg(skipCount));
        	}
    		taskInfoService.registTaskInfo(taskInfo);
        }
        
        // CSVファイルから要素MAPリストを取得
        List<Map<String, String>> elementMapList = null;
        try {
        	FileService fileService = FileService.getInstance();
        	byte[] data = fileService.getBytes(fileService.getData(taskInfo.getFileKey()));
        	String csv = new String(data, AppProperties.CNS3_ENCODING_CSV);
        	elementMapList = CsvUtil.csvToElementMaps(csv);
        }
        catch (Exception e) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, e.getMessage());
        	}
        	this.setErrorState(taskInfo, e.toString());
            return null;
		}
        // プロパティリストを取得
        List<PropertyType> propertyList =
        	ModelUtil.getPropertyTypes(modelClass);
        // プロパティリストからCopyOptionsを生成
        CopyOptions co = createCopyOptions(propertyList);
        int limit = elementMapList.size();
        try {
            for (; index < limit && System.currentTimeMillis() < deadlineTime; index++) {
            	// アップロードの１行分のデータを取得
            	Map<String, String> elementMap = elementMapList.get(index);
            	// プライマリキーのencodedKeyを取得
        		String encodedKey =
        			elementMap.get(propertyList.get(0).getName());
            	// 既存データ有無判定
            	boolean isAlreadyExist = false;
            	// トランザクション開始
        		Transaction tx = Datastore.beginTransaction();
        		// ===== get or create Model =====
            	Object model = null;
        		try {
                    // PKが指定されていれば取得を試みる
            		if (!StringUtil.isEmpty(encodedKey)) {
            			model =
            				Datastore.getOrNull(tx, modelClass, Datastore.stringToKey(encodedKey));
            		}
            		// 取得できた場合
        			if (model != null) {
        				isAlreadyExist = true;
        			}
        			// 取得できなかった場合は生成
        			else {
        				model = ModelUtil.newModel(modelClass);
        			}
        		}
        		catch (Exception e) {
            		if (tx.isActive()) tx.rollback();
            		// DEEの場合はそのままスロー
            		if (e instanceof DeadlineExceededException) throw e;
            		// それ以外の例外の場合はリトライ不可なのでエラー終了
                	if (logger.isLoggable(Level.WARNING)) {
                		logger.log(Level.WARNING, e.getMessage());
                	}
                	String msg = null;
                	if (0 < index) {
                		msg = index+" rows included."+addSkipMsg(skipCount)+" but can't including at row "+(index+1)+" and after. : "+e.toString();
                	}
                	else {
                        msg = " can't including at row 1st. : "+e.toString();
                	}
                	this.setErrorState(taskInfo, msg);
                    return null;
				}
        		// ===== copy properties =====
            	try {
        			// アップロードデータをmodelに反映
        			this.copyProperties(elementMap, model, propertyList, co);
            	}
            	catch (Exception e) {
            		if (tx.isActive()) tx.rollback();
            		// DEEの場合はそのままスロー
            		if (e instanceof DeadlineExceededException) throw e;
            		// それ以外の例外の場合はリトライ不可なのでエラー終了
                	if (logger.isLoggable(Level.WARNING)) {
                		logger.log(Level.WARNING, e.getMessage());
                	}
                	String msg = null;
                	if (0 < index) {
                		msg = index+" rows included."+addSkipMsg(skipCount)+" but can't including at row "+(index+1)+" and after. : "+e.toString();
                	}
                	else {
                        msg = "invalid format at row 1st. : "+e.toString();
                	}
                	this.setErrorState(taskInfo, msg);
                    return null;
    			}
        		// ===== Insert or Update =====
                try {
                	// Keyが設定されていない場合
                	Key key = ModelUtil.getKey(model);
                	if (key == null) {
                		// PKがない場合は採番して更新
                		ModelUtil.setKey(model, Datastore.allocateId(model.getClass()));
                		Datastore.put(tx, model);
                		tx.commit();
                	}
                	// Keyが設定されているが存在しない場合
                	// または、上書き更新モードの場合
                	else if (!isAlreadyExist || "CU".equals(asString("policy"))) {
                		// 新規or上書き追加
                		Datastore.put(tx, model);
                		tx.commit();
                	}
                	//　重複スキップモードの場合
                	else if ("CS".equals(asString("policy"))) {
                		// スキップ
                		tx.rollback();
                		// スキップカウンタを加算
                		++skipCount;
                        if (logger.isLoggable(Level.INFO)) {
                    		logger.log(Level.INFO, "CNMV UploadTask skipped entity("+
                    			key+") because already existed. (total "+skipCount+" rows skipped)");
                    	}
                	}
                	// 新規追加モードの場合
                	else {
                        throw new EntityExistsException("the entity already exists. " +
                        		"model = "+model.getClass()+", key = "+key);
                	}
                }
                catch (Exception e) {
                	// ロールバック
            		if (tx.isActive()) tx.rollback();
            		// DEE,DTEの場合はそのままスロー
            		if (e instanceof DeadlineExceededException) throw e;
            		if (e instanceof DatastoreTimeoutException) throw e;
            		// それ以外の例外の場合はリトライ不可なのでエラー終了
                	if (logger.isLoggable(Level.WARNING)) {
                		logger.log(Level.WARNING, e.getMessage());
                	}
                	String msg = index+" rows uploaded."+addSkipMsg(skipCount)+" but can't include at row "+
                		(index+1)+"th and after. : "+e.toString();
                	this.setErrorState(taskInfo, msg);
                	return null;
    			}
            }
            // 全てのレコードがアップロード完了していない場合
            if (index < limit) {
            	// 次のTQを登録
                this.addNextTaskQueue(index, skipCount, taskInfo, 0);
                if (logger.isLoggable(Level.INFO)) {
            		logger.log(Level.INFO, "CNMV executing UploadTask. "+
            				"It uploaded "+index+" rows"+addSkipMsg(skipCount)+" on "+
            				asString("_modelname_") +", and It will execute next sequence...");
            	}
            }
            // 全てのアップロードが終わった場合
            else {
            	taskInfo.setState(TaskState.SUCCEEDED);
            	taskInfo.getParams().put("startIndex", ""+index);
            	taskInfo.getParams().put("skipCount", ""+skipCount);
            	taskInfo.setMessage("upload task succeeded. "+index+" rows included."+addSkipMsg(skipCount));
            	// 上書き更新
            	taskInfoService.registTaskInfo(taskInfo);
                if (logger.isLoggable(Level.INFO)) {
            		logger.log(Level.INFO, "CNMV UploadTask succeeded!! " +
            				index+" rows included."+addSkipMsg(skipCount));
            	}
            }
        }
        catch (DeadlineExceededException e) {
        	if (logger.isLoggable(Level.SEVERE)) {
        		logger.log(Level.SEVERE, e.getMessage());
        	}
        	// DEEの場合は未処理の現在のIndexから再実行するためのTQを登録
            this.addNextTaskQueue(index, skipCount, taskInfo, 0);
            if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "CNMV UploadTask caught DeadlineExceededException. "+
        				"It uploaded "+index+" rows"+addSkipMsg(skipCount)+" on "+
        				asString("_modelname_") +", and It will execute next sequence...");
        	}
		}
        catch (DatastoreTimeoutException e) {
        	if (logger.isLoggable(Level.SEVERE)) {
        		logger.log(Level.SEVERE, e.getMessage());
        	}
        	// DTEの場合は少し間を置いてからTQを実行
            this.addNextTaskQueue(index, skipCount, taskInfo, TIMEOUT_DELAY_MS);
            if (logger.isLoggable(Level.INFO)) {
        		logger.log(Level.INFO, "CNMV UploadTask caught DatastoreTimeoutException. "+
        				"It uploaded "+index+" rows"+addSkipMsg(skipCount)+" on "+
        				asString("_modelname_") +", and It will execute next sequence after "
        				+TIMEOUT_DELAY_MS+"ms...");
        	}
		}
		return null;
	}
	
	private String addSkipMsg(int skipCount) {
		return 0 < skipCount ? " (but "+skipCount+" rows skipped)" : "";
	}

	private void addNextTaskQueue(int index, int skipCount, CnmvTaskInfo taskInfo, long countdownMillis) {
    	// 次のTQを登録
        TaskOptions taskOptions =
        	TaskOptions.Builder.withUrl("/cn/modelview/task/uploadTask");
        for (Entry<String, String> entry : taskInfo.getParams().entrySet()) {
        	if ("startIndex".equals(entry.getKey())) {
            	taskOptions.param("startIndex", ""+index);
        	}
        	else if ("skipCount".equals(entry.getKey())) {
            	taskOptions.param("skipCount", ""+skipCount);
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
	
	private String getValidatorErrorMessages() {
		StringBuffer msg = new StringBuffer();
		if (errors == null || errors.isEmpty()) {
			return msg.toString();
		}
		for (String err : errors.values()) {
			if (0 < msg.length()) {
				msg.append("\n and ");
			}
			msg.append(err);
		}
		return msg.toString();
	}

	@SuppressWarnings("unchecked")
	private CopyOptions createCopyOptions(List<PropertyType> propertyList) {
        // プロパティのコピー準備
        CopyOptions co =
            new CopyOptions()
        		.dateConverter(AppProperties.CNS3_DATE_PATTERN)
                .excludeEmptyString();
        // 除外プロパティ名のリスト
        List<String> excludePropNames = new ArrayList<String>();
        // プロパティの型に応じてコンバータを指定
        for (PropertyType pt : propertyList) {
            // ModelRefプロパティの場合
            if (pt.isModelRef()) {
            	// ModelRefはBeanUtil.copyでは取り込めないため除外（別途コピーする）
                excludePropNames.add(pt.getName());
            }
            // User型の場合
            else if (pt.isUser()) {
            	co = co.converter(UserConverter.getInstance(), pt.getName());
            }
            // GeoPt型の場合
            else if (pt.isGeoPt()) {
            	co = co.converter(GeoPtConverter.getInstance(), pt.getName());
            }
            // 配列の場合
            else if (pt.isArray() && pt.isEditable()) {
                co = co.converter(new ArrayConverter((Class)pt.getType()), pt.getName());
            }
            // コレクションの場合
            else if (pt.isCollection() && pt.isEditable()) {
                co = co.converter(
                    new CollectionConverter(
                        (Class)pt.getType(),
                        (Class)pt.getGenericType()),
                        pt.getName());
            }
            // ShortBlob型の場合
            else if (pt.isShortBlob()) {
            	co = co.converter(ShortBlobConverter.getInstance(), pt.getName());
            }
            // Blob型の場合
            else if (pt.isBlob()) {
            	co = co.converter(BlobConverter.getInstance(), pt.getName());
            }
            // Category型の場合
            else if (pt.isCategory()) {
            	co = co.converter(CategoryConverter.getInstance(), pt.getName());
            }
            // Email型の場合
            else if (pt.isEmail()) {
                co = co.converter(EmailConverter.getInstance(), pt.getName());
            }
            // Link型の場合
            else if (pt.isLink()) {
            	co = co.converter(LinkConverter.getInstance(), pt.getName());
            }
            // Text型の場合
            else if (pt.isText()) {
                co = co.converter(TextConverter.getInstance(), pt.getName());
            }
            // PhoneNumber型の場合
            else if (pt.isPhoneNumber()) {
                co = co.converter(PhoneNumberConverter.getInstance(), pt.getName());
            }
            // PostalAddress型の場合
            else if (pt.isPostalAddress()) {
                co = co.converter(PostalAddressConverter.getInstance(), pt.getName());
            }
            // Rating型の場合
            else if (pt.isRating()) {
                co = co.converter(RatingConverter.getInstance(), pt.getName());
            }
            // Serializableオブジェクト型の場合
            else if (pt.isSerializable()) {
            	co = co.converter(SerializableConverter.getInstance(), pt.getName());
            }
        }
    	// コピー対象外のプロパティ名を設定
        co = co.exclude(excludePropNames.toArray(new String[]{}));
        return co;
	}

	@SuppressWarnings("unchecked")
	private void copyProperties(Map<String, String> elementMap, Object model,
			List<PropertyType> propertyList, CopyOptions co) {
        // プロパティの型に応じてコンバータを指定
        for (PropertyType pt : propertyList) {
        	// アップロードデータに該当のプロパティが指定されていない場合
        	if (!elementMap.keySet().contains(pt.getName())) {
        		// このプロパティは何もしない
        		continue;
        	}
            // ModelRefプロパティの場合
        	else if (pt.isModelRef()) {
                // 代わりにここで入力されたリレーションKey情報を設定
                ModelRef mRef = (ModelRef)ModelUtil.getValue(model, pt.getName());
                if (StringUtil.isEmpty(elementMap.get(pt.getName()))) {
                	// 未入力の場合はリレーション解除
                	mRef.setModel(null);
                }
                else {
                	// 入力されている場合は設定
                	mRef.setKey(Datastore.stringToKey(elementMap.get(pt.getName())));
                }
            }
            // それ以外のプロパティの場合で未入力の場合
            else if (StringUtil.isEmpty(elementMap.get(pt.getName()))) {
            	// nullを設定する
            	ModelUtil.setValue(model, pt.getName(), null);
            }
        }
        // ModelにelementMapの内容をコピー
        BeanUtil.copy(elementMap, model, co);
	}

    private void setErrorState(CnmvTaskInfo taskInfo, String message) throws Exception {
    	if (logger.isLoggable(Level.WARNING)) {
    		logger.log(Level.WARNING, "UploadTask failed. "+message);
    	}
    	taskInfo.setState(TaskState.FAILED);
    	taskInfo.setMessage("UploadTask failed. "+message);
    	CnmvTaskInfoService.getInstance().registTaskInfo(taskInfo);
	}

    private boolean isValidTask(CnmvTaskInfo taskInfo) {
        // タスク情報がない場合
    	if (taskInfo == null) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv Upload task failed. task info not found.");
        	}
        	return false;
        }
    	// タスクが実行中じゃない場合
    	if (!TaskState.WORKING.equals(taskInfo.getState())) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, "cnmv Upload task failed. task state is not \"WORKING\".");
        	}
    		return false;
    	}
		return true;
	}

	private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        v.add("policy", v.required());
        v.add("startIndex", v.required(), v.integerType());
        return v.validate();
    }
    
}

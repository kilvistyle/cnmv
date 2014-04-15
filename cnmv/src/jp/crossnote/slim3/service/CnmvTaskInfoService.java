/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.service;

import java.util.ConcurrentModificationException;

import jp.crossnote.slim3.constants.GlobalConstants;
import jp.crossnote.slim3.meta.CnmvTaskInfoMeta;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.util.DateUtil;
import jp.crossnote.slim3.util.EntityExistsException;
import jp.crossnote.slim3.util.ModelUtil;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;
import org.slim3.memcache.Memcache;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

/**
 * CnmvTaskInfoService.
 * 
 * @author kilvistyle
 * @since 2010/06/03
 *
 */
public class CnmvTaskInfoService {

	private static CnmvTaskInfoService instance = null;
	private CnmvTaskInfoService() {
	}
	
	public static final CnmvTaskInfoService getInstance() {
		if (instance == null) {
			instance = new CnmvTaskInfoService();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public CnmvTaskInfo getTaskInfo(Class modelClass) {
		return Datastore.getOrNull(CnmvTaskInfoMeta.get(), CnmvTaskInfo.createKey(modelClass.getName()));
	}
	
	public void insertTaskInfo(CnmvTaskInfo taskInfo) {
        taskInfo.setUpdateDate(DateUtil.getSystemDate());
		Transaction tx = Datastore.beginTransaction();
		try {
			Datastore.get(tx, taskInfo.getKey());
            throw new EntityExistsException("the entity already exists. (" + taskInfo.getKey() +")");
		}
		catch (EntityNotFoundRuntimeException e) {
			try {
				Datastore.put(tx, taskInfo);
				tx.commit();
			}
			catch (Exception e2) {
				if (tx.isActive()) {
					tx.rollback();
				}
			}
		}
	}
	
	public void registTaskInfo(CnmvTaskInfo taskInfo) throws Exception {
        taskInfo.setUpdateDate(DateUtil.getSystemDate());
        Transaction tx = Datastore.beginTransaction();
		Datastore.put(tx, taskInfo);
		tx.commit();
	}
	
    public void delete(Key key, Long version, Object model) throws EntityNotFoundRuntimeException {
        Transaction tx = Datastore.beginTransaction();
        try {
            if (version != null) {
                Datastore.get(tx, model.getClass(), key, version);
            }
            else {
            	Datastore.get(tx, model.getClass(), key);
            }
            Datastore.delete(tx, key);
            tx.commit();
        }
        catch (EntityNotFoundRuntimeException e) {
            // 存在しない場合はロールバックして終了
            if (tx.isActive()) {
            	tx.rollback();
            }
            throw e;
        }
        catch (ConcurrentModificationException e) {
            // 追い越し更新となる場合はロールバックして終了
            if (tx.isActive()) {
            	tx.rollback();
            }
            throw e;
        }
    }
    
    public void delete(Object model)
    	throws EntityNotFoundRuntimeException, ConcurrentModificationException {
    	delete(ModelUtil.getKey(model), null, model);
    }
	
	@SuppressWarnings("unchecked")
	public boolean isStopTask(Class modelClass) {
		Boolean stopFlg = Memcache.get(GlobalConstants.CNMV_TASK_PAUSE_KEY+modelClass.getName());
		return Boolean.TRUE.equals(stopFlg);
	}
	
	@SuppressWarnings("unchecked")
	public void stopTask(Class modelClass) {
		Memcache.put(GlobalConstants.CNMV_TASK_PAUSE_KEY+modelClass.getName(), Boolean.TRUE);
	}
	
	@SuppressWarnings("unchecked")
	public void cancelStopTask(Class modelClass) {
		Memcache.delete(GlobalConstants.CNMV_TASK_PAUSE_KEY+modelClass.getName());
	}
}

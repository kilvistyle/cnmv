/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jp.crossnote.slim3.constants.GlobalConstants;
import jp.crossnote.slim3.util.EntityExistsException;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PropertyType;

import org.slim3.controller.ControllerConstants;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;
import org.slim3.datastore.ModelMetaUtil;
import org.slim3.datastore.ModelRef;
import org.slim3.datastore.S3QueryResultList;
import org.slim3.memcache.Memcache;
import org.slim3.util.BeanDesc;
import org.slim3.util.BeanUtil;
import org.slim3.util.ClassUtil;
import org.slim3.util.PropertyDesc;
import org.slim3.util.ServletContextLocator;
import org.slim3.util.StringUtil;
import org.slim3.util.WrapRuntimeException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

/**
 * ModelService.
 * 
 * @author kilvistyle
 * @since 2009/11/26
 *
 */
public class ModelService {
	
	private static ModelService instance = null;
	private ModelService () {
	}
	
	public static ModelService getInstance() {
		if (instance == null) {
			instance = new ModelService();
		}
		return instance;
	}

    public <T> Class<T> getModelClass(String modelName) {
        // アプリケーションパッケージ→cns3パッケージ→パッケージ指定なしの順にModelクラスを走査
        String[] packages = 
            new String[]{
        		getRootPackage()+"model.",
        		getDefaultCoolPackage()+"model.",
        		getRootPackage()+"shared.model.",
        		getCNS3RootPackage()+"model.",
                ""
            };
        return getModelClass(modelName, packages);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getModelClass(String modelName, String...packages) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<T> clazz = null;
        for (String pkg : packages) {
            try {
            	clazz = (Class<T>) Class.forName(pkg+modelName, true, loader);
            }
            catch (Throwable t) {
            }
        }
        if (clazz != null && ModelMetaUtil.isModelClass(clazz)) {
        	return clazz;
        }
        return null;
    }

    private String getRootPackage() {
        String root = ServletContextLocator.get()
                .getInitParameter(ControllerConstants.ROOT_PACKAGE_KEY);
        if (StringUtil.isEmpty(root)) {
        	return "";
        }
        return root+".";
    }
    
    private String getDefaultCoolPackage() {
    	String root = getRootPackage();
    	String cool = ControllerConstants.DEFAULT_COOL_PACKAGE;
    	if (StringUtil.isEmpty(cool)) {
    		return root;
    	}
    	return root+cool+".";
    }
    
    private String getCNS3RootPackage() {
    	return GlobalConstants.ROOT_PACKAGE+".";
    }
    
    public List<PropertyType> getPropertyTypes(Class<?> modelClass) {
        return ModelUtil.getPropertyTypes(modelClass);
    }

    public <T> List<T> findAll(Class<T> modelClass, int offset, int limit)
    	throws IllegalStateException {
//        return Datastore.query(modelClass)
//        	.offset(offset)
//        	.limit(limit)
//        	.asList();
    	return this.findAllByCursor(modelClass, offset, limit);
    }

    /**
     * 
     * @param modelClass
     * @param offset
     * @param limit
     * @return
     */
    private <T> S3QueryResultList<T> findAllByCursor(Class<T> modelClass, int offset, int limit) throws IllegalStateException {
    	S3QueryResultList<T> resultList = null;
    	// 最初から検索する場合
    	if (offset == 0) {
    		setCursor(modelClass, 0, "");
    		resultList = Datastore.query(modelClass)
	        	.limit(limit)
	        	.asQueryResultList();
    	}
    	// offset指定されたindexから検索する場合
    	else {
        	String cursor = getCursor(modelClass, offset);
        	if (!StringUtil.isEmpty(cursor)) {
        		resultList = Datastore.query(modelClass)
    	    		.encodedStartCursor(cursor)
    	        	.limit(limit)
    	        	.asQueryResultList();
        	}
    	}
    	// 正常に検索できた場合
    	if (resultList != null) {
    		// 次のクエリのカーソルをキャッシュしておく
    		if (resultList.hasNext()) {
        		setCursor(modelClass, (offset+limit), resultList.getEncodedCursor());
    		}
    		return resultList;
    	}
    	// 指定されたoffsetのカーソルがキャッシュに無い場合
    	if ((offset % limit)!=0) {
    		// 取得単位（limit）と指定offset位置が不整合な場合はエラー
    		throw new IllegalStateException("the cursor do not ready. (cursorIndex="+offset+")");
    	}
    	// 指定されたoffsetまでのカーソルをキャッシュ
    	for (int index = limit; index <= offset; index+=limit) {
    		String indexCursor = getCursor(modelClass, index);
    		// このindexのカーソルがない場合
    		if (indexCursor == null) {
    			// 検索してキャッシュ
    			S3QueryResultList<T> preResultList =
    				this.findAllByCursor(modelClass, (index-limit), limit);
    			// このindexの要素がない場合は空のリストを返却
    			if (!preResultList.hasNext()) {
    				return new S3QueryResultList<T>(new ArrayList<T>(),"","","",false);
    			}
    		}
    	}
    	// カーソルキャッシュのある状態で検索
    	return this.findAllByCursor(modelClass, offset, limit);
    }

    public String getCursor(Class<?> modelClass, int offset) {
    	SortedMap<Integer, String> cursorMap =
    		Memcache.get(GlobalConstants.CNMV_PAGING_CURSOR_KEY+modelClass.getName());
    	if (cursorMap == null) {
    		return null;
    	}
    	return cursorMap.get(Integer.valueOf(offset));
    }
    
    protected void setCursor(Class<?> modelClass, int offset, String cursor) {
    	SortedMap<Integer, String> cursorMap =
    		Memcache.get(GlobalConstants.CNMV_PAGING_CURSOR_KEY+modelClass.getName());
    	if (cursorMap == null) {
    		cursorMap = new TreeMap<Integer, String>();
    	}
    	cursorMap.put(offset, cursor);
    	Memcache.put(GlobalConstants.CNMV_PAGING_CURSOR_KEY+modelClass.getName(), cursorMap);
    }
    
    public void removeCursor(Class<?> modelClass, int offset) {
    	SortedMap<Integer, String> cursorMap =
    		Memcache.get(GlobalConstants.CNMV_PAGING_CURSOR_KEY+modelClass.getName());
    	if (cursorMap == null) {
    		return;
    	}
    	for (Iterator<Integer> ite = cursorMap.keySet().iterator(); ite.hasNext();) {
    		if (offset <= ite.next()) {
    			ite.remove();
    		}
    	}
    	Memcache.put(GlobalConstants.CNMV_PAGING_CURSOR_KEY+modelClass.getName(), cursorMap);
    }
    
    @SuppressWarnings("unchecked")
	public List<Key> findKeyList(Class modelClass, int limit) {
    	return Datastore.query(modelClass.getSimpleName())
    		.limit(limit)
    		.asKeyList();
    }
    
    @SuppressWarnings("unchecked")
	public int countAt(Class modelClass) {
    	return Datastore.query(modelClass).count();
    }
    
    public <T> T findByKey(Class<T> modelClass, Key key) {
        return Datastore.getOrNull(modelClass, key);
    }

    public <T> T newModel(String modelName) {
        Class<T> modelClass = getModelClass(modelName);
        if (modelClass == null) return null;
        return ClassUtil.newInstance(modelClass);
    }

    @SuppressWarnings("unchecked")
    public <M> M update(Key key, M model) {
    	Transaction tx = Datastore.beginTransaction();
        try {
        	Object m = Datastore.get(tx, model.getClass(), key);
            BeanUtil.copy(model, m);
            copyRelationshipKey(model, m);
            Datastore.put(tx, m);
            tx.commit();
            return (M)m;
        }
        catch (Exception e) {
        	if (tx.isActive()) {
        		tx.rollback();
        	}
            throw new WrapRuntimeException(e.getMessage(),e);
        }
    }
    
    /**
     * モデル内のModelRefの参照先をコピーする
     * @param src
     * @param dest
     */
	@SuppressWarnings("unchecked")
	private void copyRelationshipKey(Object src, Object dest) {
    	BeanDesc bd = BeanUtil.getBeanDesc(src.getClass());
    	for (int size = bd.getPropertyDescSize(), i = 0; i < size; i++) {
    		PropertyDesc pd = bd.getPropertyDesc(i);
    		// ModelRefプロパティの場合
    		if (ModelRef.class.isAssignableFrom(pd.getPropertyClass())) {
    			// リレーション先のモデル、またはキーを設定
    			ModelRef srcRef = (ModelRef)pd.getValue(src);
    			ModelRef destRef = (ModelRef)pd.getValue(dest);
    			// キーが設定されている場合
    			if (srcRef.getKey() != null) {
    				destRef.setKey(srcRef.getKey());
    			}
    			// モデルが設定されている場合
    			else if (srcRef.getModel() != null) {
    				destRef.setModel(srcRef.getModel());
    			}
    			// 指定されていない場合
    			else {
    				destRef.setModel(null);
    			}
    		}
    	}
    }
    
    public <M> M insert(M model) throws EntityExistsException {
        // Keyを取得
        Key key = ModelUtil.getKey(model);
        Transaction tx = Datastore.beginTransaction();
        if (key != null && key.isComplete()) {
        	// Key指定がある場合
            try {
                // このKeyの存在確認
            	Datastore.get(tx, key);
                throw new EntityExistsException("the entity already exists." +
                		"model = "+model.getClass()+", key = "+key);
            }
            catch (EntityNotFoundRuntimeException e) {
                // 存在しない場合のみ後続のput処理へ
            	Datastore.put(tx, model);
            	tx.commit();
            	removeCursor(model.getClass(), 0);
                return model;
            }
            catch (Exception e) {
            	if (tx.isActive()) {
            		tx.rollback();
            	}
            	if (e instanceof EntityExistsException) {
            		throw (EntityExistsException)e;
            	}
                throw new WrapRuntimeException(e.getMessage(),e);
            }
        }
        else {
        	// Key指定がない場合
        	try {
        		Datastore.put(tx, model);
        		tx.commit();
            	removeCursor(model.getClass(), 0);
                return model;
        	}
            catch (Exception e) {
            	if (tx.isActive()) {
            		tx.rollback();
            	}
                throw new WrapRuntimeException(e.getMessage(),e);
            }
        }
    }
    
    public boolean delete(Class<?> modelClass, Key key) {
    	return this.delete(modelClass, key, 0);
    }

    public boolean delete(Class<?> modelClass, Key key, int offset) {
    	Transaction tx = Datastore.beginTransaction();
        try {
        	Datastore.delete(tx, Datastore.get(tx, key).getKey());
        	tx.commit();
        	removeCursor(modelClass, offset);
            return true;
        }
        catch (Exception e) {
        	if (tx.isActive()) {
        		tx.rollback();
        	}
            throw new WrapRuntimeException(e.getMessage(),e);
        }
    }
    
}

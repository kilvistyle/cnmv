package jp.crossnote.slim3.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.crossnote.slim3.meta.CnmvFileDataFragmentMeta;
import jp.crossnote.slim3.meta.CnmvFileDataMeta;
import jp.crossnote.slim3.model.CnmvFileData;
import jp.crossnote.slim3.model.CnmvFileDataFragment;

import org.slim3.controller.upload.FileItem;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;
import org.slim3.util.ByteUtil;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

public class FileService {

    private static final int FRAGMENT_SIZE = 900000;

    private CnmvFileDataMeta d = CnmvFileDataMeta.get();

    private CnmvFileDataFragmentMeta f = CnmvFileDataFragmentMeta.get();
    
    private static FileService instance = null;
    
    private FileService() {
    }
    
    public static FileService getInstance() {
    	if (instance == null) {
    		instance = new FileService();
    	}
    	return instance;
    }

    public CnmvFileData getData(Key key) {
        try {
            return Datastore.get(d, key);
        }
        catch (EntityNotFoundRuntimeException e) {
            return null;
        }
    }

    public List<CnmvFileData> getDataList(List<Key> keyList) {
        try {
            return Datastore.get(d, keyList);
        }
        catch (EntityNotFoundRuntimeException e) {
            return null;
        }
    }

    public List<CnmvFileData> getDataList() {
        return Datastore.query(d).asList();
    }

    public CnmvFileData upload(FileItem formFile) {
        if (formFile == null) {
            return null;
        }
        return this.upload(formFile.getFileName(), formFile.getContentType(), formFile.getData());
    }
    
    public CnmvFileData upload(String fileName, String contentType, byte[] bytes) {
        if (StringUtil.isEmpty(fileName) || bytes == null) {
            return null;
        }
        List<Object> models = new ArrayList<Object>();
        CnmvFileData data = new CnmvFileData();
        models.add(data);
        data.setKey(Datastore.allocateId(d));
        data.setContentType(contentType);
        data.setFileName(fileName);
        data.setLength(bytes.length);
        byte[][] bytesArray = ByteUtil.split(bytes, FRAGMENT_SIZE);
        Iterator<Key> keys =
            Datastore
                .allocateIds(data.getKey(), f, bytesArray.length)
                .iterator();
        for (byte[] fragmentData : bytesArray) {
            CnmvFileDataFragment fragment = new CnmvFileDataFragment();
            models.add(fragment);
            fragment.setKey(keys.next());
            fragment.setBytes(fragmentData);
            data.getFragmentKeyList().add(fragment.getKey());
        }
        Transaction tx = Datastore.beginTransaction();
        for (Object model : models) {
        	Datastore.put(tx, model);
        }
        tx.commit();
        return data;
    }

    public byte[] getBytes(CnmvFileData cnmvFileData) {
        if (cnmvFileData == null) {
            throw new NullPointerException(
                "The fileData parameter is null.");
        }
        List<CnmvFileDataFragment> fragmentList =
            Datastore.get(f, cnmvFileData.getFragmentKeyList());
        byte[][] bytesArray = new byte[fragmentList.size()][0];
        for (int i = 0; i < fragmentList.size(); i++) {
            bytesArray[i] = fragmentList.get(i).getBytes();
        }
        return ByteUtil.join(bytesArray);
    }

    public void delete(Key key) {
        List<Key> deleteKeys = new ArrayList<Key>();
        CnmvFileData data = this.getData(key);
        if (data == null) {
            return;
        }
        deleteKeys.add(data.getKey());
        deleteKeys.addAll(data.getFragmentKeyList());
        Transaction tx = Datastore.beginTransaction();
        Datastore.delete(tx, deleteKeys);
        tx.commit();
    }
}
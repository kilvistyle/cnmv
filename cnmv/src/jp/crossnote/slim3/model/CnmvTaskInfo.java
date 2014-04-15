/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.crossnote.slim3.model.enumeration.CnmvTask;
import jp.crossnote.slim3.model.enumeration.TaskState;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.Model;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.Key;

/**
 * CnmvTaskInfo.
 * 
 * @author kilvistyle
 * @since 2010/06/03
 *
 */
@Model(schemaVersion = 1)
public class CnmvTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Attribute(primaryKey = true)
    private Key key;
    @Attribute(version = true)
    private Long version;
    
    private CnmvTask taskName;
    private String targetModelName;
    @Attribute(lob=true)
    private Map<String, String> params = new HashMap<String, String>();
    private Key fileKey;
    private Date updateDate;
    private TaskState state = TaskState.WORKING;
    @Attribute(lob=true)
    private String message;

    /**
     * Returns the key.
     *
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key
     *            the key
     */
    public void setKey(Key key) {
        this.key = key;
        this.targetModelName = key == null ? null : key.getName();
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
	 * @return taskName
	 */
	public CnmvTask getTaskName() {
		return taskName;
	}

	/**
	 * @param taskName セットする taskName
	 */
	public void setTaskName(CnmvTask taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return targetModelName
	 */
	public String getTargetModelName() {
		return targetModelName;
	}

	/**
	 * @param targetModelName セットする targetModelName
	 */
	public void setTargetModelName(String modelName) {
		this.targetModelName = modelName;
		this.key = createKey(modelName);
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @param params セットする params
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * @return fileKey
	 */
	public Key getFileKey() {
		return fileKey;
	}

	/**
	 * @param fileKey セットする fileKey
	 */
	public void setFileKey(Key fileKey) {
		this.fileKey = fileKey;
	}

	/**
	 * @return updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate セットする updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return state
	 */
	public TaskState getState() {
		return state;
	}

	/**
	 * @param state セットする state
	 */
	public void setState(TaskState upState) {
		this.state = upState;
	}

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message セットする message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isWorking() {
		return TaskState.WORKING.equals(this.state);
	}
	
	public boolean isStopped() {
		return TaskState.STOPPED.equals(this.state);
	}
	
	public boolean isSucceeded() {
		return TaskState.SUCCEEDED.equals(this.state);
	}
	
	public boolean isFailed() {
		return TaskState.FAILED.equals(this.state);
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CnmvTaskInfo other = (CnmvTaskInfo) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
    
    public static final Key createKey(String modelName) {
    	return StringUtil.isEmpty(modelName) ?
    			null : Datastore.createKey(CnmvTaskInfo.class, modelName);
    }

}

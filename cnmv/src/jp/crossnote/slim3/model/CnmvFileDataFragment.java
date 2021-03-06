package jp.crossnote.slim3.model;

import java.io.Serializable;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

/**
 * CnmvFileDataFragment.
 * 
 * @author kilvistyle (thanks slim3demo)
 * @since 2009/11/22
 * 
 */
@Model(schemaVersion = 1)
public class CnmvFileDataFragment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Attribute(primaryKey = true)
    private Key key;

    @Attribute(lob = true)
    private byte[] bytes;

    /**
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * @return the array of bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes
     *            the array of bytes
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

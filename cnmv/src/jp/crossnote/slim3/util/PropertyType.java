/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.crossnote.slim3.controller.SelectBox;

import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.InverseModelRef;
import org.slim3.datastore.ModelRef;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

/**
 * PropertyType.
 * 
 * @author kilvistyle
 * @since 2009/11/25
 *
 */
public class PropertyType implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // too many elements of array or collection.
    public static String SUFIX_OF_TOO_MANY_ELEMENTS = ".tooMany";
    
    // org.slim3.datastore.ModelRef property
    public static String SUFIX_OF_MODELREF_KEY = ".key";
    // com.google.appengine.api.datastore.Text property
    public static String SUFIX_OF_TEXT_VALUE = ".value";
    // com.google.appengine.api.users.User properties
    public static String SUFIX_OF_USER_EMAIL = ".email";
    public static String SUFIX_OF_USER_AUTHDOMAIN = ".authDomain";
    public static String SUFIX_OF_USER_USERID = ".userId";
    public static String SUFIX_OF_USER_FEDERATEDIDENTITY = ".federatedIdentity";
    // com.google.appengine.api.datastore.Blob property
    public static String SUFIX_OF_BLOB_BYTES = ".bytes";
    // com.google.appengine.api.datastore.Category property
    public static String SUFIX_OF_CATEGORY = ".category";
    // com.google.appengine.api.datastore.Email property
    public static String SUFIX_OF_EMAIL = ".email";
    // com.google.appengine.api.datastore.GeoPt properties
    public static String SUFIX_OF_GEOPT_LATITUDE = ".latitude";
    public static String SUFIX_OF_GEOPT_LONGITUDE = ".longitude";
    // com.google.appengine.api.datastore.Link property
    public static String SUFIX_OF_LINK_VALUE = ".value";
    // com.google.appengine.api.datastore.PhoneNumber property
    public static String SUFIX_OF_PHONENUMBER = ".number";
    // com.google.appengine.api.datastore.PostalAddress property
    public static String SUFIX_OF_POSTALADDRESS = ".address";
    // com.google.appengine.api.datastore.Rating property
    public static String SUFIX_OF_RATING = ".rating";
    
    private Class<?> clazz;
    private String name;
    private boolean isPrimaryKey;
    private boolean isVersion;
    private boolean isLob;
    private Class<?> genericType;
    
    public PropertyType(Class<?> clazz, String name) {
        if (clazz == null) {
            throw new NullPointerException("the clazz parameter is null.");
        }
        this.clazz = clazz;
        this.name = name;
    }

    /**
     * プロパティの型を取得する.
     * @return
     */
    public Class<?> getType() {
        return clazz;
    }
    
    /**
     * プロパティが文字列であるか判定する.
     * @return
     */
    public boolean isString() {
        return clazz == String.class;
    }
    
    /**
     * プロパティがBoolean型であるか判定する.
     * @return
     */
    public boolean isBoolean() {
        return clazz == Boolean.class || clazz == boolean.class;
    }
    
    /**
     * プロパティがEnum型であるか判定する.
     * @return
     */
    public boolean isEnum() {
        return Enum.class.isAssignableFrom(clazz);
    }
    
    /**
     * プロパティが日付型であるか判定する.
     * @return
     */
    public boolean isDate() {
        return Date.class.isAssignableFrom(clazz);
    }
    
    /**
     * プロパティがKey型であるか判定する.
     * @return
     */
    public boolean isKey() {
        return Key.class.isAssignableFrom(clazz);
    }
    
    /**
     * プロパティが整数型であるか判定する.
     * @return
     */
    public boolean isNumber() {
        return Byte.class == clazz
        	|| Short.class == clazz
        	|| Integer.class == clazz
        	|| Long.class == clazz
        	|| byte.class == clazz
        	|| short.class == clazz
        	|| int.class == clazz
        	|| long.class ==clazz;
    }
    
    /**
     * プロパティが不動小数点型であるか判定する.
     * @return
     */
    public boolean isDecimal() {
    	return Float.class == clazz
    		|| Double.class == clazz
    		|| BigDecimal.class == clazz
    		|| float.class == clazz
    		|| double.class == clazz;
    }
    
    /**
     * プロパティが配列であるか判定する.
     * @return
     */
    public boolean isArray() {
        return clazz.isArray();
    }
    
    /**
     * プロパティがコレクション型であるか判定する.
     * @return
     */
    public boolean isCollection() {
        return List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがText型であるか判定する.
     * @return
     */
    public boolean isText() {
    	return Text.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがShortBlob型であるか判定する.
     * @return
     */
	public boolean isShortBlob() {
		return ShortBlob.class.isAssignableFrom(clazz);
	}
    
    /**
     * プロパティがBlob型であるか判定する.
     * @return
     */
    public boolean isBlob() {
    	return Blob.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがUser型であるか判定する.
     * @return
     */
    public boolean isUser() {
    	return User.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがEmail型であるか判定する.
     * @return
     */
    public boolean isEmail() {
    	return Email.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがCategory型であるか判定する.
     * @return
     */
    public boolean isCategory() {
    	return Category.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがGeoPt型であるか判定する.
     * @return
     */
    public boolean isGeoPt() {
    	return GeoPt.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがPhoneNumber型であるか判定する.
     * @return
     */
    public boolean isPhoneNumber() {
    	return PhoneNumber.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがPostalAddress型であるか判定する.
     * @return
     */
    public boolean isPostalAddress() {
    	return PostalAddress.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがRating型であるか判定する.
     * @return
     */
    public boolean isRating() {
    	return Rating.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがLink型であるか判定する.
     * @return
     */
    public boolean isLink() {
    	return Link.class.isAssignableFrom(clazz);
    }

    /**
     * プロパティがシリアライズするべきオブジェクト型であるか判定する.
     * @return
     */
	public boolean isSerializable() {
		return !isEditable()
				&& !isInverseModelListRef()
				&& !isInverseModelRef();
	}
	
	public boolean isUnsupported() {
		return isInverseModelRef()
			|| isInverseModelListRef();
	}
    
    public boolean isEditable() {
    	return ModelUtil.isBaseType(clazz)
			|| isCategory()
			|| isEmail()
			|| isGeoPt()
			|| isLink()
			|| isModelRef()
			|| isPhoneNumber()
			|| isPostalAddress()
			|| isRating()
			|| isText()
			|| isUser()
			|| (isArray() && ModelUtil.isBaseType(clazz.getComponentType()))
			|| (isCollection() && ModelUtil.isBaseType(genericType));
    }
    
    public boolean isViewable() {
    	return ModelUtil.isBaseType(clazz)
    		|| isCategory()
    		|| isEmail()
    		|| isGeoPt()
    		|| isInverseModelListRef()
    		|| isInverseModelRef()
    		|| isLink()
			|| isModelRef()
    		|| isPhoneNumber()
    		|| isPostalAddress()
    		|| isRating()
    		|| isText()
    		|| isUser()
    		|| (isArray() && ModelUtil.isBaseType(clazz.getComponentType()))
    		|| (isCollection() && ModelUtil.isBaseType(genericType));
    }
    
    /**
     * プロパティの名称を取得する.
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * プロパティがEnum型の場合、その列挙内容をkey=valueのMapリストを取得する.
     * @return
     */
    public List<Map<String, String>> getEnumElements() {
        if (!isEnum()) return null;
        return new SelectBox((Enum<?>[])clazz.getEnumConstants()).getElements();
    }
    
    /**
     * プロパティが配列、またはコレクション型、かつ要素数が多すぎる場合のパラメータキーを取得する.
     * @return
     */
    public String getTooManyElementsKey() {
        if (!isCollection() && !isArray()) return null;
        return name+SUFIX_OF_TOO_MANY_ELEMENTS;
    }
    
    /**
     * プロパティが配列の場合、配列の要素型を取得する.
     * @return
     */
    public Class<?> getComponentType() {
        if (!isArray()) return null;
        return clazz.getComponentType();
    }
    /**
     * プロパティのジェネリクス型を設定する.
     * @param genericType
     */
    protected void setGenericType(Class<?> genericType) {
    	this.genericType = genericType;
    }
    /**
     * プロパティのジェネリクス型を取得する.
     * @return
     */
    public Class<?> getGenericType() {
    	if (!isCollection() && !isRelationship()) return null;
    	return genericType;
    }
    
    /**
     * モデルのプライマリキーであるか判定する.
     * @return
     */
    public boolean isPrimaryKey() {
        return this.isKey() && isPrimaryKey;
    }
    /**
     * モデルのプライマリキーであるかを設定する.
     * @param isPrimaryKey
     */
    protected void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }
    
    /**
	 * @return isVersion
	 */
	public boolean isVersion() {
		return isVersion;
	}

	/**
	 * @param isVersion セットする isVersion
	 */
	protected void setVersion(boolean isVersion) {
		this.isVersion = isVersion;
	}

	/**
	 * @return isLob
	 */
	public boolean isLob() {
		return isLob;
	}

	/**
	 * @param isLob セットする isLob
	 */
	protected void setLob(boolean isLob) {
		this.isLob = isLob;
	}

	/**
	 * @return isRelationship
	 */
	public boolean isRelationship() {
		return ModelRef.class.isAssignableFrom(clazz)
			|| InverseModelRef.class.isAssignableFrom(clazz)
			|| InverseModelListRef.class.isAssignableFrom(clazz);
	}
	
	public boolean isModelRef() {
		return ModelRef.class.isAssignableFrom(clazz);
	}
	
	public boolean isInverseModelRef() {
		return InverseModelRef.class.isAssignableFrom(clazz);
	}
	
	public boolean isInverseModelListRef() {
		return InverseModelListRef.class.isAssignableFrom(clazz);
	}

    public String getUserEmailKey() {
        if (!isUser()) return null;
        return name+SUFIX_OF_USER_EMAIL;
    }
    
    public String getUserAuthDomainKey() {
    	if (!isUser()) return null;
    	return name+SUFIX_OF_USER_AUTHDOMAIN;
    }
    
    public String getUserUserIdKey() {
    	if (!isUser()) return null;
    	return name+SUFIX_OF_USER_USERID;
    }
    
    public String getUserFederatedIdentityKey() {
    	if (!isUser()) return null;
    	return name+SUFIX_OF_USER_FEDERATEDIDENTITY;
    }
    
    public String getGeoPtLatitudeKey() {
    	if(!isGeoPt()) return null;
    	return name+SUFIX_OF_GEOPT_LATITUDE;
    }
    
    public String getGeoPtLongitudeKey() {
    	if(!isGeoPt()) return null;
    	return name+SUFIX_OF_GEOPT_LONGITUDE;
    }

}

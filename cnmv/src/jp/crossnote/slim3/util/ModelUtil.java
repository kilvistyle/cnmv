/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.InverseModelRef;
import org.slim3.datastore.ModelMetaUtil;
import org.slim3.datastore.ModelRef;
import org.slim3.util.BeanDesc;
import org.slim3.util.BeanUtil;
import org.slim3.util.ClassUtil;
import org.slim3.util.ConversionUtil;
import org.slim3.util.DateUtil;
import org.slim3.util.PropertyDesc;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

/**
 * ModelUtil.
 * slim3 model util.
 * 
 * @author kilvistyle
 * @since 2009/11/27
 *
 */
public class ModelUtil {
    private ModelUtil() {
    }
    
    private static void checkSlim3Model(Class<?> modelClass) {
        if (ModelMetaUtil.isModelClass(modelClass) == false) {
            throw new IllegalArgumentException("the modelClass is not slim3 model.");
        }
    }

    /**
     * modelClassのPropertyTypeリストを取得する.
     * PKになるkeyプロパティは必ず戻り値のリストの先頭に格納され返却されます。
     * @param modelClass slim3のモデルクラス
     * @return PropertyTypeリスト（先頭はプライマリキー）
     */
    @SuppressWarnings("unchecked")
	public static List<PropertyType> getPropertyTypes(Class<?> modelClass) {
        if (modelClass == null) return null;
        checkSlim3Model(modelClass);
        // プロパティのソート順を担保するMapを生成
        Map<String, PropertyType> sortedPropertyType =
        	new LinkedHashMap<String, PropertyType>();
        String pkName = null;
        String versionName = null;
        Set<String> lobNames = new HashSet<String>();
        Map<String, Class> elementClassMap = new HashMap<String, Class>();
        LinkedHashSet<Field> fields = getFields(modelClass);
        for (Field f : fields) {
        	sortedPropertyType.put(f.getName(), null);
        	putGenericType(f, elementClassMap);
        	Attribute attr = f.getAnnotation(Attribute.class);
        	if (attr == null) continue;
        	else if (attr.primaryKey()) pkName = f.getName();
        	else if (attr.version()) versionName = f.getName();
        	else if (attr.lob()) lobNames.add(f.getName());
        }
        BeanDesc desc = BeanUtil.getBeanDesc(modelClass);
        int size = desc.getPropertyDescSize();
        for (int i = 0; i < size; i++) {
            PropertyDesc prop = desc.getPropertyDesc(i);
            if (prop.isReadable() && prop.isWritable()) {
                PropertyType pt =
                    new PropertyType(prop.getPropertyClass(),prop.getName());
                // このパラメータのメタ情報を追加
                pt.setPrimaryKey(pt.getName().equals(pkName));
                pt.setVersion(pt.getName().equals(versionName));
                pt.setLob(lobNames.contains(pt.getName()));
                pt.setGenericType(elementClassMap.get(pt.getName()));
                // 予めソートされたMapにValueを設定
                sortedPropertyType.put(pt.getName(), pt);
            }
            else if (prop.isReadable() &&
            		(ModelRef.class.isAssignableFrom(prop.getPropertyClass())
            		|| InverseModelRef.class.isAssignableFrom(prop.getPropertyClass())
            		|| InverseModelListRef.class.isAssignableFrom(prop.getPropertyClass()))) {
                PropertyType pt =
                    new PropertyType(prop.getPropertyClass(),prop.getName());
                // このパラメータのメタ情報を追加
                pt.setPrimaryKey(false);
                pt.setVersion(false);
                pt.setLob(false);
                pt.setGenericType(elementClassMap.get(pt.getName()));
                // 予めソートされたMapにValueを設定
                sortedPropertyType.put(pt.getName(), pt);
            }
        }
        List<PropertyType> lstResult = new ArrayList<PropertyType>();
        for (PropertyType pt : sortedPropertyType.values()) {
        	if (pt == null) {
        		continue;
        	}
        	// プライマリキーは常に先頭へ
        	if (pt.isPrimaryKey()) {
        		lstResult.add(0, pt);
        	}
        	else {
        		lstResult.add(pt);
        	}
        }
        return lstResult;
    }

    private static LinkedHashSet<Field> getFields(Class<?> modelClass) {
    	if (modelClass == null) return new LinkedHashSet<Field>();
    	LinkedHashSet<Field> fields = getFields(modelClass.getSuperclass());
    	fields.addAll(Arrays.asList(modelClass.getDeclaredFields()));
    	return fields;
	}

	@SuppressWarnings("unchecked")
	private static void putGenericType(
		Field f,
		Map<String, Class> elementClassMap) {
    	// コレクションの場合はプロパティ名：ジェネリック型をputする
    	if (List.class.isAssignableFrom(f.getType())
    		|| Set.class.isAssignableFrom(f.getType())
    		|| ModelRef.class.isAssignableFrom(f.getType())
    		|| InverseModelRef.class.isAssignableFrom(f.getType())
    		|| InverseModelListRef.class.isAssignableFrom(f.getType())) {
			Type t = f.getGenericType();
	    	if (t instanceof ParameterizedType) {
	    		Type[] atas = ((ParameterizedType)t).getActualTypeArguments();
	    		if (0 < atas.length && atas[0] instanceof Class) {
	    			elementClassMap.put(f.getName(), (Class)atas[0]);
	    		}
	    	}
    	}
	}

	/**
     * modelClassの新しいインスタンスを取得する.
     * ※modelClassはslim3のモデル（@Model注釈付き）である必要あり.
     * @param modelClass slim3のモデルクラス
     * @return modelClassの新しいインスタンス
     */
    public static <T> T newModel(Class<?> modelClass) {
        if (modelClass == null) return null;
        checkSlim3Model(modelClass);
        return ClassUtil.newInstance(modelClass);
    }

    /**
     * modelのpropertyNameプロパティの値を取得する.
     * @param model model
     * @param propertyName プロパティ名
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T>T getValue(Object model, String propertyName) {
        if (model == null || propertyName == null) return null;
        checkSlim3Model(model.getClass());
        BeanDesc desc = BeanUtil.getBeanDesc(model.getClass());
        PropertyDesc pDesc = desc.getPropertyDesc(propertyName);
        if (pDesc.isReadable()) {
            return (T)pDesc.getValue(model);
        }
        else {
            return null;
        }
    }
    
    /**
     * modelのpropertyNameプロパティにvalueをセットする.
     * @param model model
     * @param propertyName プロパティ名
     * @param value セットするvalue
     */
    public static void setValue(Object model, String propertyName, Object value) {
        if (model == null || propertyName == null) return;
        checkSlim3Model(model.getClass());
        BeanDesc desc = BeanUtil.getBeanDesc(model.getClass());
        PropertyDesc pDesc = desc.getPropertyDesc(propertyName);
        if (pDesc.isWritable()) {
            pDesc.setValue(model, value);
        }
    }
    
    /**
     * modelのPropertyDescリストを取得する.
     * @param model
     * @return
     */
    public static List<PropertyDesc> getPropertyDescList(Object model) {
        if (model == null) return null;
        checkSlim3Model(model.getClass());
        BeanDesc desc = BeanUtil.getBeanDesc(model.getClass());
        int size = desc.getPropertyDescSize();
        List<PropertyDesc> propDescList = new ArrayList<PropertyDesc>();
        for (int i = 0; i < size; i++) {
            PropertyDesc prop = desc.getPropertyDesc(i);
            if (prop.isReadable() && prop.isWritable()) {
                propDescList.add(prop);
            }
        }
        return propDescList;
    }
    
    /**
     * modelからkeyを取得する.
     * modelの@Attribute(primaryKey=true)指定されているKeyプロパティを取得する.
     * @param model model
     * @return modelのプライマリキー
     */
	public static Key getKey(Object model) {
        return ModelMetaUtil.getKey(model);
    }
    
    /**
     * modelにkeyをセットする.
     * modelの@Attribute(primaryKey=true)指定されているKeyプロパティにkeyをセットする.
     * @param model model
     * @param key modelのプライマリキー
     */
	public static void setKey(Object model, Key key) {
		ModelMetaUtil.setKey(model, key);
    }
    
    public static Entity modelToEntity(Object model) {
        if (model == null) return null;
        checkSlim3Model(model.getClass());
        return Datastore.getModelMeta(model.getClass()).modelToEntity(model);
    }
    public static <T>T entityToModel(Entity entity, Class<T> modelClass) {
        return Datastore.getModelMeta(modelClass).entityToModel(entity);
    }
    
    /**
     * 文字列を指定された型のオブジェクトに変換する.
     * @param clazz 変換後のオブジェクトの型
     * @param s　変換する文字列
     * @return
     */
    public static <T>T stringToValue(Class<T> clazz, String s) {
        if (clazz == null) {
            throw new NullPointerException("the clazz parameter is null.");
        }
        if (s == null) {
            return null;
        }
        try {
            return ExConversionUtil.convert(s, clazz);
        }
        catch (IllegalArgumentException e) {
            if (clazz.isArray()) {
                return (T)stringToArrayValue(clazz, s);
            }
        }
        throw new IllegalArgumentException("The class("
            + s.getClass().getName()
            + ") can not be converted to the class("
            + clazz.getName()
            + ").");
    }

    /**
     * 文字列の配列から指定された配列型のオブジェクトに変換する.
     * @param arrayClazz 変換後のオブジェクトの型
     * @param s　変換する文字列（カンマ区切り）
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T>T stringToArrayValue(Class<T> arrayClazz, String s) {
        if (arrayClazz == null) {
            throw new NullPointerException("the arrayClazz parameter is null.");
        }
        if (s == null) {
            return null;
        }
        String[] arrStr = StrUtil.splittrim(s, ",");
        Object[] arrObj = new Object[arrStr.length];
        for (int i = 0; i < arrStr.length; i++) {
            arrObj[i] = ExConversionUtil.convert(arrStr[i], arrayClazz.getComponentType());
        }
        // プリミティブ型配列に変換
        if (arrayClazz.getComponentType().isPrimitive()) {
            return (T)wrapperToPrimitiveArray(arrObj, arrayClazz);
        }
        // ラッパー型配列に変換
        else {
            return (T)Arrays.copyOf(arrObj, arrObj.length, (Class)arrayClazz);
        }
    }
    
    /**
     * 文字列の配列から指定されたコレクション型オブジェクトに変換する.
     * @param collectionType 変換後のコレクション（java.util.List, java.util.Set, java.util.SortedSet）の型
     * @param elementType 変換後のコレクション要素の型
     * @param strElm 変換する文字列、またはその配列
     * @return　
     */
    @SuppressWarnings("unchecked")
    public static Collection<?> stringToCollectionValue(
            Class collectionType,
            Class elementType,
            String...strElm) throws IllegalArgumentException {
        if (collectionType == null) {
            throw new NullPointerException("the collectionType parameter is null.");
        }
        if (elementType == null) {
            throw new NullPointerException("the elementType parameter is null.");
        }
        if (strElm == null) {
            throw new NullPointerException("the strElm parameter is null.");
        }
        // コレクションオブジェクトの生成
        Collection collection = null;
        if (ArrayList.class.isAssignableFrom(collectionType) ||
        	LinkedList.class.isAssignableFrom(collectionType) ||
        	HashSet.class.isAssignableFrom(collectionType) ||
        	LinkedHashSet.class.isAssignableFrom(collectionType) ||
        	TreeSet.class.isAssignableFrom(collectionType)) {
        	try {
            	collection = (Collection<?>)collectionType.newInstance();
        	}
        	catch (Exception e) {
        		throw new IllegalArgumentException("the collectionType parameter is not supported type. " +
	            "usable Collection(java.util.ArrayList, java.util.LinkedList, java.util.HashSet, " +
	            "java.util.LinkedHashSet, java.util.TreeSet, java.util.List, java.util.Set, java.util.SortedSet) of a core datastore type.", e);
			}
        }
        else if (List.class.isAssignableFrom(collectionType)) {
            collection = new ArrayList(strElm.length);
        }
        else if (SortedSet.class.isAssignableFrom(collectionType)) {
            collection = new TreeSet();
        }
        else if (Set.class.isAssignableFrom(collectionType)) {
            collection = new HashSet(strElm.length);
        }
        else {
    		throw new IllegalArgumentException("the collectionType parameter is not supported type. " +
	            "usable Collection(java.util.ArrayList, java.util.LinkedList, java.util.HashSet, " +
	            "java.util.LinkedHashSet, java.util.TreeSet, java.util.List, java.util.Set, java.util.SortedSet) of a core datastore type.");
        }
        // コレクション要素の変換
        for (String elm : strElm) {
        	collection.add(ExConversionUtil.convert(elm, elementType));
        }
        return collection;
    }
    
    /**
     * valueを文字列に変換する.
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
	public static String valueToString(Object value) {
        String returnStr = null;
        if (value == null) {
            return returnStr;
        }
        else if (value instanceof String) {
            returnStr = (String)value;
        }
        else if (Date.class.isAssignableFrom(value.getClass())) {
            returnStr = DateUtil.toString((Date)value, AppProperties.CNS3_DATE_PATTERN);
        }
        else if (Key.class.isAssignableFrom(value.getClass())) {
        	// no-id-yetキーのkeyToString例外対応
//            returnStr = KeyFactory.keyToString((Key)value);
        	returnStr = keyToString((Key)value);
        }
        else if (Enum.class.isAssignableFrom(value.getClass())) {
        	returnStr = ((Enum)value).name();
        }
        else if (ModelRef.class.isAssignableFrom(value.getClass())) {
        	returnStr = keyToString(((ModelRef)value).getKey());
        }
        else if (isBaseType(value.getClass())) {
        	returnStr = ConversionUtil.convert(value, String.class);
        }
        else if (User.class.isAssignableFrom(value.getClass())) {
        	returnStr = UserConverter.getInstance().getAsString(value);
        }
        else if (GeoPt.class.isAssignableFrom(value.getClass())) {
        	returnStr = GeoPtConverter.getInstance().getAsString(value);
        }
        else if (Text.class.isAssignableFrom(value.getClass())) {
        	returnStr = TextConverter.getInstance().getAsString(value);
        }
        else if (ShortBlob.class.isAssignableFrom(value.getClass())) {
        	returnStr = ShortBlobConverter.getInstance().getAsString(value);
        }
        else if (Blob.class.isAssignableFrom(value.getClass())) {
        	returnStr = BlobConverter.getInstance().getAsString(value);
        }
        else if (Category.class.isAssignableFrom(value.getClass())) {
        	returnStr = CategoryConverter.getInstance().getAsString(value);
        }
        else if (Email.class.isAssignableFrom(value.getClass())) {
        	returnStr = EmailConverter.getInstance().getAsString(value);
        }
        else if (Link.class.isAssignableFrom(value.getClass())) {
        	returnStr = LinkConverter.getInstance().getAsString(value);
        }
        else if (PhoneNumber.class.isAssignableFrom(value.getClass())) {
        	returnStr = PhoneNumberConverter.getInstance().getAsString(value);
        }
        else if (PostalAddress.class.isAssignableFrom(value.getClass())) {
        	returnStr = PostalAddressConverter.getInstance().getAsString(value);
        }
        else if (Rating.class.isAssignableFrom(value.getClass())) {
        	returnStr = RatingConverter.getInstance().getAsString(value);
        }
        else {
        	try {
                if (Collection.class.isAssignableFrom(value.getClass())) {
                    returnStr = arrayToString(((Collection<?>)value).toArray());
                }
                else if (value.getClass().isArray()) {
                    returnStr = arrayToString(value);
                }
                else {
                	returnStr = SerializableConverter.getInstance().getAsString(value);
                }
        	}
        	catch (IllegalArgumentException e) {
                returnStr = SerializableConverter.getInstance().getAsString(value);
			}
        }
        return returnStr;
    }
    
    /**
     * コレクション、または配列の要素数を取得する.
     * valueがコレクション、配列のどちらでもない場合は -1 を返す.
     * @param value
     * @return
     */
    public static int valueSize(Object value) {
        if (value == null) {
            return -1;
        }
        else if (Collection.class.isAssignableFrom(value.getClass())) {
            return ((Collection<?>)value).size();
        }
        else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().isPrimitive()) {
                return primitiveSize(value);
            }
            else {
                return ((Object[])value).length;
            }
        }
        else {
            return -1;
        }
    }
    
    /**
     * Keyを文字列に変換する.
     * ※no-id-yetキー対応で例外時はそのままでは保存できないが表示用の文字列を返す。
     * @param key
     * @return
     */
    private static String keyToString(Key key) {
    	if (key == null) return null;
    	try {
    		return KeyFactory.keyToString(key);
    	}
    	catch (Exception e) {
    		return key.toString();
    	}
    }
    
    /**
     * 配列型を文字列に変換する.
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
	private static String arrayToString(Object value) {
        // 配列がプリミティブ型の場合はラッパー型配列に変換
        if (value.getClass().getComponentType().isPrimitive()) {
            value = primitiveToWrapperArray(value);
        }
        // ラッパー型配列にキャスト
        Object[] valArray = (Object[])value;
        StringBuffer sb = new StringBuffer();
        for (Object val : valArray) {
            if (0 < sb.length()) {
                sb.append(",");
            }
            if (val == null) {
                continue;
            }
            else if (val instanceof Key) {
//                sb.append(KeyFactory.keyToString((Key)val));
            	sb.append(keyToString((Key)val));
            }
            else if (val instanceof Date) {
                sb.append(DateUtil.toString((Date)val, AppProperties.CNS3_DATE_PATTERN));
            }
            else if (val instanceof Enum) {
            	sb.append(((Enum)val).name());
            }
            else if (isBaseType(val.getClass())){
                sb.append(val.toString());
            }
            else {
                throw new IllegalArgumentException("The class("
                    + value.getClass().getName()
                    + ") can not be arrayToString.");
            }
        }
        return sb.toString();
    }
    
    /**
     * プリミティブ型配列の要素数を取得する.
     * @param value
     * @return
     */
    private static int primitiveSize(Object value) {
        Class<?> primitiveClass = value.getClass().getComponentType();
        if (primitiveClass == int.class) {
            return ((int[])value).length;
        }
        else if (primitiveClass == boolean.class) {
            return ((boolean[])value).length;
        }
        else if (primitiveClass == long.class) {
            return ((long[])value).length;
        }
        else if (primitiveClass == double.class) {
            return ((double[])value).length;
        }
        else if (primitiveClass == short.class) {
            return ((short[])value).length;
        }
        else if (primitiveClass == float.class) {
            return ((float[])value).length;
        }
        else if (primitiveClass == byte.class) {
            return ((byte[])value).length;
        }
        else {
            throw new IllegalArgumentException("Unsupported primitive class: "
                + primitiveClass.getName());
        }
    }

    /**
     * プリミティブ型配列をラッパー型配列に変換する.
     * @param primitiveArray
     * @return
     */
    private static Object primitiveToWrapperArray(Object primitiveArray) {
        Class<?> primitiveClass = primitiveArray.getClass().getComponentType();
        Object[] arrWrapper = null;
        if (primitiveClass == int.class) {
            int[] arr = (int[])primitiveArray;
            arrWrapper = new Integer[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Integer.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == boolean.class) {
            boolean[] arr = (boolean[])primitiveArray;
            arrWrapper = new Boolean[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Boolean.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == long.class) {
            long[] arr = (long[])primitiveArray;
            arrWrapper = new Long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Long.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == double.class) {
            double[] arr = (double[])primitiveArray;
            arrWrapper = new Double[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Double.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == short.class) {
            short[] arr = (short[])primitiveArray;
            arrWrapper = new Short[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Short.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == float.class) {
            float[] arr = (float[])primitiveArray;
            arrWrapper = new Float[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Float.valueOf(arr[i]);
            }
        }
        else if (primitiveClass == byte.class) {
            byte[] arr = (byte[])primitiveArray;
            arrWrapper = new Byte[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrWrapper[i] = Byte.valueOf(arr[i]);
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported primitive class: "
                + primitiveClass.getName());
        }
        return arrWrapper;
    }
    
    /**
     * プリミティブラッパー型配列をプリミティブ型配列に変換する.
     * @param wrapArr
     * @param primitiveArrayType
     * @return
     */
    private static Object wrapperToPrimitiveArray(Object wrapperArray, Class<?> primitiveArrayType) {
        Object[] wrapArr = (Object[])wrapperArray;
        if (primitiveArrayType == int[].class) {
            int[] arr = new int[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Integer)wrapArr[i]).intValue();
            }
            return arr;
        }
        else if (primitiveArrayType == boolean[].class) {
            boolean[] arr = new boolean[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Boolean)wrapArr[i]).booleanValue();
            }
            return arr;
        }
        else if (primitiveArrayType == long[].class) {
            long[] arr = new long[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Long)wrapArr[i]).longValue();
            }
            return arr;
        }
        else if (primitiveArrayType == double[].class) {
            double[] arr = new double[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Double)wrapArr[i]).doubleValue();
            }
            return arr;
        }
        else if (primitiveArrayType == short[].class) {
            short[] arr = new short[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Short)wrapArr[i]).shortValue();
            }
            return arr;
        }
        else if (primitiveArrayType == float[].class) {
            float[] arr = new float[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Float)wrapArr[i]).floatValue();
            }
            return arr;
        }
        else if (primitiveArrayType == byte[].class) {
            byte[] arr = new byte[wrapArr.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ((Byte)wrapArr[i]).byteValue();
            }
            return arr;
        }
        else {
            throw new IllegalArgumentException("Unsupported conversion primitive class: "
                + primitiveArrayType.getName());
        }
    }

	/**
     * 引数のクラスが基本的な型であるか判定する.（配列、コレクション型を除く）
     * @param c
     * @return
     */
    public static boolean isBaseType(Class<?> c) {
        return c == String.class
        || Boolean.class.isAssignableFrom(c)
        || Enum.class.isAssignableFrom(c)
        || Date.class.isAssignableFrom(c)
        || Key.class.isAssignableFrom(c)
        // BigIntegerはslim3の変換に対応していないため除外
        || (Number.class.isAssignableFrom(c) && !BigInteger.class.isAssignableFrom(c))
        || boolean.class == c
        || int.class == c
        || long.class == c
        || double.class == c
        || short.class == c
        || float.class == c
        || byte.class == c;
    }
    
}

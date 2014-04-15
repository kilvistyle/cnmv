/**
 * Copyright (c) 2011 kilvistyle
 */
package org.slim3.datastore;

import com.google.appengine.api.datastore.Key;

/**
 * ModelMetaUtil.
 * 
 * @author kilvistyle
 * @since 2011/02/25
 *
 */
public class ModelMetaUtil {

	private ModelMetaUtil() {
	}
	
	@SuppressWarnings("unchecked")
	public static Key getKey(Object model) {
		ModelMeta meta =
			DatastoreUtil.getModelMeta(model.getClass());
		if (meta == null) throw new IllegalArgumentException("the model is not model of slim3.");
		return meta.getKey(model);
	}
	
	@SuppressWarnings("unchecked")
	public static void setKey(Object model, Key key) {
		ModelMeta meta =
			DatastoreUtil.getModelMeta(model.getClass());
		if (meta == null) throw new IllegalArgumentException("the model is not model of slim3.");
		meta.setKey(model, key);
	}
	
	@SuppressWarnings("unchecked")
	public static long getVersion(Object model) {
		ModelMeta meta =
			DatastoreUtil.getModelMeta(model.getClass());
		if (meta == null) throw new IllegalArgumentException("the model is not model of slim3.");
		return meta.getVersion(model);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isModelClass(Class modelClass) {
		return modelClass.getAnnotation(Model.class) != null;
	}
}

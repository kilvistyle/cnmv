package jp.crossnote.slim3.util;

import java.io.Serializable;

import org.slim3.util.ByteUtil;
import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

public class SerializableConverter implements Converter<Serializable> {
	
	private static SerializableConverter instance = null;
	
	private SerializableConverter() {
	}
	
	public static SerializableConverter getInstance() {
		if (instance == null) {
			instance = new SerializableConverter();
		}
		return instance;
	}

	@Override
	public Serializable getAsObject(String value) {
		if (StringUtil.isEmpty(value)) return null;
//        byte[] bytes = ModelUtil.stringToArrayValue(byte[].class, value);
        String[] arrStr = StrUtil.splittrim(value, ",");
        byte[] bytes = new byte[arrStr.length];
        for (int i = 0; i < arrStr.length; i++) {
        	bytes[i] = ByteUtil.toByte(arrStr[i]);
        }
        return ByteUtil.toObject(bytes);
	}

	@Override
	public String getAsString(Object value) {
		if (value == null) return null;
		byte[] bytes = ByteUtil.toByteArray(value);
		return ModelUtil.valueToString(bytes);
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return true;
	}

}

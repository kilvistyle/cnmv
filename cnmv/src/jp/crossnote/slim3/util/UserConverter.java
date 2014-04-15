/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import org.slim3.util.Converter;
import org.slim3.util.StringUtil;

import com.google.appengine.api.users.User;

/**
 * UserConverter.
 * 
 * @author kilvistyle
 * @since 2010/09/14
 *
 */
public class UserConverter implements Converter<User> {
	
	private static UserConverter instance = null;
	
	private UserConverter() {
	}
	
	public static UserConverter getInstance() {
		if (instance == null) {
			instance = new UserConverter();
		}
		return instance;
	}

	@Override
	public User getAsObject(String value) {
        if (StringUtil.isEmpty(value)) return null;
        String[] arrUserDatas = CsvUtil.readCsv(value)[0];
        // [0] user.email (required)
        // [1] user.authDomain (required)
        // [2] user.userId (optional)
        // [3] user.federatedIdentity (optional)
        // check elements
        if (arrUserDatas.length < 2
        	|| StringUtil.isEmpty(arrUserDatas[0])
        	|| StringUtil.isEmpty(arrUserDatas[1])) {
        	throw new IllegalArgumentException("User.email or User.authDomain is empty.");
        }
        if (arrUserDatas.length == 4
        	&& !StringUtil.isEmpty(arrUserDatas[3])) {
        	// create user with all parameters.
        	return new User(
        			arrUserDatas[0],
        			arrUserDatas[1],
        			arrUserDatas[2],
        			arrUserDatas[3]);
        }
        if (arrUserDatas.length == 3
        	&& !StringUtil.isEmpty(arrUserDatas[2])) {
        	// create user with userId.
        	return new User(
        			arrUserDatas[0],
        			arrUserDatas[1],
        			arrUserDatas[2]);
        }
    	// create user.
    	return new User(
    			arrUserDatas[0],
    			arrUserDatas[1]);
	}

	@Override
	public String getAsString(Object value) {
        if (value == null) return null;
        User user = (User)value;
        StringBuffer sb = new StringBuffer();
        sb.append(user.getEmail()+",");
        sb.append(user.getAuthDomain()+",");
        sb.append(StringUtil.isEmpty(user.getUserId()) ? "," : user.getUserId()+",");
        sb.append(StringUtil.isEmpty(user.getFederatedIdentity()) ? "" : user.getFederatedIdentity());
        return sb.toString();
	}

	@Override
	public boolean isTarget(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}
	
}

/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.controller.cn.modelview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.crossnote.slim3.controller.cn.AbstractBaseController;
import jp.crossnote.slim3.controller.validator.ExValidators;
import jp.crossnote.slim3.controller.validator.KeyValidator;
import jp.crossnote.slim3.model.CnmvTaskInfo;
import jp.crossnote.slim3.service.CnmvTaskInfoService;
import jp.crossnote.slim3.service.ModelService;
import jp.crossnote.slim3.util.AppProperties;
import jp.crossnote.slim3.util.ArrayConverter;
import jp.crossnote.slim3.util.CategoryConverter;
import jp.crossnote.slim3.util.CollectionConverter;
import jp.crossnote.slim3.util.EmailConverter;
import jp.crossnote.slim3.util.KeyConverter;
import jp.crossnote.slim3.util.LinkConverter;
import jp.crossnote.slim3.util.ModelUtil;
import jp.crossnote.slim3.util.PhoneNumberConverter;
import jp.crossnote.slim3.util.PostalAddressConverter;
import jp.crossnote.slim3.util.PropertyType;
import jp.crossnote.slim3.util.RatingConverter;
import jp.crossnote.slim3.util.TextConverter;

import org.slim3.controller.Navigation;
import org.slim3.datastore.Datastore;
import org.slim3.datastore.ModelRef;
import org.slim3.util.BeanUtil;
import org.slim3.util.CopyOptions;
import org.slim3.util.StringUtil;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;

/**
 * ConfirmController.
 * 
 * @author kilvistyle
 * @since 2009/11/26
 *
 */
public class RegistController extends AbstractBaseController {
	
	private static final Logger logger =
		Logger.getLogger(RegistController.class.getName());

    private ModelService service = ModelService.getInstance();

    @SuppressWarnings("unchecked")
    @Override
    protected Navigation run() {
        if (!isPost() || !validate()) {
            return redirect(basePath);
        }
        // ModelClassを取得
        Class<?> modelClass = service.getModelClass(asString("_modelname_"));
        if (modelClass == null) {
            return redirect(basePath);
        }
        // 既にこのモデルでタスク実行中か判定する
        CnmvTaskInfoService taskInfoService = CnmvTaskInfoService.getInstance();
        CnmvTaskInfo taskInfo = taskInfoService.getTaskInfo(modelClass);
        if (taskInfo != null) {
        	// タスク実行中画面へリダイレクト
        	return redirect("working?_modelname_="+modelClass.getName());
        }
        // INSERT / UPDATE　のモード判定
        boolean isInsert = StringUtil.isEmpty(asString("_key_"));
        // モード毎にモデルを取得
        Object model = isInsert ?
        		service.newModel(asString("_modelname_"))
        		: service.findByKey(modelClass, asKey("_key_"));
        if (model == null) {
            return redirect(basePath);
        }
        // このモデルのプロパティリストを取得
        List<PropertyType> propertyList =
            ModelUtil.getPropertyTypes(model.getClass());
        // このモデルの持つプロパティ毎のバリデーションを実施
        if (!validateProperty(propertyList)) {
            requestScope("_propertyList_", propertyList);
            return forward("edit.jsp");
        }
        // CopyOptionsを生成
        CopyOptions co = createCopyOptions(modelClass, propertyList);
        // リクエストの内容をModelに反映
        try {
	        // プロパティの型に応じてコンバータを指定
	        for (PropertyType pt : propertyList) {
	            // ModelRefプロパティの場合
	        	if (pt.isModelRef()) {
	                // 代わりにここで入力されたリレーションKey情報を設定
	                ModelRef mRef = (ModelRef)ModelUtil.getValue(model, pt.getName());
	                if (StringUtil.isEmpty(asString(pt.getName()))) {
	                	// 未入力の場合はリレーション解除
	                	mRef.setModel(null);
	                }
	                else {
	                	// 入力されている場合は設定
	                	mRef.setKey(Datastore.stringToKey(asString(pt.getName())));
	                }
	            }
	            // User型の場合は複数の入力項目から一つのプロパティを生成する
	            else if (pt.isUser()) {
	            	// UserのEmail、AuthDomainKeyどちらかが未入力の場合
	            	if (StringUtil.isEmpty(asString(pt.getUserEmailKey()))
	            		|| StringUtil.isEmpty(asString(pt.getUserAuthDomainKey()))) {
	            		ModelUtil.setValue(model, pt.getName(), null);
	            	}
	            	else {
	            		// FederatedIdentityが入力されている場合
	            		if (!StringUtil.isEmpty(asString(pt.getUserFederatedIdentityKey()))) {
	            			ModelUtil.setValue(model, pt.getName(),
	            					new User(asString(pt.getUserEmailKey()),
	            							asString(pt.getUserAuthDomainKey()),
	            							asString(pt.getUserUserIdKey()),
	            							asString(pt.getUserFederatedIdentityKey())));
	            		}
	            		// UserIdが入力されている場合
	            		else if (!StringUtil.isEmpty(asString(pt.getUserUserIdKey()))) {
	            			ModelUtil.setValue(model, pt.getName(),
	            					new User(asString(pt.getUserEmailKey()),
	            							asString(pt.getUserAuthDomainKey()),
	            							asString(pt.getUserUserIdKey())));
	            		}
	            		// それ以外の場合は必須入力項目から生成
	            		else {
	            			ModelUtil.setValue(model, pt.getName(),
	            					new User(asString(pt.getUserEmailKey()),
	            							asString(pt.getUserAuthDomainKey())));
	            		}
	            	}
	            }
	            // GeoPt型の場合は複数の入力項目から一つのプロパティを生成する
	            else if (pt.isGeoPt()) {
	            	// GeoPtの値どちらかが未入力の場合
	                if (StringUtil.isEmpty(asString(pt.getGeoPtLatitudeKey()))
	                	|| StringUtil.isEmpty(asString(pt.getGeoPtLongitudeKey()))) {
	                	ModelUtil.setValue(model, pt.getName(), null);
	                }
	                // 入力されている場合
	                else {
	                	ModelUtil.setValue(model, pt.getName(),
	                			new GeoPt(asFloat(pt.getGeoPtLatitudeKey()),
	                					asFloat(pt.getGeoPtLongitudeKey())));
	                }
	            }
	            // それ以外のプロパティの場合で未入力の場合
	            else if (StringUtil.isEmpty(asString(pt.getName()))
	            		&& pt.isEditable()) {
	            	// nullを設定する
	            	ModelUtil.setValue(model, pt.getName(), null);
	            }
	        }
        	// コピー実施
            BeanUtil.copy(request, model, co);
        }
        catch (Exception e) {
        	errors.put("err", "some parameter is invalid. (message : "+e.getMessage()+")");
            requestScope("_propertyList_", propertyList);
            return forward("edit.jsp");
		}
        // モデルの新規登録または上書更新
        try {
            if (isInsert) {
                model = service.insert(model);
            }
            else {
                model = service.update(asKey("_key_"), model);
            }
        }
        catch (Exception e) {
        	if (logger.isLoggable(Level.WARNING)) {
        		logger.log(Level.WARNING, e.getMessage());
        	}
            errors.put("err", "Update failed. (message : "+e.getMessage()+")");
            return forward(basePath);
        }
        // リダイレクト
        return redirectWith(isInsert);
    }

	@SuppressWarnings("unchecked")
	private CopyOptions createCopyOptions(Class modelClass, List<PropertyType> propertyList) {
        // プロパティのコピー準備
        CopyOptions co =
            new CopyOptions()
        		.dateConverter(AppProperties.CNS3_DATE_PATTERN)
                .excludeEmptyString();
        // 除外プロパティ名のリスト
        List<String> excludePropNames = new ArrayList<String>();
        // プロパティの型に応じてコンバータを指定
        for (PropertyType pt : propertyList) {
            // 取り込まないプロパティを除外指定
            if (!pt.isEditable()
            	|| pt.isModelRef()
            	|| pt.isUser()
            	|| pt.isGeoPt()
            	|| pt.isShortBlob()
            	|| pt.isBlob()
            	|| pt.isSerializable()
            	|| has(pt.getTooManyElementsKey())) {
            	// 除外指定
                excludePropNames.add(pt.getName());
            }
            // プライマリキーの場合
            else if (pt.isPrimaryKey()) {
            	// PK編集されている場合はコンバータを指定
            	if (has("_pk_input_") && asBoolean("_pk_input_")) {
	    			if ("stk".equals(asString("_pk_policy_"))) {
	    				co = co.converter(new KeyConverter(
	    					modelClass,
	    					KeyConverter.Policy.STRING_TO_KEY),
	    					pt.getName());
	    			}
	    			else if ("iok".equals(asString("_pk_policy_"))) {
	    				co = co.converter(new KeyConverter(
	    					modelClass,
	    					KeyConverter.Policy.ID_OF_KEY),
	    					pt.getName());
	    			}
	    			else if ("nok".equals(asString("_pk_policy_"))) {
	    				co = co.converter(new KeyConverter(
	    					modelClass,
	    					KeyConverter.Policy.NAME_OF_KEY),
	    					pt.getName());
	    			}
            	}
            	// PK編集されていない場合はコピー対象外
            	else {
            		excludePropNames.add(pt.getName());
            	}
            }
            // 配列の場合
            else if (pt.isArray() && pt.isEditable()) {
                co = co.converter(new ArrayConverter((Class)pt.getType()), pt.getName());
            }
            // コレクションの場合
            else if (pt.isCollection() && pt.isEditable()) {
                co = co.converter(
                    new CollectionConverter(
                        (Class)pt.getType(),
                        (Class)pt.getGenericType()),
                        pt.getName());
            }
            // Category型の場合
            else if (pt.isCategory()) {
            	co = co.converter(CategoryConverter.getInstance(), pt.getName());
            }
            // Email型の場合
            else if (pt.isEmail()) {
                co = co.converter(EmailConverter.getInstance(), pt.getName());
            }
            // Link型の場合
            else if (pt.isLink()) {
            	co = co.converter(LinkConverter.getInstance(), pt.getName());
            }
            // Text型の場合
            else if (pt.isText()) {
                co = co.converter(TextConverter.getInstance(), pt.getName());
            }
            // PhoneNumber型の場合
            else if (pt.isPhoneNumber()) {
                co = co.converter(PhoneNumberConverter.getInstance(), pt.getName());
            }
            // PostalAddress型の場合
            else if (pt.isPostalAddress()) {
                co = co.converter(PostalAddressConverter.getInstance(), pt.getName());
            }
            // Rating型の場合
            else if (pt.isRating()) {
                co = co.converter(RatingConverter.getInstance(), pt.getName());
            }
        }
    	// コピー対象外のプロパティ名を設定
        co = co.exclude(excludePropNames.toArray(new String[]{}));
        return co;
	}

    private Navigation redirectWith(boolean isInsert) {
    	if (StringUtil.isEmpty(asString("_owner_model_"))) {
        	return redirect(basePath
        			+"?_modelname_="+asString("_modelname_")
        			+"&_page_="+asString("_page_")
        			+"&_limit_="+asString("_limit_")
        			+"&i="+isInsert);
    	}
    	else {
        	return redirect("/cn/modelview/refView"
        			+"?_modelname_="+asString("_owner_model_")
        			+"&_key_="+asString("_owner_key_")
        			+"&_ref_="+asString("_owner_param_")
        			+"&_page_="+asString("_page_")
        			+"&_limit_="+asString("_limit_")
        			+"&i="+isInsert);
    	}
	}

    private boolean validate() {
        ExValidators v = new ExValidators(request);
        v.add("_modelname_", v.required());
        if (!StringUtil.isEmpty(asString("_key_"))) {
            v.add("_key_", v.required(), v.key());
        }
        return v.validate();
    }
    
    @SuppressWarnings("unchecked")
    private boolean validateProperty(List<PropertyType> propTypeList) {
        ExValidators v = new ExValidators(request);
        for (PropertyType pt : propTypeList) {
        	if (pt.isPrimaryKey()) {
        		if (has("_pk_input_") && asBoolean("_pk_input_")) {
        			if ("stk".equals(asString("_pk_policy_"))) {
        				v.add(pt.getName(), v.required(), v.key());
        			}
        			else if ("iok".equals(asString("_pk_policy_"))) {
        				v.add(pt.getName(), v.required(), v.key(KeyValidator.Policy.ID_OF_KEY));
        			}
        			else if ("nok".equals(asString("_pk_policy_"))) {
        				v.add(pt.getName(), v.required(), v.key(KeyValidator.Policy.NAME_OF_KEY));
        			}
        		}
        	}
        	else if (pt.isModelRef()) {
        		v.add(pt.getName(), v.key());
        	}
        	else if (pt.isNumber()) {
                v.add(pt.getName(), v.longType());
            }
            else if (pt.isDecimal()) {
            	v.add(pt.getName(), v.doubleType());
            }
            else if (pt.isDate()) {
                v.add(pt.getName(), v.dateType(AppProperties.CNS3_DATE_PATTERN));
            }
            else if (pt.isKey()) {
                v.add(pt.getName(), v.key());
            }
            else if (pt.isEnum()) {
                v.add(pt.getName(), v.enumType(pt.getType()));
            }
            else if (pt.isUser()) {
            	if (!StringUtil.isEmpty(asString(pt.getUserEmailKey()))
            		|| !StringUtil.isEmpty(asString(pt.getUserAuthDomainKey()))) {
            		v.add(pt.getUserAuthDomainKey(), v.required());
            		v.add(pt.getUserEmailKey(), v.required());
            	}
            }
            else if (pt.isGeoPt()) {
            	if (!StringUtil.isEmpty(asString(pt.getGeoPtLatitudeKey()))
            		|| !StringUtil.isEmpty(asString(pt.getGeoPtLongitudeKey()))) {
            		v.add(pt.getGeoPtLongitudeKey(), v.required(), v.floatType());
            		v.add(pt.getGeoPtLatitudeKey(), v.required(), v.floatType());
            	}
            }
            else if (pt.isRating()) {
            	if (!StringUtil.isEmpty(asString(pt.getName()))) {
            		v.add(pt.getName(), v.integerType(), v.longRange(0, 100));
            	}
            }
            else if (!has(pt.getTooManyElementsKey())) {
                if (pt.isArray()) {
                    v.add(pt.getName(), v.arrayType((Class)pt.getType()));
                }
                else if (pt.isCollection()) {
                    v.add(pt.getName(),
                        v.collectionType(
                            (Class)pt.getType(),
                            (Class)pt.getGenericType()));
                }
            }
        }
        return v.validate();
    }

}

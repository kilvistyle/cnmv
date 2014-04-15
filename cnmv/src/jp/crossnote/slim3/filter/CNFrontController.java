/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.filter;

import jp.crossnote.slim3.constants.GlobalConstants;
import jp.crossnote.slim3.util.CNResourceBundleApplicationMessageDelegate;

import org.slim3.controller.ControllerConstants;
import org.slim3.controller.FrontController;
import org.slim3.util.ApplicationMessage;

/**
 * CNFrontController.
 * crossnote用FrontController
 * 
 * @author kilvistyle
 * @since 2009/12/04
 *
 */
public class CNFrontController extends FrontController {

    @Override
    protected void initRootPackageName() {
        rootPackageName = GlobalConstants.ROOT_PACKAGE;
    }

    /**
     * Returns the controller package name.
     * 
     * @return the controller package name
     */
    protected String getControllerPackageName() {
        return ControllerConstants.DEFAULT_CONTROLLER_PACKAGE;
    }

    /**
     * Initializes the default bundle and CN message bundle.
     */
    protected void initBundleName() {
        super.initBundleName();
        ApplicationMessage.setDelegateClass(CNResourceBundleApplicationMessageDelegate.class);
    }

}

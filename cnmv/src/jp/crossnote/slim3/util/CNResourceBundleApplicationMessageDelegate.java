/**
 * 
 */
package jp.crossnote.slim3.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jp.crossnote.slim3.constants.GlobalConstants;

import org.slim3.util.LocaleLocator;
import org.slim3.util.ResourceBundleApplicationMessageDelegate;

/**
 * @author kilvistyle
 * @since 2011/03/03
 */
public class CNResourceBundleApplicationMessageDelegate extends ResourceBundleApplicationMessageDelegate {

    /**
     * The resource bundles.
     */
    protected ThreadLocal<ResourceBundle> cnBundles =
        new ThreadLocal<ResourceBundle>();
    
    public CNResourceBundleApplicationMessageDelegate() {
    	cnBundles.set(ResourceBundle.getBundle(GlobalConstants.CN_MESSAGE_BUNDLE,
    			LocaleLocator.get(), Thread.currentThread().getContextClassLoader()));
    }

    public void setBundle(String bundleName, Locale locale)
            throws NullPointerException {
    	// set application bundle
    	super.setBundle(bundleName, locale);
    	// set cn bundle
        try {
            cnBundles.set(ResourceBundle.getBundle(
            	GlobalConstants.CN_MESSAGE_BUNDLE,
            	locale,
            	Thread.currentThread().getContextClassLoader()));
        } catch (MissingResourceException ignore) {
            cnBundles.set(ResourceBundle.getBundle(
            	GlobalConstants.CN_MESSAGE_BUNDLE,
                Locale.ENGLISH,
                Thread.currentThread().getContextClassLoader()));
        }
    }

    public String get(String key, Object... args)
            throws MissingResourceException {
        try {
            ResourceBundle bundle = cnBundles.get();
            if (bundle == null) {
                throw new IllegalStateException(
                    "The cnBundle attached to the current thread is not found.");
            }
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        }
        catch (MissingResourceException e) {
        	return super.get(key, args);
		}
    }
}

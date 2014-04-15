package jp.crossnote.slim3.util;

/**
 * EntityExistsException.
 * 
 * @author kilvistyle
 * @since 2013/08/19
 *
 */
public class EntityExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public EntityExistsException() {
		super();
	}

	/**
	 * @param message
	 */
	public EntityExistsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EntityExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EntityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}

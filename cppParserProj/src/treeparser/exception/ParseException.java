

package treeparser.exception;

/**
 *
 * @author Tomi
 */
public class ParseException extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = -879776632103524891L;

	public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException() {
        super();
    }

}

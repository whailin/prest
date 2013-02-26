

package treeparser.exception;

/**
 *
 * @author Tomi
 */
public class StringParseException extends ParseException{

    public StringParseException() {
        super();
    }

    public StringParseException(String message) {
        super(message);
    }

    public StringParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public StringParseException(Throwable cause) {
        super(cause);
    }

}

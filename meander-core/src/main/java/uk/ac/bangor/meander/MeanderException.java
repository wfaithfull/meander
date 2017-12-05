package uk.ac.bangor.meander;

/**
 * @author Will Faithfull
 *
 * Exception class to isolate and identify library-specific exceptions.
 */
public class MeanderException extends RuntimeException {

    public MeanderException(String message) {
        super(message);
    }

    public MeanderException(String message, Throwable cause) {
        super(message, cause);
    }

}

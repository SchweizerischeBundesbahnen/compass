package ch.sbb.compass.util.exception;

/**
 * This exception indicates the case, where a key already exists in the database.
 *
 * @author Kerem Adigüzel
 * @since 24.06.2016
 */
public class KeyAlreadyExistsException extends Exception {

    private static final String unformattedBaseMessage = "Key %s already exists in the database.";

    public KeyAlreadyExistsException(String key) {
        super(String.format(unformattedBaseMessage, key));
    }
}
package io.github.hligaty.exception;

/**
 * @author hligaty
 */
public class ReceiveException extends SimpleSocketIOException {
    public ReceiveException(String message) {
        super(message);
    }

    public ReceiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

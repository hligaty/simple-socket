package io.github.hligaty.exception;

/**
 * @author hligaty
 */
public class SimpleSocketRuntimeException extends RuntimeException {
    public SimpleSocketRuntimeException() {
    }

    public SimpleSocketRuntimeException(String message) {
        super(message);
    }

    public SimpleSocketRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

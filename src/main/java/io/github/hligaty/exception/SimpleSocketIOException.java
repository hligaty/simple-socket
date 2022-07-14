package io.github.hligaty.exception;

import java.io.IOException;

/**
 * @author hligaty
 */
public class SimpleSocketIOException extends IOException {
    public SimpleSocketIOException(String message) {
        super(message);
    }

    public SimpleSocketIOException(String message, Throwable cause) {
        super(message, cause);
    }
}

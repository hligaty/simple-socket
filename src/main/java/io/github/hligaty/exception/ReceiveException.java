package io.github.hligaty.exception;

/**
 * 接收消息异常
 *
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

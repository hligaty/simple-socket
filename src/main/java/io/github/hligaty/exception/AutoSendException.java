package io.github.hligaty.exception;

import io.github.hligaty.message.Message;

/**
 * @author hligaty
 */
public class AutoSendException extends SimpleSocketRuntimeException {
    private final Message sendMessage;

    public AutoSendException(String message, Throwable cause, Message sendMessage) {
        super(message, cause);
        this.sendMessage = sendMessage;
    }

    public Message getSendMessage() {
        return sendMessage;
    }
}

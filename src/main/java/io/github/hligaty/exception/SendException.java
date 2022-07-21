package io.github.hligaty.exception;

import io.github.hligaty.message.Message;

/**
 * 发送消息异常
 *
 * @author hligaty
 */
public class SendException extends SimpleSocketIOException {
    private final Message sendMessage;

    public SendException(String message, Throwable cause, Message sendMessage) {
        super(message, cause);
        this.sendMessage = sendMessage;
    }

    /**
     * @return 发送失败的消息
     */
    public Message getSendMessage() {
        return sendMessage;
    }
}

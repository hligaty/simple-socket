package io.github.hligaty.exception;

import io.github.hligaty.message.Message;

/**
 * 自动发送回复消息时异常
 *
 * @author hligaty
 */
public class AutoSendException extends SimpleSocketRuntimeException {
    private final Message sendMessage;

    public AutoSendException(String message, Throwable cause, Message sendMessage) {
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

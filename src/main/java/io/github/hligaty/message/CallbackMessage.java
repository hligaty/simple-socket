package io.github.hligaty.message;

import io.github.hligaty.Session;
import io.github.hligaty.exception.SendException;

/**
 * 发送成功和失败后可回调的消息
 *
 * @author hligaty
 */
public class CallbackMessage {
    private final Message internalMessage;

    /**
     * @param internalMessage 发送的消息
     */
    public CallbackMessage(Message internalMessage) {
        this.internalMessage = internalMessage;
    }

    public final Message getInternalMessage() {
        return internalMessage;
    }

    /**
     * 发送成功的回调
     *
     * @param session 会话
     */
    public void writeCallback(Session session) {
    }

    /**
     * 发送失败的回调
     *
     * @param e 导致发送失败的异常
     * @param session 会话
     */
    public void exceptionCallback(SendException e, Session session) {
    }
}

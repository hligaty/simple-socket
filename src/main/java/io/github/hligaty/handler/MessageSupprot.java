package io.github.hligaty.handler;

import io.github.hligaty.Session;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.message.CallbackMessage;

/**
 * 消息帮助类
 *
 * @author hligaty
 */
public class MessageSupprot {
    /**
     * Enhanced messaging.
     *
     * @param message message
     * @param session client
     */
    public final void send(CallbackMessage message, Session session) {
        try {
            if (message.getInternalMessage().isAsyncSend()) {
                session.send(message.getInternalMessage());
            } else {
                session.send(message.getInternalMessage());
            }
            message.writeCallback(session);
        } catch (SendException e) {
            message.exceptionCallback(e, session);
        }
    }
}

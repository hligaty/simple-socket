package io.github.hligaty.handler;

import io.github.hligaty.Session;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.message.CallbackMessage;

/**
 * Abstract message Handler.
 * Provides Enhanced messaging, etc.
 *
 * @author hligaty
 */
public abstract class AbstractMessageHandler {
    /**
     * Enhanced messaging.
     *
     * @param message message
     * @param session client
     */
    public void send(CallbackMessage message, Session session) {
        try {
            if (message.getInternalMessage().isAsyncSend()) {
                session.asyncSend(message.getInternalMessage());
            } else {
                session.send(message.getInternalMessage());
            }
            message.writeCallback(session);
        } catch (SendException e) {
            message.exceptionCallback(e, session);
        }
    }
}

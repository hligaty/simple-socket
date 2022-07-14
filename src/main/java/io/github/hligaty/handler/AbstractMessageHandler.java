package io.github.hligaty.handler;

import io.github.hligaty.message.Message;
import io.github.hligaty.util.Session;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Abstract message Handler.
 * Provides Enhanced messaging, etc.
 *
 * @author hligaty
 */
public abstract class AbstractMessageHandler implements SpecialMessageHandler {
    Executor writerGroup;

    /**
     * Enhanced messaging.
     *
     * @param message   message
     * @param session           client
     * @param writeCallback     write successful callback
     * @param exceptionCallback failed callback
     */
    public void send(Message message, Session session, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        try {
            session.send(message);
            if (writeCallback != null) {
                writeCallback.accept(session);
            }
        } catch (Exception e) {
            if (exceptionCallback != null) {
                exceptionCallback.accept(e, session);
            }
        }
    }

    /**
     * Async enhanced messaging.
     *
     * @param message   message
     * @param session           client
     * @param writeCallback     write successful callback
     * @param exceptionCallback failed callback
     */
    public void asyncSend(Message message, Session session, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        writerGroup.execute(() -> send(message, session, writeCallback, exceptionCallback));
    }

    public void setWriterGroup(Executor writerGroup) {
        this.writerGroup = writerGroup;
    }
}

package io.github.hligaty.handler;

import io.github.hligaty.message.Message;
import io.github.hligaty.util.Session;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Supports broadcast
 *
 * @author hligaty
 */
public abstract class BroadcastCapableMessageHandler extends AbstractMessageHandler implements SpecialMessageHandler {
    Map<Object, WeakReference<Session>> onLineList;

    /**
     * Broadcast message by filter
     *
     * @param allowSend write if true
     * @see AbstractMessageHandler#send(Message, Session, Consumer, BiConsumer)
     */
    public void broadcast(Message message, Predicate<Session> allowSend, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        onLineList.values().stream()
                .map(Reference::get)
                .filter(session -> session != null && allowSend != null && allowSend.test(session))
                .forEach(session -> send(message, session, writeCallback, exceptionCallback));
    }

    /**
     * Broadcast message by list
     *
     * @param idList connections that need to be broadcast
     * @see AbstractMessageHandler#send(Message, Session, Consumer, BiConsumer)
     */
    public void broadcast(Message message, List<Object> idList, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        idList.forEach(id -> send(message, onLineList.get(id).get(), writeCallback, exceptionCallback));
    }

    /**
     * Async broadcast message by filter
     *
     * @param allowSend write if true
     * @see AbstractMessageHandler#send(Message, Session, Consumer, BiConsumer)
     */
    public void asyncBroadcast(Message message, Predicate<Session> allowSend, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        writerGroup.execute(() -> broadcast(message, allowSend, writeCallback, exceptionCallback));
    }

    /**
     * Async broadcast message
     *
     * @param idList connections that need to be broadcast
     * @see AbstractMessageHandler#send(Message, Session, Consumer, BiConsumer)
     */
    public void asyncBroadcast(Message message, List<Object> idList, Consumer<Session> writeCallback, BiConsumer<Exception, Session> exceptionCallback) {
        writerGroup.execute(() -> broadcast(message, idList, writeCallback, exceptionCallback));
    }

    /**
     * get session by id
     *
     * @param id id
     * @return session
     */
    public Optional<Session> getSession(Object id) {
        WeakReference<Session> reference = onLineList.get(id);
        return Optional.ofNullable(reference == null ? null : reference.get());
    }

    public void setOnLineList(Map<Object, WeakReference<Session>> onLineList) {
        if (this.onLineList != null) {
            throw new IllegalArgumentException("onLineList not allow to set");
        }
        this.onLineList = onLineList;
    }
}

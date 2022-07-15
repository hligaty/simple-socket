package io.github.hligaty.handler;

import io.github.hligaty.Session;
import io.github.hligaty.message.CallbackMessage;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     */
    public void broadcast(CallbackMessage message, Predicate<Session> allowSend) {
        filter(allowSend).forEach(session -> send(message, session));
    }

    public Stream<Session> filter(Predicate<Session> allowSend) {
        return onLineList.values().stream()
                .map(Reference::get)
                .filter(session -> session != null && allowSend != null && allowSend.test(session));
    }

    /**
     * Broadcast message by list
     *
     * @param idList connections that need to be broadcast
     */
    public void broadcast(CallbackMessage message, List<Object> idList) {
        search(idList).forEach(session -> send(message, session));
    }

    public Stream<Session> search(List<Object> idList) {
        return idList.stream()
                .map(id -> onLineList.get(id))
                .filter(Objects::nonNull)
                .map(WeakReference::get)
                .filter(Objects::nonNull);
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

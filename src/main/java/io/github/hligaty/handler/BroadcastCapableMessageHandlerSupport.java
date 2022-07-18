package io.github.hligaty.handler;

import io.github.hligaty.Session;
import io.github.hligaty.message.CallbackMessage;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Supports broadcast
 *
 * @author hligaty
 */
public abstract class BroadcastCapableMessageHandlerSupport extends MessageHandlerSupprot implements SpecialMessageHandler {
    Map<Object, WeakReference<Session>> onLineList;

    public final Stream<Session> search(Predicate<Session> allowSend) {
        return onLineList.values().stream()
                .map(Reference::get)
                .filter(session -> session != null && allowSend != null && allowSend.test(session));
    }

    public final Stream<Session> search(Collection<Object> idList) {
        return idList.stream()
                .map(id -> onLineList.get(id))
                .filter(Objects::nonNull)
                .map(WeakReference::get)
                .filter(Objects::nonNull);
    }

    /**
     * Broadcast message by filter
     *
     * @param allowSend write if true
     */
    public final void broadcast(CallbackMessage message, Predicate<Session> allowSend) {
        search(allowSend).forEach(session -> send(message, session));
    }

    /**
     * Broadcast message by list
     *
     * @param idList connections that need to be broadcast
     */
    public final void broadcast(CallbackMessage message, Collection<Object> idList) {
        search(idList).forEach(session -> send(message, session));
    }

    /**
     * get session by id
     *
     * @param id id
     * @return session
     */
    public final Optional<Session> getSession(Object id) {
        WeakReference<Session> reference = onLineList.get(id);
        return Optional.ofNullable(reference == null ? null : reference.get());
    }

    public final void setOnLineList(Map<Object, WeakReference<Session>> onLineList) {
        if (this.onLineList != null) {
            throw new IllegalArgumentException("onLineList not allow to set");
        }
        this.onLineList = onLineList;
    }
}

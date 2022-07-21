package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.util.SessionFactory;
import io.github.hligaty.message.CallbackMessage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 广播支持类
 *
 * @author hligaty
 */
public class BroadcastMessageSupport extends MessageSupprot implements SpecialMessageHandler {
    SessionFactory sessionFactory;

    public final Stream<Session> search(Predicate<Session> allowSend) {
        Object id = Server.getCurrentSession().getId();
        return sessionFactory.getAllSession()
                .filter(session -> !id.equals(session))
                .filter(session -> allowSend != null && allowSend.test(session));
    }

    public final Stream<Session> search(Collection<Object> idList) {
        return idList.stream()
                .map(id -> sessionFactory.getSession(id))
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
        return Optional.ofNullable(sessionFactory.getSession(id));
    }

    public final void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

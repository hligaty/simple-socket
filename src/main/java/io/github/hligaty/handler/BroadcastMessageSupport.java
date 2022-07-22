package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.message.CallbackMessage;
import io.github.hligaty.util.SessionFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 广播支持类
 *
 * @author hligaty
 */
public class BroadcastMessageSupport extends MessageSupprot implements SpecialMessageHandler {
    SessionFactory sessionFactory;

    /**
     * Broadcast message by filter
     *
     * @param allowSend write if true
     */
    public final void broadcast(CallbackMessage message, Predicate<Session> allowSend) {
        Object id = Server.getCurrentSession().getId();
        sessionFactory.getAllSession().forEach(session -> {
            if (!Objects.equals(id, session.getId()) && allowSend != null && allowSend.test(session)) {
                send(message, session);
            }
        });
    }

    /**
     * Broadcast message by list
     *
     * @param idList connections that need to be broadcast
     */
    public final void broadcast(CallbackMessage message, Collection<Object> idList) {
        for (Object id : idList) {
            send(message, sessionFactory.getSession(id));
        }
    }

    /**
     * get session by id
     *
     * @param id id
     * @return session
     */
    public final Session getSession(Object id) {
        return sessionFactory.getSession(id);
    }

    public final void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

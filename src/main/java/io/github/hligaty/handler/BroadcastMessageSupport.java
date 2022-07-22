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
        Object userId = Server.getCurrentSession().getUserId();
        sessionFactory.getAllSession().forEach(session -> {
            if (!Objects.equals(userId, session.getUserId()) && allowSend != null && allowSend.test(session)) {
                send(message, session);
            }
        });
    }

    /**
     * Broadcast message by list
     *
     * @param userIdList connections that need to be broadcast
     */
    public final void broadcast(CallbackMessage message, Collection<Object> userIdList) {
        for (Object userId : userIdList) {
            send(message, sessionFactory.getSession(userId));
        }
    }

    /**
     * get session by userId
     *
     * @param userId userId
     * @return session
     */
    public final Session getSession(Object userId) {
        return sessionFactory.getSession(userId);
    }

    public final void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

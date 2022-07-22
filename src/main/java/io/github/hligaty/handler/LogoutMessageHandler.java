package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.message.Message;

import java.nio.ByteBuffer;

/**
 * 登出处理器
 *
 * @author hligaty
 */
public abstract class LogoutMessageHandler extends BroadcastMessageSupport implements MessageHandler {

    @Override
    public final void doHandle(ByteBuffer byteBuffer) {
        try {
            logout(byteBuffer);
        } catch (RuntimeException e) {
            throw new LoginException();
        } finally {
            Session session = Server.getCurrentSession();
            sessionFactory.removeSession(session.getId(), session);
            session.setId(null);
        }
        throw new LoginException();
    }

    public final void exceptionCaught(Exception e, Message message) {
        if (Server.getCurrentSession().getId() != null) {
            exceptionLogout(e, message);
        }
    }

    /**
     * 登出
     */
    public abstract void logout(ByteBuffer byteBuffer);

    /**
     * 异常登出
     *
     * @param e       异常
     * @param message 被处理的消息
     */
    public abstract void exceptionLogout(Exception e, Message message);
}

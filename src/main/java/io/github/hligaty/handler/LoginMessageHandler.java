package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.exception.LoginException;

import java.nio.ByteBuffer;

/**
 * 登入处理器
 *
 * @author hligaty
 */
public abstract class LoginMessageHandler extends BroadcastMessageSupport implements MessageHandler {

    @Override
    public final void doHandle(ByteBuffer byteBuffer) {
        Object userId;
        try {
            if ((userId = login(byteBuffer)) != null) {
                Session session = Server.getCurrentSession();
                session.setUserId(userId);
                sessionFactory.putSession(session);
            }
        } catch (LoginException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new LoginException();
        }
    }

    /**
     * 登入，必须返回用户对应的 id
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    public abstract Object login(ByteBuffer byteBuffer);
}

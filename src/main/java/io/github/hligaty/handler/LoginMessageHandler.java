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
        Object id;
        try {
            if ((id = login(byteBuffer)) != null) {
                Session session = Server.getCurrentSession();
                session.setId(id);
                sessionFactory.putSession(session);
            }
        } catch (LoginException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new LoginException();
        }
    }

    /**
     * 登入，必须返回 id
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    public abstract Object login(ByteBuffer byteBuffer);
}

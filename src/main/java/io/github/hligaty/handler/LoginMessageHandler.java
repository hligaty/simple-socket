package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.Session;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Abstract login message Handler
 *
 * @author hligaty
 */
public abstract class LoginMessageHandler extends BroadcastCapableMessageHandlerSupport implements MessageHandler {

    @Override
    public final void doHandle(ByteBuffer byteBuffer) {
        Object id;
        try {
            if ((id = login(byteBuffer)) != null) {
                Session session = Server.getCurrentSession();
                session.setId(id);
                WeakReference<Session> reference = onLineList.put(id, new WeakReference<>(session));
                Session prevSession;
                if (reference != null && (prevSession = reference.get()) != null) {
                    prevSession.setId(null);
                }
            }
        } catch (LoginException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new LoginException();
        }
    }

    /**
     * login
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    public abstract Object login(ByteBuffer byteBuffer);
}

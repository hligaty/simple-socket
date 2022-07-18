package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.message.Message;

import java.nio.ByteBuffer;

/**
 * Abstract logout message Handler
 *
 * @author hligaty
 */
public abstract class LogoutMessageHandler extends BroadcastCapableMessageHandlerSupport implements MessageHandler {

    @Override
    public final void doHandle(ByteBuffer byteBuffer) {
        try {
            logout(byteBuffer);
        } finally {
            Server.getCurrentSession().setId(null);
        }
    }

    public final void exceptionCaught(Exception e, Message message) {
        if (Server.getCurrentSession().getId() != null) {
            exceptionLogout(e, message);
        }
    }

    /**
     * Handler logout.
     * It will not enter the {@link #exceptionLogout(Exception, Message)} method after throwing an exception
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    public abstract void logout(ByteBuffer byteBuffer);

    /**
     * Handle exception logout.
     * Only possible after successful login.
     * The reason for the exception may be that the connection is disconnected, the connection times out, and {@link MessageHandler#doHandle(ByteBuffer)} throws an exception.
     *
     * @param e       exception
     * @param message not null if {@link MessageHandler#doHandle(ByteBuffer)} throws an exception
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    public abstract void exceptionLogout(Exception e, Message message);
}

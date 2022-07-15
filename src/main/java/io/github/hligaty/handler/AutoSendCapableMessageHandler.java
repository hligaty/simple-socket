package io.github.hligaty.handler;

import io.github.hligaty.Server;
import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.message.Message;

import java.nio.ByteBuffer;

/**
 * Supports AutoWrite
 *
 * @author hligaty
 */
public interface AutoSendCapableMessageHandler extends MessageHandler {

    /**
     * Auto write message.
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    Message doHandleAndWrite(ByteBuffer byteBuffer);

    @Override
    default void doHandle(ByteBuffer byteBuffer) {
        Message message = null;
        try {
            message = doHandleAndWrite(byteBuffer);
            Server.getCurrentSession().send(message);
        } catch (SendException e) {
            throw new AutoSendException("auto write error", e.getCause(), message);
        }
    }
}

package io.github.hligaty.handler;

import java.nio.ByteBuffer;

/**
 * message Handler
 *
 * @author hligaty
 */
public interface MessageHandler {
    /**
     * Bind the message code that this message handler can handle, cannot be repeated
     *
     * @return message code
     */
    int bindCode();

    /**
     * The message handling is implemented here, and the message body needs to be parsed by you
     *
     * @param byteBuffer message body
     */
    void doHandle(ByteBuffer byteBuffer);
}

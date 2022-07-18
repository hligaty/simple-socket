package io.github.hligaty.util;

import io.github.hligaty.message.Message;

import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public final class EmptyObjects {
    public static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);
    public static final Message EMPTY_MESSAGE = new Message(false, 0) {};
}

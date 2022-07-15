package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public class ByteMessage extends Message {
    private ByteBuffer byteBuffer;

    public static ByteMessage async(int code) {
        return async(code, null);
    }

    public static ByteMessage async(int code, ByteBuffer byteBuffer) {
        ByteMessage streamMessage = sync(code, byteBuffer);
        streamMessage.setAsyncSend(true);
        return streamMessage;
    }

    public static ByteMessage sync(int code) {
        return new ByteMessage(code, null);
    }

    public static ByteMessage sync(int code, ByteBuffer byteBuffer) {
        return new ByteMessage(code, byteBuffer);
    }

    protected ByteMessage(int code, ByteBuffer byteBuffer) {
        super(false, code);
        this.byteBuffer = byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer != null ? byteBuffer : EmptyObjects.EMPTY_BYTEBUFFER;
    }
}

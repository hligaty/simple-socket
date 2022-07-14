package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

public class DefaultMessage extends Message {
    private ByteBuffer byteBuffer;

    public DefaultMessage(int code) {
        super(code);
    }

    public DefaultMessage(int code, ByteBuffer byteBuffer) {
        super(code);
        this.byteBuffer = byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer != null ? byteBuffer : EmptyObjects.EMPTY_BYTEBUFFER;
    }
}

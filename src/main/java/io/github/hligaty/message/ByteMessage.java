package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public class ByteMessage extends Message {
    private ByteBuffer byteBuffer;

    public ByteMessage(int code) {
        super(code);
    }

    public ByteMessage(int code, ByteBuffer byteBuffer) {
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

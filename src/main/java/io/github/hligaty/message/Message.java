package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public class Message {
    private int code;
    private ByteBuffer byteBuffer;

    public Message(int code) {
        this.code = code;
    }

    public Message(int code, ByteBuffer byteBuffer) {
        this.code = code;
        this.byteBuffer = byteBuffer;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer != null ? byteBuffer : EmptyObjects.EMPTY_BYTEBUFFER;
    }
}

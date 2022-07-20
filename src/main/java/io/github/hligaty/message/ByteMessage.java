package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public class ByteMessage extends Message {
    private ByteBuffer byteBuffer;

    public static ByteMessage asyncMessage(int code) {
        return asyncMessage(code, null);
    }

    public static ByteMessage asyncMessage(int code, ByteBuffer byteBuffer) {
        ByteMessage streamMessage = syncMessage(code, byteBuffer);
        streamMessage.setAsyncSend(true);
        return streamMessage;
    }

    public static ByteMessage syncMessage(int code) {
        return new ByteMessage(code, null);
    }

    public static ByteMessage syncMessage(int code, ByteBuffer byteBuffer) {
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

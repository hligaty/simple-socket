package io.github.hligaty.message;

import io.github.hligaty.util.EmptyObjects;

import java.nio.ByteBuffer;

/**
 * Byte 消息
 *
 * @author hligaty
 */
public class ByteMessage extends Message {
    private ByteBuffer byteBuffer;

    public static ByteMessage asyncMessage(int code) {
        if (code >= 0 && code <= ByteMessageCache.length) {
            return ByteMessageCache.asyncCache[code];
        }
        ByteMessage streamMessage = syncMessage(code, null);
        streamMessage.setAsyncSend(true);
        return streamMessage;
    }

    public static ByteMessage asyncMessage(int code, ByteBuffer byteBuffer) {
        ByteMessage streamMessage = syncMessage(code, byteBuffer);
        streamMessage.setAsyncSend(true);
        return streamMessage;
    }

    public static ByteMessage syncMessage(int code) {
        if (code >= 0 && code < ByteMessageCache.length) {
            return ByteMessageCache.syncCache[code];
        }
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

    /**
     * 消息缓存，区间为 0 <= code < 512
     */
    private static class ByteMessageCache {
        static final int length = 512;
        static final ByteMessage[] syncCache = new ByteMessage[length];
        static final ByteMessage[] asyncCache = new ByteMessage[length];

        static {
            for (int i = 0; i < length; i++) {
                syncCache[i] = ByteMessage.syncMessage(i);
                asyncCache[i] = ByteMessage.asyncMessage(i);
            }
        }
    }
}

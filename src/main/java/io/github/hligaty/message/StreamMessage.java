package io.github.hligaty.message;

/**
 * 流消息，推荐使用
 *
 * @author hligaty
 */
public class StreamMessage extends Message {
    private final int streamSize;
    private final StreamProvider streamProvider;

    public static StreamMessage asyncMessage(int code, int streamSize, StreamProvider streamProvider) {
        StreamMessage streamMessage = syncMessage(code, streamSize, streamProvider);
        streamMessage.setAsyncSend(true);
        return streamMessage;
    }

    public static StreamMessage syncMessage(int code, int streamSize, StreamProvider streamProvider) {
        return new StreamMessage(code, streamSize, streamProvider);
    }

    protected StreamMessage(int code, int streamSize, StreamProvider streamProvider) {
        super(false, code);
        this.streamSize = streamSize;
        this.streamProvider = streamProvider;
    }

    public int getStreamSize() {
        return streamSize;
    }

    public StreamProvider getSender() {
        return streamProvider;
    }
}

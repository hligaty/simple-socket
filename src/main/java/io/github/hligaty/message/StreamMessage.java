package io.github.hligaty.message;

import io.github.hligaty.util.StreamProvider;

/**
 * @author hligaty
 */
public class StreamMessage extends Message {
    private final int streamSize;
    private final StreamProvider streamProvider;

    public StreamMessage(int code, int streamSize, StreamProvider streamProvider) {
        super(code);
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

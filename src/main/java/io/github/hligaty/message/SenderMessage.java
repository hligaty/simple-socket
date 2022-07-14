package io.github.hligaty.message;

import io.github.hligaty.util.Sender;

public class SenderMessage extends Message {
    private final int bodySize;
    private final Sender sender;

    public SenderMessage(int code, int bodySize, Sender sender) {
        super(code);
        this.bodySize = bodySize;
        this.sender = sender;
    }

    public int getBodySize() {
        return bodySize;
    }

    public Sender getSender() {
        return sender;
    }
}

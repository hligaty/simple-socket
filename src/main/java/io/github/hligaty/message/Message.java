package io.github.hligaty.message;

/**
 * @author hligaty
 */
public abstract class Message {
    private final int code;
    private boolean asyncSend;

    public Message(boolean asyncSend, int code) {
        this.asyncSend = asyncSend;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isAsyncSend() {
        return asyncSend;
    }

    public void setAsyncSend(boolean asyncSend) {
        this.asyncSend = asyncSend;
    }
}

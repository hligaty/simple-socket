package io.github.hligaty.message;

/**
 * @author hligaty
 */
public abstract class Message {
    private final int code;

    public Message(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

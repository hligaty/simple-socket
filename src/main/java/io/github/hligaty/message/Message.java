package io.github.hligaty.message;

/**
 * 消息类，支持同步发送和异步发送
 * 此处同步和异步的区别是：
 * 同步，立即刷新 Socket 发送缓冲区，此时消息不会有框架导致的延迟
 * 异步，等待一段时间或下一条消息发现 Socket 发送缓冲区已满时 flush，此时消息有框架导致的延迟，延迟时间见 {@link io.github.hligaty.ServerOption#FLUSH_SNDBUF_INTERVAL} 说明
 *
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

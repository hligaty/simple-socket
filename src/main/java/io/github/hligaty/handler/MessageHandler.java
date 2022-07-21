package io.github.hligaty.handler;

import java.nio.ByteBuffer;

/**
 * 消息处理器
 *
 * @author hligaty
 */
public interface MessageHandler {
    /**
     * 绑定这个消息处理器能处理的消息码
     *
     * @return message code
     */
    int bindCode();

    /**
     * 实现消息处理
     *
     * @param byteBuffer 收到的消息内容
     */
    void doHandle(ByteBuffer byteBuffer);
}

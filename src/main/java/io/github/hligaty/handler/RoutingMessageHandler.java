package io.github.hligaty.handler;

import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理器委托类
 *
 * @author hligaty
 */
public final class RoutingMessageHandler {
    private final Logger log = LoggerFactory.getLogger(RoutingMessageHandler.class);
    private final Map<Integer, MessageHandler> messageHandlers = new HashMap<>();

    public void handleMessage(ByteMessage message) {
        try {
            messageHandlers.get(message.getCode()).doHandle(message.getByteBuffer());
        } catch (RuntimeException e) {
            if (e instanceof LoginException || e instanceof AutoSendException) {
                throw e;
            }
            // 用户异常，只打印不断连
            log.error("Message handleing failed, nested exception is {}: {}, with root cause",
                    e.getClass().getName(),
                    e.getMessage(),
                    e);
        }
    }

    public void addMessageHandler(MessageHandler messageHandler) {
        Assert.notNull(messageHandlers.put(messageHandler.bindCode(), messageHandler), "messageHandler must be unique. bindCode:'" + messageHandler.bindCode() + "'");
    }
}

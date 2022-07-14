package io.github.hligaty.handler;

import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.message.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hligaty
 */
public final class RoutingMessageHandler implements MessageHandler {
    private final Logger log = LoggerFactory.getLogger(RoutingMessageHandler.class);
    private final Map<Integer, MessageHandler> messageHandlers = new HashMap<>();

    @Override
    public int bindCode() {
        return -1;
    }

    @Override
    public void doHandle(ByteBuffer byteBuffer) {
        //
    }

    public void handleMessage(DefaultMessage message) {
        try {
            messageHandlers.get(message.getCode()).doHandle(message.getByteBuffer());
        } catch (RuntimeException e) {
            if (e instanceof LoginException || e instanceof AutoSendException) {
                throw e;
            }
            log.error("Message handleing failed, nested exception is {}: {}, with root cause",
                    e.getClass().getName(),
                    e.getMessage(),
                    e);
        }
    }

    public void addMessageHandler(MessageHandler messageHandler) {
        messageHandlers.put(messageHandler.bindCode(), messageHandler);
    }
}

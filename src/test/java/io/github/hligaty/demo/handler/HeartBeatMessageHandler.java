package io.github.hligaty.demo.handler;

import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class HeartBeatMessageHandler implements MessageHandler {
    @Override
    public int bindCode() {
        return MessageCode.HEART_BEAT;
    }

    @Override
    public void doHandle(ByteBuffer byteBuffer) {
        log.info("heartbeat. {}", byteBuffer.getInt());
    }
}

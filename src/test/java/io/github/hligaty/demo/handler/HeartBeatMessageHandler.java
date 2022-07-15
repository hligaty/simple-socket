package io.github.hligaty.demo.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.handler.MessageHandler;
import io.github.hligaty.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class HeartBeatMessageHandler implements MessageHandler {
    static byte[] bytes = new byte[512];

    static {
        for (byte i = 0; i < 127; i++) {
            bytes[i] = i;
        }
        for (byte i = 0; i < 127; i++) {
            bytes[i + 127] = i;
        }
        for (byte i = 0; i < 127; i++) {
            bytes[i + (127 * 2)] = i;
        }
        for (byte i = 0; i < 127; i++) {
            bytes[i + (127 * 3)] = i;
        }
    }

    @Override
    public int bindCode() {
        return MessageCode.HEART_BEAT;
    }

    @Override
    public void doHandle(ByteBuffer byteBuffer) {
        Session session = Server.getCurrentSession();

        long start = System.currentTimeMillis();
        int num = 300;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
        threadPoolExecutor.prestartAllCoreThreads();
        Message message;
        message = buildMessage();
        for (int i = 0; i < num; i++) {
            threadPoolExecutor.execute(() -> {
                //message = buildMessage();
                try {
                    for (int i1 = 0; i1 < 1000; i1++) {
                        session.asyncSend(message);
                    }
                } catch (SendException e) {
                    log.error("failed to send 10000 big msg", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("----------time:{}----------", System.currentTimeMillis() - start);
        log.info("heartbeat. {}", byteBuffer.getInt());
    }

    private Message buildMessage() {
        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
        //try {
        //    outputStream.write(bytes);
        //} catch (IOException ignored) {
        //    return null;
        //}
        Message message = null;
        //message = StreamMessage.async();
        //message = ByteMessage.async();
        return message;
    }
}

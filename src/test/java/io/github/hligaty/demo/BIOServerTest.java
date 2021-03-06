package io.github.hligaty.demo;

import io.github.hligaty.Server;
import io.github.hligaty.ServerOption;
import io.github.hligaty.Session;
import io.github.hligaty.demo.handler.HeartBeatMessageHandler;
import io.github.hligaty.demo.handler.LoginMessageHandler;
import io.github.hligaty.demo.handler.LogoutMessageHandler;
import io.github.hligaty.message.ByteMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.IntStream;

@Slf4j
public class BIOServerTest {
    public static final String GROUP_ONE = "id < 3";

    @Test
    public void test() throws IOException, InterruptedException {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "16");
        try (Server server = new Server(new InetSocketAddress(19630))
                .registerMessageHandler(new LoginMessageHandler())
                .registerMessageHandler(new LogoutMessageHandler())
                .registerMessageHandlers(Collections.singletonList(new HeartBeatMessageHandler()))
                .option(ServerOption.TIMEOUT, 1000 * 60)
                .option(ServerOption.BOSS_THREAD_NUMBER, 1)
                .option(ServerOption.ANNOTATIONSCAN_PACKAGE, "io.github.hligaty")
                .option(ServerOption.FLUSH_SNDBUF_INTERVAL, 100)
                .start()) {
            // mock client
            IntStream.rangeClosed(1, 10).parallel().forEach(i -> {
                try {
                    Session client = new Session(new Socket("localhost", 19630));
                    client.setUserId("root-" + i);
                    client.send(ByteMessage.syncMessage( MessageCode.LOGIN_REQ, ByteBuffer.wrap(client.getUserId().toString().getBytes(StandardCharsets.UTF_8))));
                    ByteMessage message = client.receive();
                    client.send(ByteMessage.syncMessage(MessageCode.HEART_BEAT, ByteBuffer.wrap(client.getUserId().toString().getBytes(StandardCharsets.UTF_8))));
                    for (int i1 = 0; i1 < 500000; i1++) {
                        ByteMessage receive = client.receive();
                    }
                    log.info("{}: login {}", client.getUserId(), new String(message.getByteBuffer().array()));
                    if (i <= 9) {
                        message = client.receive();
                        log.info("{} get {} logout", client.getUserId(), new String(message.getByteBuffer().array()));
                    }
                    client.send(ByteMessage.syncMessage(MessageCode.LOGOUT_REQ));
                    // group_one(id < 3) get broadcast message
                    if (i < 3) {
                        message = client.receive();
                        log.info("{} get {} logout", client.getUserId(), new String(message.getByteBuffer().array()));
                    }
                } catch (IOException e) {
                    log.error("client {}: ?", i, e);
                }
            });
            Thread.sleep(1000L);
        }
    }
}

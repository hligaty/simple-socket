package io.github.hligaty.demo.handler;

import io.github.hligaty.Server;
import io.github.hligaty.Session;
import io.github.hligaty.cache.Group;
import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.message.CallbackMessage;
import io.github.hligaty.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static io.github.hligaty.demo.BIOServerTest.GROUP_ONE;

@Slf4j
public class LogoutMessageHandler extends io.github.hligaty.handler.LogoutMessageHandler {

    @Override
    public int bindCode() {
        return MessageCode.LOGOUT_REQ;
    }

    @Override
    public void logout(ByteBuffer byteBuffer) {
        Session currentSession = Server.getCurrentSession();
        log.info("{} logout", currentSession.getId());
        Message syncMessage = ByteMessage.sync(MessageCode.BROADCAST, ByteBuffer.wrap(currentSession.getId().toString().getBytes(StandardCharsets.UTF_8)));
        super.broadcast(new CallbackMessage(syncMessage) {
            @Override
            public void writeCallback(Session session) {}

            @Override
            public void exceptionCallback(SendException e, Session session) {}
        }, session -> !currentSession.getId().equals(session.getId()));
        Message asyncMessage = ByteMessage.async(MessageCode.BROADCAST, ByteBuffer.wrap(currentSession.getId().toString().getBytes(StandardCharsets.UTF_8)));
        super.broadcast(new CallbackMessage(asyncMessage), Group.get(GROUP_ONE));
    }

    @Override
    public void exceptionLogout(Exception e, Message message) {
        log.info("{}: exception logout", Server.getCurrentSession().getId());
    }
}

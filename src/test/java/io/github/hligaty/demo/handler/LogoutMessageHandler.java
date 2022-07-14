package io.github.hligaty.demo.handler;

import io.github.hligaty.Server;
import io.github.hligaty.cache.GroupCache;
import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.handler.AbstractLogoutMessageHandler;
import io.github.hligaty.message.Message;
import io.github.hligaty.message.DefaultMessage;
import io.github.hligaty.util.Session;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.github.hligaty.demo.BIOServerTest.GROUP_ONE;

@Slf4j
public class LogoutMessageHandler extends AbstractLogoutMessageHandler {

    @Override
    public int bindCode() {
        return MessageCode.LOGOUT_REQ;
    }

    @Override
    public void logout(ByteBuffer byteBuffer) {
        Session currentSession = Server.getCurrentSession();
        log.info("{} logout", currentSession.getId());
        Message message = new DefaultMessage(MessageCode.BROADCAST, ByteBuffer.wrap(currentSession.getId().toString().getBytes(StandardCharsets.UTF_8)));
        super.broadcast(message, session -> !Objects.equals(currentSession.getId(), session.getId()), null, null);
        super.asyncBroadcast(message, GroupCache.getGroup(GROUP_ONE), null, null);
    }

    @Override
    public void exceptionLogout(Exception e, Message message) {
        log.info("{}: exception logout", Server.getCurrentSession().getId());
    }
}

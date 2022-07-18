package io.github.hligaty.demo.handler;

import io.github.hligaty.Server;
import io.github.hligaty.cache.Group;
import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static io.github.hligaty.demo.BIOServerTest.GROUP_ONE;

/**
 * 登录实现
 */
public class LoginMessageHandler extends io.github.hligaty.handler.LoginMessageHandler {
    private static final String ROOT = "root-";

    @Override
    public int bindCode() {
        return MessageCode.LOGIN_REQ;
    }

    @Override
    public Object login(ByteBuffer byteBuffer) {
        ByteMessage message = ByteMessage.sync(MessageCode.LOGIN_RESP);
        String id = new String(byteBuffer.array());
        Session session = Server.getCurrentSession();
        boolean hasLogin = false;
        if (id.startsWith(ROOT)) {
            hasLogin = true;
            session.setAttachment("whitelist users");
            message.setByteBuffer(ByteBuffer.wrap("success".getBytes(StandardCharsets.UTF_8)));
        } else {
            message.setByteBuffer(ByteBuffer.wrap("fail".getBytes(StandardCharsets.UTF_8)));
        }
        try {
            session.send(message);
            if (Integer.parseInt(id.split("-")[1]) < 3) {
                Group.add(GROUP_ONE, id);
            }
        } catch (IOException e) {
            throw new RuntimeException("write error", e);
        }
        return hasLogin ? id : null;
    }
}

package io.github.hligaty.util;

import io.github.hligaty.Session;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session 工厂，负责创建/获取连接
 *
 * @author hligaty
 */
public class SessionFactory {
    private int sendBufferSize;
    private final Map<Object, Session> onLineList = new ConcurrentHashMap<>();

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public Session createSession(Socket socket) throws IOException {
        socket.setSendBufferSize(sendBufferSize);
        return new Session(socket);
    }

    public Session getSession(Object id) {
        return onLineList.get(id);
    }

    public Collection<Session> getAllSession() {
        return onLineList.values();
    }

    public void putSession(Session session) {
        Session prevSession = onLineList.put(session.getUserId(), session);
        if (prevSession != null) {
            prevSession.close();
        }
    }

    public void removeSession(Object userId, Session session) {
        onLineList.remove(userId, session);
    }
}

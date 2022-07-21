package io.github.hligaty.util;

import io.github.hligaty.Session;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Session 工厂，负责创建/获取连接
 *
 * @author hligaty
 */
public class SessionFactory {
    private int sendBufferSize;
    private final Map<Object, WeakReference<Session>> onLineList = new ConcurrentHashMap<>();

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public Session createSession(Socket socket) throws IOException {
        socket.setSendBufferSize(sendBufferSize);
        return new Session(socket);
    }

    public Session getSession(Object id) {
        WeakReference<Session> reference = onLineList.get(id);
        return reference == null ? null : reference.get();
    }

    public Stream<Session> getAllSession() {
        return onLineList.values().stream()
                .filter(Objects::nonNull)
                .map(Reference::get)
                .filter(Objects::nonNull);
    }

    public Session putSession(Session session) {
        WeakReference<Session> reference = onLineList.put(session.getId(), new WeakReference<>(session));
        Session prevSession;
        if (reference != null && (prevSession = reference.get()) != null) {
            return prevSession;
        }
        return null;
    }
}

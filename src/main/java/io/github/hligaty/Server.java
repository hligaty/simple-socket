package io.github.hligaty;

import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.exception.SimpleSocketIOException;
import io.github.hligaty.handler.*;
import io.github.hligaty.message.DefaultMessage;
import io.github.hligaty.util.NamedThreadFactory;
import io.github.hligaty.util.Session;
import io.github.hligaty.util.ThreadPerTaskExecutor;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Socket Server
 *
 * @author hligaty
 */
public class Server implements Closeable {
    private static final ThreadLocal<Session> sessionThreadLocal = ThreadLocal.withInitial(() -> null);
    private final CountDownLatch runState;
    private final ServerSocket serverSocket;
    private final int timeout;
    private int nThreads;
    /**
     * Receive connection request
     */
    private final Executor bossGroup;
    /**
     * Handle read event
     */
    private final Executor readerGroup;
    /**
     * Handle async write event
     */
    private final Executor writerGroup;
    private final RoutingMessageHandler delegateMessageHandler = new RoutingMessageHandler();
    private AbstractLoginMessageHandler loginMessageHandler;
    private AbstractLogoutMessageHandler logoutMessageHandler;
    private final Map<Object, WeakReference<Session>> onLineList = new ConcurrentHashMap<>();

    /**
     * @param address bind address.
     * @param timeout the specified timeout, in milliseconds.
     * @param nThreads accept thread size.
     * @throws IOException IO error when opening the socket.
     */
    public Server(InetSocketAddress address, int timeout, int nThreads) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(address);
        this.nThreads = nThreads;
        this.timeout = timeout;
        runState = new CountDownLatch(nThreads);
        bossGroup = new ThreadPerTaskExecutor(new NamedThreadFactory("bossGroup-"));
        readerGroup = new ThreadPerTaskExecutor(new NamedThreadFactory("readerGroup-"));
        writerGroup = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1024), new NamedThreadFactory("writerGroup-"), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * start server
     */
    public Server start() {
        for (; nThreads > 0; nThreads--) {
            bossGroup.execute(() -> {
                while (isRunning()) {
                    try {
                        Session session = new Session(serverSocket.accept(), timeout);
                        readerGroup.execute(() -> {
                            sessionThreadLocal.set(session);
                            DefaultMessage message = null;
                            try {
                                // Is a login message or has logged in
                                while (!session.isLogouted() && (loginMessageHandler.bindCode() == (message = session.receive()).getCode() || session.getId() != null)) {
                                    delegateMessageHandler.handleMessage(message);
                                }
                            } catch (SimpleSocketIOException | AutoSendException e) {
                                logoutMessageHandler.exceptionCaught(e, message);
                            } catch (LoginException ignored) {
                            } finally {
                                session.close();
                                sessionThreadLocal.remove();
                            }
                        });
                    } catch (Exception ignored) {
                    }
                }
                runState.countDown();
            });
        }
        return this;
    }

    /**
     * Returns the run state of the Socket Server.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return runState != null && runState.getCount() > 0;
    }

    /**
     * Register message Handler list.
     *
     * @param messageHandlers message Handler list
     */
    public final Server registerMessageHandlers(List<MessageHandler> messageHandlers) {
        messageHandlers.forEach(this::registerMessageHandler);
        return this;
    }

    /**
     * Register message Handler.
     *
     * @param messageHandler messageHandler
     */
    public final Server registerMessageHandler(MessageHandler messageHandler) {
        // prepare message Handler
        prepareMessageHandler(messageHandler);
        delegateMessageHandler.addMessageHandler(messageHandler);
        return this;
    }

    private void prepareMessageHandler(MessageHandler messageHandler) {
        if (messageHandler instanceof SpecialMessageHandler) {
            if (messageHandler instanceof AbstractMessageHandler) {
                ((AbstractMessageHandler) messageHandler).setWriterGroup(writerGroup);
            }
            if (messageHandler instanceof BroadcastCapableMessageHandler) {
                // Add online list for broadcast-capable message Handler
                ((BroadcastCapableMessageHandler) messageHandler).setOnLineList(onLineList);
                if (messageHandler instanceof AbstractLoginMessageHandler) {
                    // Get the login message Handler
                    loginMessageHandler = (AbstractLoginMessageHandler) messageHandler;
                } else if (messageHandler instanceof AbstractLogoutMessageHandler) {
                    // Get the logout message Handler
                    logoutMessageHandler = (AbstractLogoutMessageHandler) messageHandler;
                }
            }
        }
    }

    /**
     * @return current connect
     */
    public static Session getCurrentSession() {
        return sessionThreadLocal.get();
    }

    @Override
    public final void close() throws IOException {
        while (runState.getCount() != 0) {
            runState.countDown();
        }
        serverSocket.close();
    }
}

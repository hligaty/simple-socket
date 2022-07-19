package io.github.hligaty;

import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.exception.SimpleSocketIOException;
import io.github.hligaty.handler.*;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.util.EmptyObjects;
import io.github.hligaty.util.NamedThreadFactory;
import io.github.hligaty.util.ThreadPerTaskExecutor;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

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
    private final Executor workerGroup;
    private final RoutingMessageHandler delegateMessageHandler = new RoutingMessageHandler();
    private LoginMessageHandler loginMessageHandler;
    private LogoutMessageHandler logoutMessageHandler;
    private final ServerConfig serverConfig = new ServerConfig();
    private final SessionFactory sessionFactory = new SessionFactory();


    /**
     * @param address  bind address.
     * @param timeout  the specified timeout, in milliseconds.
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
        workerGroup = new ThreadPerTaskExecutor(new NamedThreadFactory("readerGroup-"));
    }

    /**
     * start server
     */
    public Server start() {
        prepare();
        doStart();
        flushSendBuffer();
        return this;
    }

    private void prepare() {
        sessionFactory.setSendBufferSize(serverConfig.getOption(ServerOption.SNDBUF_SIZE));
        //String packages = serverConfig.getOption(ServerOption.ANNOTATIONSCAN_PACKAGE);
        //Reflections reflections = new Reflections(packages, Scanners.values());
        //Set<Class<?>> controllerClasses = reflections.get(TypesAnnotated.with(Controller.class).asClass());
        //for (Class<?> controllerClass : controllerClasses) {
        //
        //}
    }

    private void doStart() {
        for (; nThreads > 0; nThreads--) {
            bossGroup.execute(() -> {
                while (isRunning()) {
                    try {
                        Session session = sessionFactory.createSession(serverSocket.accept());
                        workerGroup.execute(() -> handleMessage(session));
                    } catch (Exception ignored) {
                    }
                }
                runState.countDown();
            });
        }
    }

    /**
     * Returns the run state of the Socket Server.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return runState != null && runState.getCount() > 0;
    }

    private void handleMessage(Session session) {
        sessionThreadLocal.set(session);
        ByteMessage message = null;
        try {
            session.setTimeout(timeout);
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
    }

    private void flushSendBuffer() {
        int intervalTime = serverConfig.getOption(ServerOption.FLUSH_SNDBUF_INTERVAL);
        workerGroup.execute(() -> {
            while (isRunning()) {
                long start = System.currentTimeMillis();
                sessionFactory.getAllSession().forEach(session -> {
                    try {
                        session.send(EmptyObjects.EMPTY_MESSAGE);
                    } catch (SendException ignored) {
                    }
                });
                long sleepTime = start - System.currentTimeMillis() + intervalTime;
                try {
                    Thread.sleep(sleepTime > 0 ? sleepTime : 0);
                } catch (InterruptedException ignored) {
                }
            }
        });
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
            if (messageHandler instanceof BroadcastCapableMessageHandlerSupport) {
                // Add online list for broadcast-capable message Handler
                ((BroadcastCapableMessageHandlerSupport) messageHandler).setSessionFactory(sessionFactory);
                if (messageHandler instanceof LoginMessageHandler) {
                    // Get the login message Handler
                    loginMessageHandler = (LoginMessageHandler) messageHandler;
                } else if (messageHandler instanceof LogoutMessageHandler) {
                    // Get the logout message Handler
                    logoutMessageHandler = (LogoutMessageHandler) messageHandler;
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

    public <T> Server option(ServerOption<T> option, T value) {
        serverConfig.setOption(option, value);
        return this;
    }

    @Override
    public final void close() throws IOException {
        while (runState.getCount() != 0) {
            runState.countDown();
        }
        serverSocket.close();
    }
}

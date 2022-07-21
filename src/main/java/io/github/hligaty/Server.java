package io.github.hligaty;

import io.github.hligaty.annotation.SocketController;
import io.github.hligaty.exception.AutoSendException;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.exception.SimpleSocketIOException;
import io.github.hligaty.handler.*;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * BIO 实现的 Socket 服务器
 *
 * @author hligaty
 */
public class Server implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final ThreadLocal<Session> sessionThreadLocal = ThreadLocal.withInitial(() -> null);
    private CountDownLatch runState;
    private final ServerSocket serverSocket;
    /**
     * 负责接收连接的线程池
     */
    private final Executor bossGroup;
    /**
     * 负责处理读事件和异步刷新 Socket 缓冲区的线程池
     */
    private final Executor workerGroup;
    /**
     * 消息处理的委托类
     */
    private final RoutingMessageHandler delegateMessageHandler = new RoutingMessageHandler();
    /**
     * 登入消息处理器
     */
    private LoginMessageHandler loginMessageHandler;
    /**
     * 登出消息处理器
     */
    private LogoutMessageHandler logoutMessageHandler;
    /**
     * 配置信息
     */
    private final ServerConfig serverConfig = new ServerConfig();
    /**
     * 会话工厂
     */
    private final SessionFactory sessionFactory = new SessionFactory();
    private Timer flushSendBufferTimer;


    /**
     * @param address 绑定的地址
     * @throws IOException 打开 Socket 时出现 IO 错误
     */
    public Server(InetSocketAddress address) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(address);
        bossGroup = new ThreadPerTaskExecutor(new NamedThreadFactory("bossGroup-"));
        workerGroup = new ThreadPerTaskExecutor(new NamedThreadFactory("readerGroup-"));
    }

    /**
     * 启动服务器
     */
    public Server start() {
        // 设置服务器的参数，扫描包并注册相关消息处理器
        prepare();
        // 启动异步刷新发送缓冲区
        flushSendBuffer();
        // 启动服务器
        doStart();
        return this;
    }

    private void prepare() {
        sessionFactory.setSendBufferSize(serverConfig.getOption(ServerOption.SNDBUF_SIZE));
        // 寻找没有通过 registerMessageHandler() 注册的消息处理器
        // 使用 SpringBoot 时通过 Spring 容器获取
        // 没使用 SpringBoot 时通过反射获取
        Collection<MessageHandler> messageHandlers = ComponentScanUtils.annotationScan(serverConfig.getOption(ServerOption.ANNOTATIONSCAN_PACKAGE), sessionFactory);
        registerMessageHandlers(messageHandlers);
        // 检查是否有登入和登出消息处理器的实现
        Assert.isTrue(loginMessageHandler == null || logoutMessageHandler == null, "loginMessageHandler and logoutMessageHandler must implemented");
    }


    private void doStart() {
        int nThreads = serverConfig.getOption(ServerOption.BOSS_THREAD_NUMBER);
        runState = new CountDownLatch(nThreads);
        for (; nThreads > 0; nThreads--) {
            // 开启 nThreads 个线程用于接收连接
            bossGroup.execute(() -> {
                while (isRunning()) {
                    try {
                        Session session = sessionFactory.createSession(serverSocket.accept());
                        // BIO，为每个连接创建一个单独的线程去处理读写事件
                        workerGroup.execute(() -> handleMessage(session));
                    } catch (Exception e) {
                        log.warn("failed to accept connect", e);
                    }
                }
                runState.countDown();
            });
        }
    }

    /**
     * 判断是否运行
     *
     * @return 返回 true 即正在运行
     */
    public boolean isRunning() {
        return runState != null && runState.getCount() > 0;
    }

    private void handleMessage(Session session) {
        // 使用 ThreadLocal 后 MessageHandler 的 handle 方法不需要传入 Session，需要使用时通过 Server.getcurrentSession 获取就可以了
        sessionThreadLocal.set(session);
        ByteMessage message = null;
        try {
            session.setTimeout(serverConfig.getOption(ServerOption.TIMEOUT));
            // 是登入消息或者已经登入了
            while (loginMessageHandler.bindCode() == (message = session.receive()).getCode() || session.getId() != null) {
                delegateMessageHandler.handleMessage(message);
            }
        } catch (SimpleSocketIOException | AutoSendException e) {
            // 接收或发送消息时错误，断开连接
            logoutMessageHandler.exceptionCaught(e, message);
        } catch (LoginException ignored) {
        } finally {
            session.close();
            sessionThreadLocal.remove();
        }
    }

    private void flushSendBuffer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isRunning()) {
                    sessionFactory.getAllSession().forEach(session -> {
                        try {
                            session.send(EmptyObjects.EMPTY_MESSAGE);
                        } catch (SendException ignored) {
                        }
                    });
                }
            }
        };
        flushSendBufferTimer = new Timer("flush-send-buffer");
        flushSendBufferTimer.schedule(task, 0, serverConfig.getOption(ServerOption.FLUSH_SNDBUF_INTERVAL));
    }

    /**
     * 注册 MessageHandler
     *
     * @param messageHandlers messageHandler 列表
     */
    public final Server registerMessageHandlers(Collection<MessageHandler> messageHandlers) {
        messageHandlers.forEach(this::registerMessageHandler);
        return this;
    }

    /**
     * 注册 MessageHandler
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
        Assert.isTrue(messageHandler.getClass().isAnnotationPresent(SocketController.class), "@controller must not be on MessageHandler");
        if (!(messageHandler instanceof SpecialMessageHandler)) {
            return;
        }
        // 处理需要再加工的消息处理器
        if (messageHandler instanceof BroadcastMessageSupport) {
            // 有广播能力，注入 SessionFactory
            ((BroadcastMessageSupport) messageHandler).setSessionFactory(sessionFactory);
        }
        if (messageHandler instanceof LoginMessageHandler) {
            // 获取到登入消息处理器
            loginMessageHandler = (LoginMessageHandler) messageHandler;
        } else if (messageHandler instanceof LogoutMessageHandler) {
            // 获取到登出消息处理器
            logoutMessageHandler = (LogoutMessageHandler) messageHandler;
        }
    }

    /**
     * @return 当前的会话
     */
    public static Session getCurrentSession() {
        return sessionThreadLocal.get();
    }

    /**
     * 设置服务器参数
     *
     * @param option 参数选项
     * @param value 参数值
     * @param <T> 参数类型
     * @return 被设置的服务器
     */
    public <T> Server option(ServerOption<T> option, T value) {
        serverConfig.setOption(option, value);
        return this;
    }

    @Override
    public final void close() throws IOException {
        while (runState.getCount() != 0) {
            runState.countDown();
        }
        flushSendBufferTimer.cancel();
        serverSocket.close();
    }
}

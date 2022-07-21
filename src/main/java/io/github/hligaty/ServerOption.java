package io.github.hligaty;

import io.github.hligaty.util.Assert;

/**
 * 服务器参数
 *
 * @param <T> 参数类型
 * @author hligaty
 */
public abstract class ServerOption<T> {
    private ServerOption() {
    }

    public abstract void validate(T t);

    // 异步刷新 Socket 发送缓冲区的时间间隔，实际每个连接的缓冲区刷新时间 = 该时间间隔 + 其他连接缓冲区刷新的耗时
    public static final ServerOption<Integer> FLUSH_SNDBUF_INTERVAL = new ServerOption<Integer>() {
        @Override
        public void validate(Integer intervalTime) {
            Assert.isTrue(intervalTime == null || intervalTime <= 0, "intervalTime must be > 0");
        }
    };
    // 注解扫描的目录
    public static final ServerOption<String> ANNOTATIONSCAN_PACKAGE = new ServerOption<String>() {
        @Override
        public void validate(String packages) {
            Assert.isTrue(packages == null || packages.isEmpty(), "packages must not be empty");
        }
    };
    // Socket 连接的发送缓冲区大小
    public static final ServerOption<Integer> SNDBUF_SIZE = new ServerOption<Integer>() {
        @Override
        public void validate(Integer sendBufferSize) {
            Assert.isTrue(sendBufferSize == null || sendBufferSize <= 0, "basePackages must be > 0");
        }
    };
    // Socket 连接在多久未收到消息后主动断开连接，为 0 表示不主动断开来凝结
    public static final ServerOption<Integer> TIMEOUT = new ServerOption<Integer>() {
        @Override
        public void validate(Integer timeout) {
            Assert.isTrue(timeout == null || timeout < 0, "time must be >= 0");
        }
    };
    // 同时接收连接的线程数
    public static final ServerOption<Integer> BOSS_THREAD_NUMBER = new ServerOption<Integer>() {
        @Override
        public void validate(Integer bossThreadNumber) {
            Assert.isTrue(bossThreadNumber == null || bossThreadNumber <= 0, "time must be > 0");
        }
    };
}

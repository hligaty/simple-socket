package io.github.hligaty;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器配置类
 *
 * @author hligaty
 */
public final class ServerConfig {
    private static final int SNDBUF_SIZE = 65535;
    private static final int FLUSH_SNDBUF_INTERVAL = 100;
    private static final String ANNOTATIONSCAN_PACKAGE = "io.github.hligaty";
    private static final int TIMEOUT = 0;
    private static final int BOSS_THREAD_NUMBER = 1;
    private final Map<ServerOption<?>, Object> options = new HashMap<>();

    public ServerConfig() {
        options.put(ServerOption.FLUSH_SNDBUF_INTERVAL, FLUSH_SNDBUF_INTERVAL);
        options.put(ServerOption.ANNOTATIONSCAN_PACKAGE, ANNOTATIONSCAN_PACKAGE);
        options.put(ServerOption.SNDBUF_SIZE, SNDBUF_SIZE);
        options.put(ServerOption.TIMEOUT, TIMEOUT);
        options.put(ServerOption.BOSS_THREAD_NUMBER, BOSS_THREAD_NUMBER);
    }

    public <T> void setOption(ServerOption<T> option, T value) {
        option.validate(value);
        options.put(option, value);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getOption(ServerOption<T> option) {
        return (T) options.get(option);
    }
}

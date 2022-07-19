package io.github.hligaty;

public abstract class ServerOption<T> {
    private final String name;

    private ServerOption(String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    public abstract void validate(T t);

    public static final ServerOption<Integer> FLUSH_SNDBUF_INTERVAL = new ServerOption<Integer>("FLUSH_SNDBUF_INTERVAL") {
        @Override
        public void validate(Integer intervalTime) {
            if (intervalTime == null || intervalTime <= 0) {
                throw new IllegalArgumentException("intervalTime must be > 0");
            }
        }
    };
    public static final ServerOption<String> ANNOTATIONSCAN_PACKAGE = new ServerOption<String>("ANNOTATIONSCAN_PACKAGE") {
        @Override
        public void validate(String packages) {
            if (packages == null || packages.isEmpty()) {
                throw new IllegalArgumentException("packages must not be empty");
            }
        }
    };
    public static final ServerOption<Integer> SNDBUF_SIZE = new ServerOption<Integer>("SNDBUF_SIZE") {
        @Override
        public void validate(Integer sendBufferSize) {
            if (sendBufferSize == null || sendBufferSize <= 0) {
                throw new IllegalArgumentException("basePackages must be > 0");
            }
        }
    };
}

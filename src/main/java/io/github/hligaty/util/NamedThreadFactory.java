package io.github.hligaty.util;

import java.util.concurrent.ThreadFactory;

/**
 * @author hligaty
 */
public final class NamedThreadFactory implements ThreadFactory {
    private final String threadNamePrefix;

    public NamedThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, threadNamePrefix);
    }
}

package io.github.hligaty.util;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

/**
 * 可命名的线程工厂
 *
 * @author hligaty
 */
public final class NamedThreadFactory implements ThreadFactory {
    private final String threadNamePrefix;

    public NamedThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        return new Thread(r, threadNamePrefix);
    }
}

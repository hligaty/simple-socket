package io.github.hligaty.util;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * 为每个任务都创建一个线程的线程池
 *
 * @author hligaty
 */
public final class ThreadPerTaskExecutor implements Executor {
    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        threadFactory.newThread(command).start();
    }
}

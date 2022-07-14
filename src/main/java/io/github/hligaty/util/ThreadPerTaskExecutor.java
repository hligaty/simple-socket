package io.github.hligaty.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * @author hligaty
 */
public final class ThreadPerTaskExecutor implements Executor {
    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
    }
}

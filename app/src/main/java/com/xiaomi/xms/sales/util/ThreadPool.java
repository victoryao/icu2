package com.xiaomi.xms.sales.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static ExecutorService sExecutor;
    private static int THREAD_POOL_COUNT = 10;

    static {
        sExecutor = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
    }

    public static void execute(Runnable runnable) {
        sExecutor.execute(runnable);
    }
}

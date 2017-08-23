package com.fighter.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Global thread pool
 *
 * Created by zhangjg on 17-8-23.
 */
public class GlobalThreadPool {

    private static final int FIXED_THREAD_POOL_COUNT = 5;

    private static ExecutorService sFixedThreadPool;
    private static ExecutorService sCachedThreadPool;
    private static ExecutorService sSingleThreadPool;

    public static synchronized ExecutorService getFixedThreadPool() {
        if (sFixedThreadPool == null) {
            sFixedThreadPool = Executors.newFixedThreadPool(FIXED_THREAD_POOL_COUNT);
        }
        return sFixedThreadPool;
    }

    public static synchronized ExecutorService getCachedThreadPool() {
        if (sCachedThreadPool == null) {
            sCachedThreadPool = Executors.newCachedThreadPool();
        }
        return sCachedThreadPool;
    }

    public static synchronized ExecutorService getSingleThreadPool() {
        if (sSingleThreadPool == null) {
            sSingleThreadPool = Executors.newSingleThreadExecutor();
        }
        return sSingleThreadPool;
    }
}

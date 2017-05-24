package com.fighter.wrapper;

import com.fighter.common.utils.ThreadPoolUtils;

enum AdThreadPool {
    INSTANCE;

    private ThreadPoolUtils mThreadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.SingleThread, 1);

    public ThreadPoolUtils getThreadPoolUtils() {
        return mThreadPoolUtils;
    }
}

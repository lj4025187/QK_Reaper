package com.fighter.wrapper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 所有广告SDK使用同一份OkHttpClient，减少对网络资源的占用。
 */
enum AdOkHttpClient {
    INSTANCE;

    private OkHttpClient mClient = new OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .build();

    public OkHttpClient getOkHttpClient() {
        return mClient;
    }
}

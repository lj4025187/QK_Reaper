package com.fighter.wrapper;

import okhttp3.OkHttpClient;

/**
 * 所有广告SDK使用同一份OkHttpClient，减少对网络资源的占用。
 */
enum AdOkHttpClient {
    INSTANCE;

    private OkHttpClient mClient = new OkHttpClient();

    public OkHttpClient getOkHttpClient() {
        return mClient;
    }
}

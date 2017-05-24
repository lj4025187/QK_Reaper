package com.fighter.wrapper.download;

import android.content.Context;

import com.fighter.cache.AdFileCacheUtil;
import com.fighter.common.utils.CloseUtils;

import java.io.File;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpDownloader {
    private static final long MAX_SIZE = 2 * 1024 * 1024; // Max cache size 2M

    private OkHttpClient mClient;
    private AdFileCacheUtil mAdFileCacheUtil;

    public OkHttpDownloader(Context context, OkHttpClient client) {
        mClient = client;
        mAdFileCacheUtil = AdFileCacheUtil.getInstance(context, MAX_SIZE);
    }

    public File downloadSync(Request request, String destFileDir, String destFileName,
                             boolean keepExtension) {
        Response response = null;

        try {
            // Clear cache
            mAdFileCacheUtil.clearCacheFile(new File(destFileDir));

            // Start download
            response = mClient.newCall(request).execute();
            FileConvert convert = new FileConvert(destFileDir, destFileName, keepExtension);
            return convert.convert(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }

        return null;
    }
}

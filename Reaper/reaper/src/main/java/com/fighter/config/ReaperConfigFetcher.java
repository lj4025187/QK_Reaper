package com.fighter.config;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;
import com.fighter.config.db.ReaperConfigDB;
import com.fighter.reaper.BumpVersion;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Reaper config fetcher
 *
 * fetch config data and save them to local database
 *
 * Created by zhangjg on 17-5-10.
 */
public class ReaperConfigFetcher {

    private static final String TAG = "ReaperConfigFetcher";

    /**
     * Fetch config data from config server.
     * It is a sync request.
     *
     * <p>
     * If fail, then retry it
     *
     * @param context
     * @param pkg
     * @param salt
     * @param appKey
     * @return
     */
    public static boolean fetchWithRetry(Context context, String pkg,
                                         String salt, String appKey, String appId) {
        boolean result = fetch(context, pkg, salt, appKey, appId);
        if (result) {
            return true;
        }
        // retry
        for (int i = 0; i < ReaperConfig.RETRY_TIMES; i++) {
            ReaperLog.i(TAG, "fetch . ================= start retry =======================");
            result = fetch(context, pkg, salt, appKey, appId);
            if (result) {
                return true;
            }
        }

        return false;

    }

    /**
     * Fetch config data from config server
     * It is a sync request
     *
     * @param context
     * @param pkg
     * @param salt
     * @param appKey
     * @return
     */
    public static boolean fetch(Context context, String pkg,
                                String salt, String appKey, String appId) {

        String baseUrl = ReaperConfig.TEST ? ReaperConfig.TEST_URL_HTTPS : ReaperConfig.URL_HTTPS;
        String sdkVersion = ReaperConfig.TEST ? ReaperConfig.TEST_SDK_VERSION : BumpVersion.value();
        String url = baseUrl +
                "?" + ReaperConfig.KEY_URL_PARAM_SDK_VERSION + "=" + sdkVersion +
                "&" + ReaperConfig.KEY_URL_PARAM_ID + "=" + appId;

        ReaperLog.i(TAG, "fetch . url is : " + url);
        byte[] reqBodyData =
                ReaperConfigHttpHelper.getConfigRequestBody(context, pkg, salt, appKey);

        OkHttpClient okHttpClient = ReaperConfigHttpHelper.getHttpsClient();
        if (okHttpClient == null) {
            okHttpClient = ReaperConfigHttpHelper.getHttpClient();
        }

        if (okHttpClient == null) {
            ReaperLog.i(TAG, "fetch error, http client init fail");
            return false;
        }

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), reqBodyData);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            ReaperLog.i(TAG, "fetch . after execute. Response : " + response);
            if (response == null) {
                return false;
            }
            if (response.code() != 200) {
                return false;
            }
            ResponseBody resBody = response.body();
            if (resBody == null) {
                return false;
            }
            byte[] resBodyData = resBody.bytes();
            onFetchComplete(context, resBodyData, salt + appKey);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Save config data and current time
     *
     * @param context
     * @param responseBody
     * @return
     */
    private static void onFetchComplete(Context context, byte[] responseBody, String key) {
        ReaperConfigHttpHelper.recordLastSuccessTime(context);
        List<ReaperAdvPos> posList =
                ReaperConfigHttpHelper.parseResponseBody(context, responseBody, key);
        ReaperConfigDB.getInstance(context).saveReaperAdvPos(posList);
    }

}


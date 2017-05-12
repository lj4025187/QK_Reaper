package com.fighter.config;

import android.content.Context;

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

    /**
     * Fetch config data from config server.
     *
     * If fail, then retry it
     *
     * @param context
     * @param pkg
     * @param salt
     * @param appKey
     * @return
     */
    public static boolean fetchWithRetry(Context context, String pkg, String salt, String appKey) {
        boolean result = fetch(context, pkg, salt, appKey);
        if (result) {
            return true;
        }
        // retry
        for (int i = 0; i < ReaperConfig.RETRY_TIMES; i++) {
            result = fetch(context, pkg, salt, appKey);
            if (result) {
                return true;
            }
        }

        return false;

    }

    /**
     * Fetch config data from config server
     *
     * @param context
     * @param pkg
     * @param salt
     * @param appKey
     * @return
     */
    public static boolean fetch(Context context, String pkg, String salt, String appKey) {
        String requestBodyStr =
                ReaperConfigUtils.getConfigRequestBody(context, pkg, salt, appKey);
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), requestBodyStr);
        Request request = new Request.Builder()
                .url(ReaperConfig.URL_HTTPS)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response == null) {
                return false;
            }
            if (response.code() != 200) {
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                return false;
            }
            String text = body.string();
            return onFetchComplete(context, text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Save config data
     *
     * @param context
     * @param responseBody
     * @return
     */
    private static boolean onFetchComplete(Context context, String responseBody) {
        List<ReaperAdvPos> posList = ReaperConfigUtils.parseResponseBody(responseBody);
        return ReaperConfigUtils.saveConfigToDB(context, posList);
    }

}
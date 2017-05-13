package com.fighter.config;

import android.content.Context;

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
    public static boolean fetchWithRetry(Context context, String pkg,
                                         String salt, String appKey, String appId) {
        boolean result = fetch(context, pkg, salt, appKey, appId);
        if (result) {
            return true;
        }
        // retry
        for (int i = 0; i < ReaperConfig.RETRY_TIMES; i++) {
            result = fetch(context, pkg, salt, appKey, appId);
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
    public static boolean fetch(Context context, String pkg,
                                String salt, String appKey, String appId) {
        String url = ReaperConfig.URL_HTTPS +
                "?" + ReaperConfig.KEY_URL_PARAM_SDK_VERSION + "=" + BumpVersion.value() +
                "&" + ReaperConfig.KEY_URL_PARAM_ID + "=" + appId;
        String requestBodyStr =
                ReaperConfigUtils.getConfigRequestBody(context, pkg, salt, appKey);
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), requestBodyStr);
        Request request = new Request.Builder()
                .url(url)
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
            onFetchComplete(context, text);
            return true;
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
    private static void onFetchComplete(Context context, String responseBody) {
        List<ReaperAdvPos> posList = ReaperConfigUtils.parseResponseBody(responseBody);
        ReaperConfigUtils.saveConfigToDB(context, posList);
    }

}
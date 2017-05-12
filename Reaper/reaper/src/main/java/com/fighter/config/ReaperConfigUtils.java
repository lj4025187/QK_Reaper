package com.fighter.config;


import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;
import com.fighter.common.utils.ReaperLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Some utils for handle configs
 *
 * Created by zhangjg on 17-5-10.
 */

public final class ReaperConfigUtils {

    private static final String TAG = "ReaperConfigUtils";


    /**
     * Return json String used as post body to request config
     *
     * @return encrypt json string
     */

    /**
     * Return json String used as post body to request config
     *
     * @param context app context
     * @param pkg app package name
     * @param salt it is allocated by config server, it is relative to sdk version
     * @param appKey it is allocated by config server for every app
     * @return encrypt json string used as post body
     */
    public static String getConfigRequestBody(Context context, String pkg, String salt, String appKey) {
        if (context == null) {
            throw new IllegalArgumentException("context is null !!!");
        }
        if (TextUtils.isEmpty(pkg)) {
            throw new IllegalArgumentException("package is empty !!!");
        }
        ReaperConfigRequestBody body = ReaperConfigRequestBody.create(context, pkg);
        String oriJson = body.toJson();
        String encryptKey = salt + appKey;
        IRC4 rc4 = RC4Factory.create(encryptKey);
        return rc4.encryptToBase64(oriJson);
    }

    public static List<ReaperAdvPos> parseResponseBody (String responseBody) {

        //TODO : decrypt response body first
        ReaperLog.i(TAG, "parseResponseBody");

        JSONObject responseObj = JSON.parseObject(responseBody);
        String result = responseObj.getString(ReaperConfig.KEY_RES_RESULT);
        if ("ok".equals(result)) {
            ReaperLog.i(TAG, "parseResponseBody ok");

            String nextTime = responseObj.getString(ReaperConfig.KEY_RES_NEXT_TIME);
            ReaperLog.i(TAG, "parseResponseBody . next time : " + nextTime);
            JSONArray posArray = responseObj.getJSONArray(ReaperConfig.KEY_RES_POS_IDS);
            int posSize = posArray.size();
            ArrayList<ReaperAdvPos> posList = new ArrayList<>(posSize);
            ReaperLog.i(TAG, "parseResponseBody pos size : " + posSize);
            for (int i = 0; i < posSize; i++) {
                JSONObject posObj = posArray.getJSONObject(i);
                ReaperAdvPos pos = posObj.toJavaObject(ReaperAdvPos.class);
                ReaperLog.i(TAG, "parse ReaperAdvPos : " + pos);
                JSONArray adsenseArray = posObj.getJSONArray(ReaperConfig.KEY_RES_ADSENSES);
                int adsenseSize = adsenseArray.size();
                ReaperLog.i(TAG, "    parse adsense size : " + adsenseSize);
                for (int j = 0; j < adsenseSize; j++) {
                    JSONObject adsenseObj = adsenseArray.getJSONObject(j);
                    ReaperAdSense adSense = adsenseObj.toJavaObject(ReaperAdSense.class);
                    ReaperLog.i(TAG, "    parse adsense  : " + adSense);
                    pos.addAdSense(adSense);
                }
                posList.add(pos);
            }
            return posList;
        } else if ("error".equals(result)) {
            String reason = responseObj.getString(ReaperConfig.KEY_RES_REASON);
            ReaperLog.i(TAG, "parseResponseBody . get config error : " + reason);
        }
        return null;
    }

    public static boolean saveConfigToDB(Context context, List<ReaperAdvPos> posList) {
        return false;
    }

}

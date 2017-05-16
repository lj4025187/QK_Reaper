package com.fighter.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fighter.common.utils.ReaperLog;
import com.qiku.serversdk.custom.AppConf;


import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by huayang on 17-5-10.
 */

public class ReaperNetwork {
    private static final String TAG = ReaperNetwork.class.getSimpleName();
    private static final boolean DEBUG_DOWNLOAD = true;

    private static final int REAPER_VERSION_CHECK_NEW_VERSION = 1;
    private static final int REAPER_VERSION_CHECK_SAME_VERSION = 0;
    private static final int REAPER_VERSION_CHECK_FAILED = -1;

    private static final int COMPARE_SERVER_HIGHER = 1;
    private static final int COMPARE_CURRENT_HIGHER = -1;
    private static final int COMPARE_EQUALS = 0;
    private static final int COMPARE_FAILED = -10;

    private static final String URL_REAPER_DOWNLOAD = "";
    private static Context sContext;

    /**
     * Check higher version
     * @return
     * 1 = check success and has higher version
     * 0 = check success and dont have higher version
     * -1 = check failed
     */
    public static int doQuery(String version) {
        if (TextUtils.isEmpty(version) || !isValidVersion(version))
            return REAPER_VERSION_CHECK_FAILED;

        String serverVersion = queryVersion();
        if (!isValidVersion(serverVersion)) {
            if (DEBUG_DOWNLOAD) {
                ReaperLog.e(TAG, "query a bad version. : " + serverVersion);
            }
            return REAPER_VERSION_CHECK_FAILED;
        }

        int compared = compareVersion(version, serverVersion);
        ReaperLog.e(TAG, "compared : " + compared);
        if (compared != COMPARE_SERVER_HIGHER) {
            if (compared == COMPARE_FAILED) {
                return REAPER_VERSION_CHECK_FAILED;
            }
            return REAPER_VERSION_CHECK_SAME_VERSION;
        } else {
            return REAPER_VERSION_CHECK_NEW_VERSION;
        }
    }

    private static String queryVersion() {
        try {
            AppConf ac = new AppConf("{'baseUrl':'https://api.os.qiku.com','resourceUrl':'api/list'}");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("app", "Reaper"); // 设置app
            params.put("version", "1.0.0"); // 设置version
            params.put("api", "version"); // 设置api
            //params.put("time", "1494922168");
            SharedPreferences sp = ReaperEnv.sContext.getSharedPreferences("aa", Context.MODE_PRIVATE);
            String versionTime = sp.getString("version_time", "0");
            params.put("time", versionTime);
            ReaperLog.e(TAG, "versionTime : " + versionTime);
            JSONObject result = ac.getAppConfSyncCustom(params);
            if (result == null) {
                if (DEBUG_DOWNLOAD)
                    ReaperLog.e(TAG, "getAppConfSyncCustom == null");
                return null;
            }
            String jsonString = result.toString();
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonString);
            String ret = jsonObject.getString("result");
            com.alibaba.fastjson.JSONObject obj = jsonObject.getJSONObject("list");
            JSONArray data = obj.getJSONArray("data");
            for (int i = 0; i < data.size(); ++i) {
                Object o = data.get(i);
                if (!(o instanceof JSONArray)) {
                    continue;
                }
                JSONArray dataArray = ((JSONArray)o);
                String version = (String) dataArray.get(1);
                String url = (String) dataArray.get(2);

                ReaperLog.e(TAG, "version : " + version + "; url : " + url);
            }
            //ReaperLog.e(TAG, "json : " + ret + "---" + data);

        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "err : " + e.getMessage());
        }
        return "1.0.1";
    }

    private static boolean downloadHigherVersionReaper() {
//        if (ReaperNetwork.sHttpsUtil == null) {
//            if (DEBUG_DOWNLOAD)
//                ReaperLog.e(TAG, "downloadReaper, HttpsUtil == null !");
//            return false;
//        }
//        Response response = ReaperNetwork.sHttpsUtil.requestSync(URL_REAPER_DOWNLOAD);
//        if (response == null || !response.isSuccessful()) {
//            if (DEBUG_DOWNLOAD)
//                ReaperLog.e(TAG, "downloadReaper, response == null or unsuccessful !");
//            return false;
//        }
//        ResponseBody body = response.body();
//        if (body == null) {
//            if (DEBUG_DOWNLOAD)
//                ReaperLog.e(TAG, "downloadReaper, body == null !");
//            return false;
//        }
//        InputStream is = body.byteStream();
//        if (is == null) {
//            if (DEBUG_DOWNLOAD)
//                ReaperLog.e(TAG, "downloadReaper, is == null !");
//            return false;
//        }
//
//        //test start
//        String bodyString = null;
//        try {
//            bodyString = body.string();
//            ReaperLog.e(TAG, "bodyString : " + bodyString);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bodyString != null;
        //test end

        return false;
    }

    /**
     *
     * @param currentVersion
     * @param serverVersion
     * @return
     * 1, when serverVersion > currentVersion
     * 0, when serverVersion = currentVersion
     * -1, when serverVersion < currentVersion
     * -10, error
     */
    private static int compareVersion(String currentVersion, String serverVersion) {
        String[] currentSplitStr = splitVersionStr(currentVersion);
        String[] serverSplitStr = splitVersionStr(serverVersion);
        if (currentSplitStr == null || serverSplitStr == null)
            return COMPARE_FAILED;

        try {
            int currentRelease = Integer.valueOf(currentSplitStr[0]);
            int currentSecond = Integer.valueOf(currentSplitStr[1]);
            int currentRevision = Integer.valueOf(currentSplitStr[2]);

            int serverRelease = Integer.valueOf(serverSplitStr[0]);
            int serverSecond = Integer.valueOf(serverSplitStr[1]);
            int serverRevision = Integer.valueOf(serverSplitStr[2]);

            if (DEBUG_DOWNLOAD) {
                ReaperLog.e(TAG, "server : " + getSplitString(serverSplitStr));
                ReaperLog.e(TAG, "current : " + getSplitString(currentSplitStr));
            }
            if (serverRelease > currentRelease) {
                return COMPARE_SERVER_HIGHER;
            } else if (serverRelease < currentRelease){
                return COMPARE_CURRENT_HIGHER;
            }

            //serverRelease == currentRelease
            if (serverSecond > currentSecond) {
                return COMPARE_SERVER_HIGHER;
            } else if (serverSecond < currentSecond) {
                return COMPARE_CURRENT_HIGHER;
            }

            //serverSecond == currentSecond
            if (serverRevision > currentRevision) {
                return COMPARE_SERVER_HIGHER;
            } else if (serverRevision < currentRevision) {
                return COMPARE_CURRENT_HIGHER;
            }

            //serverRevision == currentRevision
            if (serverSplitStr.length != 4 || currentSplitStr.length != 4) {
                return COMPARE_EQUALS;
            }

            //both has suffix
            String alpha = "-alpha";
            String beta = "-beta";
            String stable = "-stable";
            if (TextUtils.isEmpty(serverSplitStr[3])
                    || TextUtils.isEmpty(currentSplitStr[3])
                    || TextUtils.equals(serverSplitStr[3], currentSplitStr[3])) {
                return COMPARE_EQUALS;
            }
            boolean serverAlpha = TextUtils.equals(serverSplitStr[3], alpha);
            boolean serverBeta = TextUtils.equals(serverSplitStr[3], beta);
            boolean serverStable = TextUtils.equals(serverSplitStr[3], stable);
            boolean currentAlpha = TextUtils.equals(currentSplitStr[3], alpha);
            boolean currentBeta = TextUtils.equals(currentSplitStr[3], beta);
            boolean currentStable = TextUtils.equals(currentSplitStr[3], stable);
            if ((serverStable && (currentBeta || currentAlpha)) ||
                    (serverBeta && currentAlpha)) {
                return COMPARE_SERVER_HIGHER;
            } else if ((currentStable && (serverBeta || serverAlpha)) ||
                    (currentBeta && serverAlpha)) {
                return COMPARE_CURRENT_HIGHER;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return COMPARE_FAILED;
        }
        return COMPARE_FAILED;
    }

    private static boolean isValidVersion(String version) {
        String[] splitVerStr = splitVersionStr(version);
        if (splitVerStr == null)
            return false;
        try {
            Integer.valueOf(splitVerStr[0]);
            Integer.valueOf(splitVerStr[1]);
            Integer.valueOf(splitVerStr[2]);
            return true;
        } catch (Exception e){
            if (DEBUG_DOWNLOAD) {
                ReaperLog.e(TAG, "err msg : " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    private static String[] splitVersionStr(String version) {
        if (TextUtils.isEmpty(version))
            return null;
        String[] split = version.split("\\.");
        if (split.length < 3)
            return null;
        if (split[2].contains("-")) {
            String[] last = split[2].split("-");
            if (last.length != 2) {
                if (DEBUG_DOWNLOAD) {
                    ReaperLog.e(TAG, "splitVersionStr error !");
                }
                return null;
            }
            return new String[] {split[0], split[1], last[0], "-" + last[1]};
        }

        return split;
    }

    private static String getSplitString(String[] split) {
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(s).append(".");
        }
        return sb.toString();
    }
}

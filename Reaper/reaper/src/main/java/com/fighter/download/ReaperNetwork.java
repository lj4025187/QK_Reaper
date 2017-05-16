package com.fighter.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fighter.common.utils.ReaperLog;
import com.qiku.serversdk.custom.AppConf;


import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huayang on 17-5-10.
 */

public class ReaperNetwork {
    private static final String TAG = ReaperNetwork.class.getSimpleName();
    private static final String SP_REAPER_NETWORK = "reaper_network";
    private static final String KEY_TIME = "reaper_time";

    private static final boolean DEBUG_DOWNLOAD = true;

    private static final int REAPER_VERSION_CHECK_NEW_VERSION = 1;
    private static final int REAPER_VERSION_CHECK_SAME_VERSION = 0;
    private static final int REAPER_VERSION_CHECK_FAILED = -1;

    private static final int COMPARE_SERVER_HIGHER = 1;
    private static final int COMPARE_CURRENT_HIGHER = -1;
    private static final int COMPARE_EQUALS = 0;
    private static final int COMPARE_FAILED = -10;

    private static final String URL_REAPER_DOWNLOAD = "";


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

        VersionPiece piece = queryVersion();
        if (piece == null || TextUtils.isEmpty(piece.version)) {
            return REAPER_VERSION_CHECK_FAILED;
        }
        if (!isValidVersion(piece.version)) {
            if (DEBUG_DOWNLOAD) {
                ReaperLog.e(TAG, "query a bad version. : " + piece.version);
            }
            return REAPER_VERSION_CHECK_FAILED;
        }

        int compared = compareVersion(version, piece.version);
        ReaperLog.e(TAG, "compared : " + compared);
        if (compared != COMPARE_SERVER_HIGHER) {
            if (compared == COMPARE_FAILED) {
                return REAPER_VERSION_CHECK_FAILED;
            }
            return REAPER_VERSION_CHECK_SAME_VERSION;
        } else {
            boolean success = downloadHigherVersionReaper(piece);
            if (success) {
                SharedPreferences sp = ReaperEnv.sContext
                        .getSharedPreferences(SP_REAPER_NETWORK, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_TIME, piece.time).apply();
            }
            return success ? REAPER_VERSION_CHECK_NEW_VERSION : REAPER_VERSION_CHECK_FAILED;
        }
    }

    /**
     * @param piece
     * @return true if download success
     */
    private static boolean
        downloadHigherVersionReaper(@NonNull VersionPiece piece) {
        if (DEBUG_DOWNLOAD) {
            ReaperLog.e(TAG, "start download ... " + piece.url);
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(piece.url)
                .build();

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Response response = client.newCall(request).execute();
            if (response == null) {
                ReaperLog.e(TAG, "response == null");
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                ReaperLog.e(TAG, "body == null");
                return false;
            }
            is = body.byteStream();
            if (is == null) {
                ReaperLog.e(TAG, "is == null");
                return false;
            }
            fos = new FileOutputStream(new File("/mnt/sdcard/aa.apk"));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            ReaperLog.e(TAG, "download success.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "download error : " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private static VersionPiece queryVersion() {
        try {
            AppConf ac = new AppConf("{'baseUrl':'https://api.os.qiku.com','resourceUrl':'api/list'}");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("app", "Reaper"); // 设置app
            params.put("version", "1.0.0"); // 设置version
            params.put("api", "version"); // 设置api
            //params.put("time", "1494922168");
            SharedPreferences sp = ReaperEnv.sContext.getSharedPreferences("aa", Context.MODE_PRIVATE);
            String versionTime = sp.getString(KEY_TIME, "0");
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
            if (!TextUtils.equals(ret, "true")) {
                if (DEBUG_DOWNLOAD) {
                    ReaperLog.e(TAG, "dont have new version !");
                }
                return null;
            }

            com.alibaba.fastjson.JSONObject obj = jsonObject.getJSONObject("list");
            if (obj == null) {
                if (DEBUG_DOWNLOAD) {
                    ReaperLog.e(TAG, "dont have list data.");
                }
                return null;
            }
            JSONArray data = obj.getJSONArray("data");
            if (data == null) {
                if (DEBUG_DOWNLOAD) {
                    ReaperLog.e(TAG, "dont have data .");
                }
                return null;
            }
            String time = obj.getString("time");

            List<VersionPiece> pieces = new ArrayList<>();
            for (int i = 0; i < data.size(); ++i) {
                Object o = data.get(i);
                if (!(o instanceof JSONArray)) {
                    continue;
                }
                JSONArray dataArray = ((JSONArray)o);
                int id = (int) dataArray.get(0);
                String version = (String) dataArray.get(1);
                String url = (String) dataArray.get(2);
                String desc = (String) dataArray.get(3);

                VersionPiece piece = new VersionPiece();
                piece.id = id;
                piece.version = version;
                piece.url = url;
                piece.description = desc;
                pieces.add(piece);

                ReaperLog.e(TAG, "version : " + version + "; url : " + url);
            }

            VersionPiece piece = sortVersions(pieces);
            piece.time = time;
            ReaperLog.e(TAG, "sort version : " + piece);
            return piece;
        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "err : " + e.getMessage());
        }
        return null;
    }

    private static VersionPiece sortVersions(List<VersionPiece> pieces) {
        if (pieces == null || pieces.size() <= 0)
            return null;
        Collections.sort(pieces, new Comparator<VersionPiece>() {
            @Override
            public int compare(VersionPiece left, VersionPiece right) {
                return compareVersion(left.version, right.version);
            }
        });

        if (DEBUG_DOWNLOAD)
        for (VersionPiece piece : pieces) {
            ReaperLog.e(TAG, "piece : " + piece);
        }

        return pieces.get(0);
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

    static class VersionPiece {
        public int id;
        public String version;
        public String url;
        public String description;
        public String time;

        @Override
        public String toString() {
            return "VersionPiece{" +
                    "version='" + version + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}

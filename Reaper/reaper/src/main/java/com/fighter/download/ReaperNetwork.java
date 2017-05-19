package com.fighter.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;
import com.qiku.serversdk.custom.AppConf;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huayang on 17-5-10.
 */

@NoProguard
public class ReaperNetwork {
    private static final boolean DEBUG_DOWNLOAD = true;
    private static final boolean TEST = false;
    private static final String TAG = ReaperNetwork.class.getSimpleName();

    private static final int REAPER_VERSION_CHECK_NEW_VERSION = 1;
    private static final int REAPER_VERSION_CHECK_NO_NEW_VERSION = 0;
    private static final int REAPER_VERSION_CHECK_FAILED = -1;

    private static final int COMPARE_SERVER_HIGHER = 1;
    private static final int COMPARE_CURRENT_HIGHER = -1;
    private static final int COMPARE_EQUALS = 0;
    private static final int COMPARE_FAILED = -10;

    private static final int CHECK_HAS_NEW_VERSION = 10;
    private static final int CHECK_NO_NEW_VERSION = -10;

    private static final String RR_SUFFIX = ".rr";


    /**
     * Check higher reaperVersion
     * @return
     * 1 = check success and has higher reaperVersion
     * 0 = check success and dont have higher reaperVersion
     * -1 = check failed
     */
    @NoProguard
    public static int doQuery(String sdkVersion, String reaperVersion) {
        if (TextUtils.isEmpty(reaperVersion) || !isValidVersion(reaperVersion))
            return REAPER_VERSION_CHECK_FAILED;

        VersionRelationShip ship = queryHighestCompatibleVersion(sdkVersion);
        if (ship == null) {
            return REAPER_VERSION_CHECK_FAILED;
        }
        VersionPiece piece = queryVersion(ship);
        if (piece == null) {
            return REAPER_VERSION_CHECK_FAILED;
        }
        if (piece.ok == CHECK_NO_NEW_VERSION) {
            return REAPER_VERSION_CHECK_NO_NEW_VERSION;
        }
        if (TextUtils.isEmpty(piece.version) || TextUtils.isEmpty(piece.url)) {
            return REAPER_VERSION_CHECK_FAILED;
        }

        if (!isValidVersion(piece.version) || piece.ok != CHECK_HAS_NEW_VERSION) {
            if (DEBUG_DOWNLOAD) {
                ReaperLog.e(TAG, "query a bad reaperVersion. : " + piece.version);
            }
            return REAPER_VERSION_CHECK_FAILED;
        }

        int compared = compareVersion(reaperVersion, piece.version);
        ReaperLog.e(TAG, "compared : " + compared);
        if (compared != COMPARE_SERVER_HIGHER) {
            if (compared == COMPARE_FAILED) {
                return REAPER_VERSION_CHECK_FAILED;
            }
            return REAPER_VERSION_CHECK_NO_NEW_VERSION;
        } else {
            int netType = NetworkUtil.getNetWorkType(ReaperEnv.sContext);
            boolean download = false;
            if (netType == NetworkUtil.NETWORN_NONE) {
                ReaperLog.e(TAG, "current dont have network to download rr !");
                return REAPER_VERSION_CHECK_FAILED;
            }
            if (netType == NetworkUtil.NETWORN_WIFI) {
                if ((TextUtils.equals(piece.updateType, ReaperNWConstants.DOWNLOAD_BOTH)
                        || TextUtils.equals(piece.updateType, ReaperNWConstants.DOWNLOAD_WIFI)
                        || TextUtils.equals(piece.updateType, ReaperNWConstants.DOWNLOAD_MOBILE))) {
                    download = true;
                }
            } else {
                if (TextUtils.equals(piece.updateType, ReaperNWConstants.DOWNLOAD_MOBILE)) {
                    download = true;
                }
            }

            if (!download) {
                ReaperLog.e(TAG, "Network problems ! updateType : " + piece.updateType + "; network : " + netType);
                return REAPER_VERSION_CHECK_FAILED;
            }

            boolean success = downloadHigherVersionReaper(piece);
            if (success) {
                SharedPreferences sp = ReaperEnv.sContext
                        .getSharedPreferences(ReaperNWConstants.SP_REAPER_NETWORK, Context.MODE_PRIVATE);
                sp.edit().putString(ReaperNWConstants.KEY_TIME, piece.time).apply();
            }
            return success ? REAPER_VERSION_CHECK_NEW_VERSION : REAPER_VERSION_CHECK_FAILED;
        }
    }

    private static VersionRelationShip
        queryHighestCompatibleVersion(String currentSdkVersion) {
        try {
            AppConf ac = new AppConf(ReaperNWConstants.SERVER_SDK_CONF);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("app", ReaperNWConstants.SERVER_SDK_APP); // 设置app
            params.put("version", ReaperNWConstants.SERVER_SDK_VERSION); // 设置version
            params.put("api", ReaperNWConstants.SERVER_COMPATIBLE_API); // 设置api
            params.put("sdk_version", currentSdkVersion);
            JSONObject result = ac.getAppConfSyncCustom(params);
            ReaperLog.e(TAG, "result : " + result);

            if (result == null) {
                if (DEBUG_DOWNLOAD)
                    ReaperLog.e(TAG, "getAppConfSyncCustom == null");
                return null;
            }
            String ret = result.getString("result");
            ReaperLog.e(TAG, "request server : " + ret);
            if (!TextUtils.equals(ret, "true")) {
                return null;
            }

            JSONObject obj = result.getJSONObject("list");
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

            List<VersionRelationShip> relationShips = new ArrayList<>();
            for (int i = 0; i < data.length(); ++i) {
                Object o = data.get(i);
                if (!(o instanceof JSONArray)) {
                    continue;
                }
                JSONArray array = (JSONArray) o;
                VersionRelationShip ship = new VersionRelationShip();
                ship.sdkVersion = array.getString(1);
                ship.reaperVersion = array.getString(2);
                ship.state = array.getString(3);
                relationShips.add(ship);
            }

            VersionRelationShip highestRelationship =
                    getHighestVersionRelationship(relationShips);
            ReaperLog.e(TAG, "highest : " + highestRelationship);

            return highestRelationship;
        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "queryHighestCompatibleVersion failed ! " + e.getMessage());
        }

        return null;
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

            File dir = new File(ReaperNWConstants.DOWNLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String name = ReaperNWConstants.DOWNLOAD_DIR + File.separator + piece.version + RR_SUFFIX;
            ReaperLog.e(TAG, "name : " + name);
            fos = new FileOutputStream(new File(name));
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

    private static VersionPiece queryVersion(VersionRelationShip ship) {
        try {
            AppConf ac = new AppConf(ReaperNWConstants.SERVER_SDK_CONF);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("app", ReaperNWConstants.SERVER_SDK_APP); // 设置app
            params.put("version", ReaperNWConstants.SERVER_SDK_VERSION); // 设置version
            params.put("api", ReaperNWConstants.SERVER_SDK_API); // 设置api
            params.put("reaper_version", ship.reaperVersion);
            SharedPreferences sp = ReaperEnv.sContext
                    .getSharedPreferences(ReaperNWConstants.SP_REAPER_NETWORK, Context.MODE_PRIVATE);
            String versionTime = sp.getString(ReaperNWConstants.KEY_TIME, "0");
            //test start
            if (TEST) {
                versionTime = "0";
            }
            //test end
            params.put("time", versionTime);
            ReaperLog.e(TAG, "versionTime : " + versionTime);
            JSONObject result = ac.getAppConfSyncCustom(params);
            if (result == null) {
                if (DEBUG_DOWNLOAD)
                    ReaperLog.e(TAG, "getAppConfSyncCustom == null");
                return null;
            }
            String ret = result.getString("result");
            ReaperLog.e(TAG, "request server : " + ret);
            if (!TextUtils.equals(ret, "true")) {
                if (DEBUG_DOWNLOAD) {
                    ReaperLog.e(TAG, "dont have new version !");
                }
                VersionPiece piece = new VersionPiece();
                piece.ok = CHECK_NO_NEW_VERSION;
                return piece;
            }

            JSONObject obj = result.getJSONObject("list");
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
            for (int i = 0; i < data.length(); ++i) {
                Object o = data.get(i);
                if (!(o instanceof JSONArray)) {
                    continue;
                }
                JSONArray dataArray = ((JSONArray)o);
                ReaperLog.e(TAG, "dataArray : " + dataArray);

                String version =  dataArray.getString(0);
                String url =  dataArray.getString(1);
                String desc =  dataArray.getString(2);
                String updateType = dataArray.getString(3);

                VersionPiece piece = new VersionPiece();
                piece.version = version;
                piece.url = url;
                piece.description = desc;
                piece.updateType = updateType;
                pieces.add(piece);

                ReaperLog.e(TAG, "version : " + version + "; url : " + url);
            }

            VersionPiece piece = pieces.size() >= 1 ? pieces.get(0) : null;
            if (piece != null) {
                piece.time = time;
                piece.ok = CHECK_HAS_NEW_VERSION;
            }
            ReaperLog.e(TAG, "final version : " + piece);
            return piece;
        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "err : " + e.getMessage());
        }
        return null;
    }

    private static VersionRelationShip
        getHighestVersionRelationship(List<VersionRelationShip> relationShips) {
        if (relationShips == null || relationShips.size() <= 0)
            return null;
        for (VersionRelationShip ship : relationShips) {
            ReaperLog.e(TAG, "before ship : " + ship);
        }

        Iterator<VersionRelationShip> it = relationShips.iterator();
        while (it.hasNext()) {
            VersionRelationShip ship = it.next();
            if (TextUtils.equals("enable", ship.state)) {
                continue;
            }
            it.remove();
        }

        Collections.sort(relationShips, new Comparator<VersionRelationShip>() {
            @Override
            public int compare(VersionRelationShip left, VersionRelationShip right) {
                return compareVersion(left.reaperVersion, right.reaperVersion);
            }
        });

        for (VersionRelationShip ship : relationShips) {
            ReaperLog.e(TAG, "ship : " + ship);
        }

        return relationShips.get(0);
    }

    private static VersionPiece getHighestVersion(List<VersionPiece> pieces) {
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

    @NoProguard
    static class VersionPiece {
        public int id;
        public String version;
        public String url;
        public String description;
        public String time;
        public String updateType;
        public int ok;

        @Override
        public String toString() {
            return "VersionPiece{" +
                    "version='" + version + '\'' +
                    ", url='" + url + '\'' +
                    ", updateType " + updateType + '\'' + 
                    '}';
        }
    }

    @NoProguard
    static class VersionRelationShip {
        public String sdkVersion;
        public String reaperVersion;
        public String state;

        @Override
        public String toString() {
            return "VersionRelationShip{" +
                    "sdkVersion='" + sdkVersion + '\'' +
                    ", reaperVersion='" + reaperVersion + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }
}

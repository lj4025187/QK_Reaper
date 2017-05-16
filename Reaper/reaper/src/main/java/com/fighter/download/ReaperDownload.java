package com.fighter.download;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by huayang on 17-5-10.
 */

public class ReaperDownload {
    private static final String TAG = ReaperDownload.class.getSimpleName();
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
//        if (ReaperNetwork.sHttpsUtil == null) {
//            if (DEBUG_DOWNLOAD)
//                ReaperLog.e(TAG, "queryVersion, HttpsUtil == null !");
//            return null;
//        }
//        try {
//            Response response = ReaperNetwork.sHttpsUtil.requestSync(URL_REAPER_HIGHER_VERSION);
//            if (response == null)
//                return null;
//            Headers responseHeaders = response.headers();
//            for (int i = 0; i < responseHeaders.size(); i++) {
//                ReaperLog.e(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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

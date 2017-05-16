package com.fighter.loader;

import android.content.Loader;
import android.text.TextUtils;

import com.fighter.utils.LoaderLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matti on 2017/5/12.
 */

public class ReaperVersionManager {
    private static final String TAG = ReaperVersionManager.class.getSimpleName();
    private static final boolean DEBUG_VERSION = true;

    private static String mVersion;
    private static boolean mCheckSuccess;
    private static ReaperVersionManager sInstance;
    private ReentrantLock mLock;
    private Class mReaperDownloadClass;

    public static ReaperVersionManager getInstance(String version) {
        synchronized (ReaperVersionManager.class) {
            if (sInstance == null) {
                sInstance = new ReaperVersionManager(version);
            }
            if (!TextUtils.equals(version, mVersion)) {
                //we get a new version,need re-init
                mCheckSuccess = false;
            }
        }
        return sInstance;
    }

    private ReaperVersionManager(String version) {
        mVersion = version;
        mLock = new ReentrantLock();
    }

    public void setReaperNetworkClass(Class claxx) {
        mReaperDownloadClass = claxx;
    }

    public void queryHigherReaper() {
        synchronized (ReaperVersionManager.class) {
            if (mCheckSuccess) {
                return;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoaderLog.e(TAG, "checkHigherVersion, before lock . Pid:"+ Thread.currentThread().getId());
                mLock.lock();
                LoaderLog.e(TAG, "checkHigherVersion, we have locked here," +
                        " and waiting for other Thread exec over. ");

                if (mCheckSuccess) {
                    return;
                }
                int checkResult = doQuery();
                //do check.
                if (checkResult == 1) {
                    mCheckSuccess = downloadHigherReaper();
                } else {
                    mCheckSuccess = checkResult == 0;
                }

                mLock.unlock();
                LoaderLog.e(TAG, "checkHigherVersion, unLock and check over. mCheckSuccess : " + mCheckSuccess);
            }
        }).start();
    }

    private boolean downloadHigherReaper() {
        if (mReaperDownloadClass == null) {
            if (DEBUG_VERSION)
                LoaderLog.e(TAG, "downloadReaper, mReaperDownloadClass == null .");
            return false;
        }

        try {
            Method downloadMethod =
                    mReaperDownloadClass.getDeclaredMethod("downloadHigherVersionReaper");
            if (downloadMethod == null) {
                if (DEBUG_VERSION)
                    LoaderLog.e(TAG, "cant find downloadMethod !");
                return false;
            }
            downloadMethod.setAccessible(true);
            Object o = downloadMethod.invoke(null);
            if (o == null || !(o instanceof Boolean)) {
                if (DEBUG_VERSION) {
                    LoaderLog.e(TAG, "downloadMethod invoke return an err result !");
                }
                return false;
            }
            return (boolean) o;
         } catch (Exception e) {
            e.printStackTrace();
            if (DEBUG_VERSION) {
                LoaderLog.e(TAG, "error : " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Check higher version
     * @return
     * 1 = check success and has higher version
     * 0 = check success and dont have higher version
     * -1 = check failed
     */
    private int doQuery() {
        if (mReaperDownloadClass == null) {
            LoaderLog.e(TAG, "doQuery, cant find ReaperDownload Class!");
            return -1;
        }

        try {
            Method doQueryMethod = mReaperDownloadClass.getDeclaredMethod("doQuery", String.class);
            if (doQueryMethod == null) {
                LoaderLog.e(TAG, "doQuery, doQueryMethod == null !");
                return -1;
            }
            doQueryMethod.setAccessible(true);
            Object retVal = doQueryMethod.invoke(null, mVersion);
            if (retVal == null || !(retVal instanceof Integer)) {
                LoaderLog.e(TAG, "doQuery, invoke method error !");
                return -1;
            }
            return (int) retVal;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

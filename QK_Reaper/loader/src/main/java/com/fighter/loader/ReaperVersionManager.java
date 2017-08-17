package com.fighter.loader;

import android.content.Context;
import android.os.SystemClock;

import com.fighter.utils.LoaderLog;
import com.fighter.utils.NetworkUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matti on 2017/5/12.
 */

public class ReaperVersionManager {

    private static final String TAG = "ReaperVersionManager";

    private static final int RETRY_TIME = 5;

    /**
     * Has a new version
     * Must be same with that defined in Reaper
     */
    private static final int REAPER_VERSION_CHECK_NEW_VERSION = 1;

    /**
     * Has no new version
     * Must be same with that defined in Reaper
     */
    private static final int REAPER_VERSION_CHECK_NO_NEW_VERSION = 0;

    /**
     * Check version fail
     * Must be same with that defined in Reaper
     */
    private static final int REAPER_VERSION_CHECK_FAILED = -1;

    private Context mContext;
    private String mVersion;
    private Class mReaperDownloadClass;

    private boolean mCheckComplete;
    private ReentrantLock mLock;
    private int mRetry;

    public ReaperVersionManager(Context context, String version, Class networkClass) {
        mContext = context;
        mVersion = version;
        mReaperDownloadClass = networkClass;
        mLock = new ReentrantLock();
    }

    /**
     * If query failed, we will try for retry times
     * @see #mRetry
     */
    public void queryHigherReaper() {
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            LoaderLog.i(TAG, "network unavailable, cant query higher reaper!!!");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                queryHigherReaperWithSubThread();
            }
        }).start();
    }

    private void queryHigherReaperWithSubThread() {
        try {
            mLock.lock();
            queryHigherReaperWithLock();
        } finally {
            mLock.unlock();
        }
    }

    private void queryHigherReaperWithLock() {
        while(!mCheckComplete && mRetry < RETRY_TIME) {
            int checkResult = doQuery();
            mCheckComplete = checkResult == REAPER_VERSION_CHECK_NEW_VERSION ||
                    checkResult == REAPER_VERSION_CHECK_NO_NEW_VERSION;

            LoaderLog.e(TAG, "queryHigherReaperWithSubThread. "
                    + "doQuery result : " + checkResult
                    + ", mCheckComplete : " + mCheckComplete
                    + ", retry time : " + mRetry);

            mRetry ++;

            SystemClock.sleep(1500);
        }
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
            return REAPER_VERSION_CHECK_FAILED;
        }

        try {
            Method doQueryMethod =
                    mReaperDownloadClass.getDeclaredMethod("doQuery", String.class, String.class);
            if (doQueryMethod == null) {
                LoaderLog.e(TAG, "doQuery, doQueryMethod == null !");
                return REAPER_VERSION_CHECK_FAILED;
            }
            doQueryMethod.setAccessible(true);
            Object retVal = doQueryMethod.invoke(null, Version.VERSION, mVersion);
            if (retVal == null || !(retVal instanceof Integer)) {
                LoaderLog.e(TAG, "doQuery, invoke method error !");
                return REAPER_VERSION_CHECK_FAILED;
            }
            return (int) retVal;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return REAPER_VERSION_CHECK_FAILED;
    }
}

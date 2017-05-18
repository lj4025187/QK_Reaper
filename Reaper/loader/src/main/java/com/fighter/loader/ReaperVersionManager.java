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

    private static final int RETRY_TIME = 4;
    private static final int REAPER_VERSION_CHECK_NEW_VERSION = 1;
    private static final int REAPER_VERSION_CHECK_SAME_VERSION = 0;
    private static final int REAPER_VERSION_CHECK_FAILED = -1;

    private static String mVersion;
    private static boolean mCheckSuccess;
    private static ReaperVersionManager sInstance;
    private ReentrantLock mLock;
    private Class mReaperDownloadClass;
    private int mRetry;

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

    /**
     * If query failed, we will try for retry times
     * @see #mRetry
     */
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
                if (checkResult == REAPER_VERSION_CHECK_NEW_VERSION) {
                    mCheckSuccess = true;
                } else {
                    mCheckSuccess = checkResult == REAPER_VERSION_CHECK_SAME_VERSION;
                }

                mLock.unlock();

                if (!mCheckSuccess && mRetry++ < RETRY_TIME) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    queryHigherReaper();
                }
                LoaderLog.e(TAG, "checkHigherVersion, unLock and check over. mCheckSuccess : "
                        + mCheckSuccess + "; retry time : " + mRetry);
            }
        }).start();
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

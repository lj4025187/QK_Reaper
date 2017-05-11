package com.fighter.loader;

import com.fighter.utils.LoaderLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApi {

    private static final java.lang.String TAG = ReaperApi.class.getSimpleName();

    //Instance of ReaperApi in reaper.rr or Reaper.apk
    private Object mInstance;
    //Current reaper version
    private String mVersion;
    private boolean mCheckSuccess;
    private boolean mChecking;
    private ReentrantLock mLock;
    private Condition mDownloadCondition;

    public ReaperApi(Object instance, String version) {
        mInstance = instance;
        LoaderLog.e(TAG, "mInstance : " + instance);
        mVersion = version;
        mLock = new ReentrantLock();
        mDownloadCondition = mLock.newCondition();
    }

    private void checkHigherVersion() {
        if (mCheckSuccess) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoaderLog.e(TAG, "checkHigherVersion, before lock . mChecking : " + mChecking);
                mLock.lock();
                LoaderLog.e(TAG, "checkHigherVersion, we have locked here, and waiting for other Thread exec over.");
                if (mChecking) {
                    try {
                        mDownloadCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LoaderLog.e(TAG, "checkHigherVersion, if we are signal by condition, mCheckSuccess : " + mCheckSuccess);
                }

                mChecking = true;

                if (mCheckSuccess) {
                    mChecking = false;
                    return;
                }
                int checkResult = doCheck();
                //do check.
                if (checkResult == 1) {
                    if (downloadHigherVersion()) {
                        mCheckSuccess = true;
                    } else {
                        mCheckSuccess = false;
                    }
                } else if (checkResult == 0) {
                    mCheckSuccess = true;
                } else {
                    mCheckSuccess = false;
                }
                mChecking = false;
                mDownloadCondition.signal();
                mLock.unlock();
                LoaderLog.e(TAG, "checkHigherVersion, unLock and check over.");
            }
        }).start();
    }

    private boolean downloadHigherVersion() {
        return false;
    }

    /**
     * Check higher version
     * @return
     * 1 = check success and has higher version
     * 0 = check success and dont have higher version
     * -1 = check failed
     */
    private int doCheck() {
        try {
            Thread.sleep(6000);
        } catch (Exception e){
        }
        return -1;
    }

    public Object requestSplashAds(String name, int time) {
        checkHigherVersion();
        try {
            Method requestSplashAdsMethod =
                mInstance.getClass().getDeclaredMethod("requestSplashAds", String.class, int.class);
            if (requestSplashAdsMethod == null)
                return null;
            requestSplashAdsMethod.setAccessible(true);
            return requestSplashAdsMethod.invoke(mInstance, name, time);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}

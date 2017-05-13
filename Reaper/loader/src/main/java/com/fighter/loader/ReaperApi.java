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
    private ReaperVersionManager mVersionManager;

    public ReaperApi(Object instance, String version) {
        LoaderLog.e(TAG, "mInstance : " + instance);
        mInstance = instance;

    }

    public Object requestSplashAds(String name, int time) {
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

package com.fighter.loader;

import com.fighter.utils.Slog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApi {

    private static final java.lang.String TAG = ReaperApi.class.getSimpleName();

    private Object mInstance;

    public ReaperApi(Object instance) {
        mInstance = instance;
        Slog.e(TAG, "mInstance : " + instance);
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

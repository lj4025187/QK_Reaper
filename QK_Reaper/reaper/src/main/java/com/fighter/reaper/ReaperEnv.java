package com.fighter.reaper;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huayang on 17-5-13.
 */

@NoProguard
public class ReaperEnv {

    private static final String TAG = "ReaperEnv";

    @NoProguard
    private static String sSdkPath;

    @NoProguard
    public static AssetManager sAssetManager;

    @NoProguard
    public static Resources sResources;

    @NoProguard
    public static ClassLoader sClassLoader;

    @NoProguard
    public static Context sContext;

    @NoProguard
    public static ContextProxy sContextProxy;

    @NoProguard
    public static void init(Context context, String sdkPath, ClassLoader loader) {
        sContext = context;
        sSdkPath = sdkPath;
        sClassLoader = loader;

        if (TextUtils.isEmpty(sSdkPath)) {
            ReaperLog.e(TAG, "sdkPath == null !");
            return;
        }
        ReaperLog.e(TAG, "sdkPath : " + sSdkPath);
        AssetManager assetManager= null;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            if (addPathMethod == null)
                return;
            addPathMethod.setAccessible(true);
            addPathMethod.invoke(assetManager, sSdkPath);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (assetManager == null) {
            ReaperLog.e(TAG, "assetManager : " + assetManager);
            return;
        }
        sAssetManager = assetManager;

        sContextProxy = new ContextProxy(sContext);

        sResources = new Resources(sAssetManager, sContext.getResources().getDisplayMetrics(),
                sContext.getResources().getConfiguration());
    }

}

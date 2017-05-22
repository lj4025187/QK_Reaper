package com.fighter.download;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.fighter.ContextProxy;
import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huayang on 17-5-13.
 */

@NoProguard
public class ReaperEnv {
    private static final String TAG = ReaperEnv.class.getSimpleName();

    @NoProguard
    private static String sSdkPath;

    @NoProguard
    public static AssetManager mAssetManager;
    public static HttpsManager sHttpsManager;

    @NoProguard
    public static ClassLoader mClassLoader;

    @NoProguard
    private static Context sContext;

    @NoProguard
    public static ContextProxy sContextProxy;

    @NoProguard
    public static void initReaperEnv() {
        if (TextUtils.isEmpty(sSdkPath)) {
            ReaperLog.e(TAG, "sSdkPath == null !");
            return;
        }
        ReaperLog.e(TAG, "abspath : " + sSdkPath);
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
        mAssetManager = assetManager;

        sContextProxy = new ContextProxy(sContext);
//        if (sHttpsManager == null) {
//            sHttpsManager = new HttpsManager("");
//        }
    }

}

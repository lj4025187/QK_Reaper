package com.fighter.download;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huayang on 17-5-13.
 */

@Deprecated
@NoProguard
public class ReaperNetwork {
    private static final String TAG = ReaperNetwork.class.getSimpleName();

    private static String SDK_ABSPATH;
    public static AssetManager sAssetManager;
    public static HttpsManager sHttpsManager;

    @NoProguard
    public static void initForNetwork() {
        if (TextUtils.isEmpty(SDK_ABSPATH)) {
            ReaperLog.e(TAG, "SDK_ABSPATH == null !");
            return;
        }
        ReaperLog.e(TAG, "abspath : " + SDK_ABSPATH);
        AssetManager assetManager= null;
        try {
            assetManager = AssetManager.class.newInstance();
            Method addPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            if (addPathMethod == null)
                return;
            addPathMethod.setAccessible(true);
            addPathMethod.invoke(assetManager, SDK_ABSPATH);
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

        if (sHttpsManager == null) {
            sHttpsManager = new HttpsManager("");
        }
    }

}

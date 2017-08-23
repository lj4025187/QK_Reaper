package com.fighter.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.util.List;

/**
 * Hook AKAD activity and AKAD FileProvider
 *
 * Created by zhangjg on 17-8-22.
 */

public final class AKADHook {

    private static final String TAG = "AKADHook";

    private static final String DATA_TYPE_ARCHIVE = "application/vnd.android.package-archive";

    public static boolean changeToProxyWhenStartAKActivity(Intent intent) {
        if (intent == null) {
            return false;
        }

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return false;
        }

        String className = componentName.getClassName();
        if (!ComponentProxyMap.AKAD_ACTIVITY.equals(className)) {
            return false;
        }

        String proxyActivityName =
                ComponentProxyMap.getProxyActivityByReal(ComponentProxyMap.AKAD_ACTIVITY);
        if (TextUtils.isEmpty(proxyActivityName)) {
            ReaperLog.e(ComponentProxyMap.AKAD_ACTIVITY + " has no proxy activity");
            return false;
        }

        ComponentName proxyComponent =
                new ComponentName(intent.getComponent().getPackageName(), proxyActivityName);
        intent.setComponent(proxyComponent);

        ReaperLog.i(TAG, "start proxy activity");
        return true;
    }

    /**
     * Change uri when call PackageInstallerActivity.
     * This is for hook the FileProvider for AKAD.
     *
     * The uri to be replaced like this : content:///xxx.apk
     */
    public static boolean changeUriWhenCallPackageInstaller(Context context, Intent intent) {
        if (intent == null || context == null) {
            return false;
        }

        if(!TextUtils.equals(intent.getType(), DATA_TYPE_ARCHIVE)) {
            return false;
        }

        if (Build.VERSION.SDK_INT < 24) {
            return false;
        }

        Uri origin = intent.getData();
        if (origin == null || TextUtils.isEmpty(origin.toString())) {
            return false;
        }

        if (!"content".equals(origin.getScheme())) {
            return false;
        }

        if(!TextUtils.isEmpty(origin.getAuthority())) {
            return false;
        }

        Uri proxyUri = generateUriForProxyProvider(context, origin);
        intent.setDataAndType(proxyUri, DATA_TYPE_ARCHIVE);
        return true;
    }

    private static Uri generateUriForProxyProvider(Context context, Uri origin) {

        List<String> pathSegments = origin.getPathSegments();
        StringBuilder path = new StringBuilder("content://" + context.getPackageName()
                + ".reaper.provider.ReaperProxyProvider");

        if(pathSegments != null && !pathSegments.isEmpty()) {
            for(String value : pathSegments) {
                path.append("/").append(value);
            }
        }
        return Uri.parse(path.toString());
    }

    private AKADHook() {}
}

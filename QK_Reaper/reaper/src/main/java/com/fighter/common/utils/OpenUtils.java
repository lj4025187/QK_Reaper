package com.fighter.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by guanshuaichao on 2017/8/10.
 */

public class OpenUtils {
    public static boolean open(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        boolean handled = false;

        if (lowerUrl.startsWith("tel:") &&
                lowerUrl.length() > "tel:".length() &&
                callPhone(context, url.substring(4))) {
            handled = true;
        } else if (lowerUrl.startsWith("file:") ||
                lowerUrl.startsWith("https:") ||
                lowerUrl.startsWith("http:")) {
            if (openWebUrl(context, url)) {
                handled = true;
            }
        } else {
            handled = openDeepLink(context, url);
        }

        return handled;
    }

    public static boolean callPhone(Context context, String arg3) {
        boolean bool;
        try {
            Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + arg3));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            bool = true;
        } catch (Exception exception) {
            bool = false;
        }

        return bool;
    }

    public static boolean openWebUrl(Context context, String url) {
        boolean startSuccess;
        if (isAppInstalled(context, "com.qihoo.browser") &&
                openWebUrlWithApp(context, url,
                        "com.qihoo.browser", null)) {
            startSuccess = true;
        } else if (isAppInstalled(context, "com.android.browser") &&
                openWebUrlWithApp(context, url,
                        "com.android.browser", "com.android.browser.BrowserActivity")) {
            startSuccess = true;
        } else {
            startSuccess = openWebUrlNormal(context, url);
        }

        return startSuccess;
    }


    public static boolean openWebUrlWithApp(Context context, String url, String pkgName,
                                            String className) {
        boolean startSuccess = false;
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(pkgName)) {
                intent.setPackage(pkgName);
                if (!TextUtils.isEmpty(className)) {
                    intent.setClassName(pkgName, className);
                }
            }

            context.startActivity(intent);
            startSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startSuccess;
    }

    public static boolean openWebUrlNormal(Context context, String url) {
        boolean startSuccess = false;
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.BROWSABLE");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
            startSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startSuccess;
    }

    private static boolean openDeepLink(Context context, String url) {
        boolean startSuccess = true;
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if (context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.GET_META_DATA).size() <= 0) {
                return false;
            }

            intent.addCategory("android.intent.category.BROWSABLE");
            intent.setComponent(null);
            intent.putExtra("com.android.browser.application_id", context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable throwable) {
            startSuccess = false;
        }

        return startSuccess;
    }

    private static boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return packageInfo != null;
    }
}

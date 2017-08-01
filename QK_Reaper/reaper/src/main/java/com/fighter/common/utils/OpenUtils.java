package com.fighter.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 打开其它APP工具类
 */

public class OpenUtils {
    public static boolean openWebUrl(Context context, String url) {
        boolean startSuccess;
        if (openWebUrlWithApp(context, url,
                "com.qihoo.browser", "com.qihoo.browser.activity.SplashActivity")) {
            startSuccess = true;
        } else if (openWebUrlWithApp(context, url,
                "com.android.browser", "com.android.browser.BrowserActivity")) {
            startSuccess = true;
        } else {
            startSuccess = openWebUrlNormal(context, url);
        }

        return startSuccess;
    }

    public static boolean openWebUrlWithApp(Context context, String url,
                                            String pkgName, String className) {
        boolean startSuccess = false;
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);

            context.startActivity(intent);
            startSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startSuccess;
    }
}

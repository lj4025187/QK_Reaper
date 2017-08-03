package com.ak.android.appIntegration;

import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.io.File;

/**
 * 安装接管类
 * <p>
 * 将此类放入你们的代码中，并不要混淆此类的任何属性，包括包名;
 * <p>
 * Any question,contact: liangyanmiao@360.cn+
 */

final public class HookInstaller {

    private final static String TAG = "AKAdSDKWrapper.HookInstaller";

    private static AKAdSilentInstallCallBack sSilentListener;
    /* 是否接管SDK的安装，为true时接管 */
    private static boolean hookInstall = false;


    public static boolean isHookInstall() {
        return hookInstall;
    }

    public final static void setHookInstall(boolean hookInstall) {
        ReaperLog.i(TAG, "setHookInstall " + hookInstall);
        HookInstaller.hookInstall = hookInstall;
    }

    public static void setSilentListener(AKAdSilentInstallCallBack silentListener) {
        HookInstaller.sSilentListener = silentListener;
    }

    /* 安装细节，请自己实现 */
    public final static void install(String... args) {  //第一个参数为key，第二个参数为path
        /*
         *  path对应的key
         *  长度: 33个字符
         *  例: 684dd91b22e4358a169a7cbaaa3c06f11
         *  */
        String key = null;

        /*
         *  apk的路径
         *  例：/storage/emulated/0/Android/data/com.qihoo360.mobilesafe/files/sllak/apk/com.yiche.autoeasy_55.apk
         *  */
        String path = null;

        if (args.length > 1) {
            key = args[0];
            path = args[1];
        }

        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(path) || !path.endsWith(".apk")) {
            ReaperLog.e(TAG, "install method params contains empty key "
                    + TextUtils.isEmpty(key)
                    + " path "
                    + (!TextUtils.isEmpty(path) ? path : " is null"));
        } else {
            File apkFile = new File(path);
            if (!apkFile.exists()) {
                ReaperLog.e(TAG, "install apk file not exits path " + path);
                return;
            }
            if (sSilentListener == null) {
                ReaperLog.e(TAG, "install get silent listener is null return");
                return;
            }
            sSilentListener.installInternal(key, path);
        }
    }

    public interface AKAdSilentInstallCallBack {
        void installInternal(String key, String apkPath);
    }
}
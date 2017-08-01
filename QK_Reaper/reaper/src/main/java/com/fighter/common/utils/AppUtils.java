package com.fighter.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     desc  : App相关工具类
 * </pre>
 */
public final class AppUtils {

    private AppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 判断App是否安装
     *
     * @param packageName 包名
     * @return {@code true}: 已安装<br>{@code false}: 未安装
     */
    public static boolean isInstallApp(Context context, String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
            List<String> pName = new ArrayList<>();
            if (installedPackages != null) {
                for (int i = 0; i < installedPackages.size(); i++) {
                    String pn = installedPackages.get(i).packageName;
                    pName.add(pn);
                }
            }
            return pName.contains(packageName);
        }
        return false;
    }

    /**
     * 安装App(支持7.0)
     *
     * @param filePath 文件路径
     */
    public static void installApp(Context context, String filePath) {
        installApp(context, getFileByPath(filePath));
    }

    /**
     * 安装App
     *
     * @param file 文件
     */
    public static void installApp(Context context, File file) {
        if (!isFileExists(file)) return;
        context.startActivity(getInstallAppIntent(file));
    }

    /**
     * 安装App
     *
     * @param activity    activity
     * @param filePath    文件路径
     * @param requestCode 请求值
     */
    public static void installApp(Activity activity, String filePath, int requestCode) {
        installApp(activity, getFileByPath(filePath), requestCode);
    }

    /**
     * 安装App(支持6.0)
     *
     * @param activity    activity
     * @param file        文件
     * @param requestCode 请求值
     */
    public static void installApp(Activity activity, File file, int requestCode) {
        if (!isFileExists(file)) return;
        activity.startActivityForResult(getInstallAppIntent(file), requestCode);
    }

    /**
     * 获取未安装apk的包名
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static String getArchivePackageName(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.packageName;
        }
        return null;
    }

    /**
     * 获取安装App的意图
     *
     * @param filePath 文件路径
     * @return intent
     */
    private static Intent getInstallAppIntent(String filePath) {
        return getInstallAppIntent(getFileByPath(filePath));
    }

    /**
     * 获取安装App的意图
     *
     * @param file 文件
     * @return intent
     */
    private static Intent getInstallAppIntent(File file) {
        if (file == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        String type = "application/vnd.android.package-archive";
        data = Uri.fromFile(file);
        intent.setDataAndType(data, type);
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private static File getFileByPath(String filePath) {
        return TextUtils.isEmpty(filePath) ? null : new File(filePath);
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    private static boolean isFileExists(String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在
     *
     * @param file 文件
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    private static boolean isFileExists(File file) {
        return file != null && file.exists();
    }
}

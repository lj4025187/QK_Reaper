package com.fighter.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Utils to get local message of current device
 * <p>
 * Created by zhangjg on 17-5-8.
 */

public final class Device {
    // ----------------------------------------------------
    // App 信息
    // ----------------------------------------------------

    public static ApplicationInfo getApplicationInfo(Context context, int flags) {
        ApplicationInfo applicationInfo = null;
        PackageManager pm = context.getPackageManager();
        try {
            applicationInfo = pm.getApplicationInfo(context.getPackageName(), flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo;
    }

    // ----------------------------------------------------
    // 系统信息
    // ----------------------------------------------------

    /**
     * 获取{@code Build.MODEL}
     *
     * @return {@code Build.MODEL}
     */
    public static String getBuildModel() {
        return Build.MODEL;
    }

    /**
     * 获取{@code Build.BRAND}
     *
     * @return {@code Build.BRAND}
     */
    public static String getBuildBrand() {
        return Build.BRAND;
    }

    /**
     * 获取{@code Build.RELEASE}
     *
     * @return {@code Build.RELEASE}
     */
    public static String getBuildRelease() {
        return Build.VERSION.RELEASE;
    }

    // ----------------------------------------------------
    // 硬件信息
    // ----------------------------------------------------

    /**
     * 获取设备AndroidID
     *
     * @return AndroidID
     */
    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取设备MAC地址
     * <p>
     * 需添加权限:<br></br>
     * {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}<br></br>
     * {@code <uses-permission android:name="android.permission.INTERNET"/>}
     * </p>
     *
     * @return MAC地址
     */
    public static String getMac(Context context) {
        String macAddress = getMacAddressByWifiInfo(context);
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByNetworkInterface();
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByFile();
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        return null;
    }

    /**
     * 获取设备MAC地址
     * <p>
     * 需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}
     * </p>
     *
     * @return MAC地址
     */
    private static String getMacAddressByWifiInfo(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (info != null) return info.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备MAC地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @return MAC地址
     */
    private static String getMacAddressByNetworkInterface() {
        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nis) {
                if (!ni.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02x:", b));
                    }
                    return res1.deleteCharAt(res1.length() - 1).toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备MAC地址
     *
     * @return MAC地址
     */
    private static String getMacAddressByFile() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            if (name != null) {
                result = ShellUtils.execCmd("cat /sys/class/net/" + name + "/address", false);
                if (result.result == 0) {
                    if (result.successMsg != null) {
                        return result.successMsg;
                    }
                }
            }
        }
        return "02:00:00:00:00:00";
    }

    // ----------------------------------------------------
    // 屏幕信息
    // ----------------------------------------------------

    /**
     * 获取屏幕的宽度（单位：px）
     *
     * @return 屏幕宽px
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(dm);// 给白纸设置宽高
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度（单位：px）
     *
     * @return 屏幕高px
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(dm);// 给白纸设置宽高
        return dm.heightPixels;
    }

    /**
     * 获取屏幕密度
     *
     * @return 屏幕密度，0.75/1.0/1.5/2.0等。
     */
    public static float getScreenDensity() {
        DisplayMetrics dm = new DisplayMetrics();
        return dm.density;
    }

    /**
     * 获取屏幕旋转角度
     *
     * @param activity activity
     * @return 屏幕旋转角度
     */
    public static int getScreenRotation(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            default:
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }

    /**
     * 判断是否横屏
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 判断是否竖屏
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;
    }

    // ----------------------------------------------------
    // 位置信息
    // ----------------------------------------------------

    // ----------------------------------------------------
    // 网络信息
    // ----------------------------------------------------

    public enum NetworkType {
        NETWORK_WIFI,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO
    }

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * 获取当前网络类型
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return 网络类型
     * <ul>
     * <li>{@link Device.NetworkType#NETWORK_WIFI   } </li>
     * <li>{@link Device.NetworkType#NETWORK_4G     } </li>
     * <li>{@link Device.NetworkType#NETWORK_3G     } </li>
     * <li>{@link Device.NetworkType#NETWORK_2G     } </li>
     * <li>{@link Device.NetworkType#NETWORK_UNKNOWN} </li>
     * <li>{@link Device.NetworkType#NETWORK_NO     } </li>
     * </ul>
     */
    public static NetworkType getNetworkType(Context context) {
        NetworkType netType = NetworkType.NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NetworkType.NETWORK_2G;
                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NetworkType.NETWORK_3G;
                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NetworkType.NETWORK_4G;
                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetworkType.NETWORK_3G;
                        } else {
                            netType = NetworkType.NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NetworkType.NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
    }

    /**
     * 获取公网IP地址，
     * TODO 这个需要服务器提供能力
     *
     * @return 公网IP地址
     */
    public static String getRemoteIp() {
        return "127.0.0.1";
    }

    // ----------------------------------------------------
    // 运营商信息
    // ----------------------------------------------------

    public enum SimOperator {
        /**
         * 未知
         */
        SIM_OPERATOR_UNKNOWN,
        /**
         * 移动
         */
        SIM_OPERATOR_CHINA_MOBILE,
        /**
         * 联通
         */
        SIM_OPERATOR_CHINA_UNICOM,
        /**
         * 电信
         */
        SIM_OPERATOR_CHINA_TELCOM
    }

    /**
     * 获取系统IMEI1
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMEI1值
     */
    public static String getM1(Context context) {
        String m1 = null;
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                m1 = tm.getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m1;
    }

    // TODO 获取M2

    /**
     * 获取系统IMEI2
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMEI2值
     */
    public static String getM2(Context context) {
        return null;
    }

    /**
     * 获取Sim卡运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 移动网络运营商名称
     */
    public static SimOperator getSimOperatorByMnc(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm != null ? tm.getSimOperator() : null;
        if (operator == null) return SimOperator.SIM_OPERATOR_UNKNOWN;
        switch (operator) {
            case "46000":
            case "46002":
            case "46007":
                return SimOperator.SIM_OPERATOR_CHINA_MOBILE;
            case "46001":
                return SimOperator.SIM_OPERATOR_CHINA_UNICOM;
            case "46003":
                return SimOperator.SIM_OPERATOR_CHINA_TELCOM;
            default:
                return SimOperator.SIM_OPERATOR_UNKNOWN;
        }
    }

    // ----------------------------------------------------
    // 其它信息
    // ----------------------------------------------------

}

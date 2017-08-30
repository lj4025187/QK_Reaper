package com.fighter.common;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EncryptUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utils to get local message of current device
 * <p>
 * Created by zhangjg on 17-5-8.
 */

public final class Device {

    private static final String TAG = "Device";
    // ----------------------------------------------------
    // App 信息
    // ----------------------------------------------------
    private static MacThread sMacThread;

    public static String getApplicationName(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo =
                getApplicationInfo(context, pm, PackageManager.GET_UNINSTALLED_PACKAGES);
        if (applicationInfo == null) {
            return "";
        } else {
            return (String) pm.getApplicationLabel(applicationInfo);
        }
    }

    public static ApplicationInfo getApplicationInfo(Context context, PackageManager pm, int flags) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo(context.getPackageName(), flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo;
    }

    public static PackageInfo getPackageInfo(Context context, String pkgName, int flags) {
        PackageInfo pkgInfo = null;
        PackageManager pm = context.getPackageManager();
        try {
            pkgInfo = pm.getPackageInfo(pkgName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkgInfo;
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
     * 获取{@code Build.MANUFACTURER}
     *
     * @return {@code Build.MANUFACTURER}
     */
    public static String getBuildManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取{@code Build.PRODUCT}
     *
     * @return {@code Build.PRODUCT}
     */
    public static String getBuildProduct() {
        return Build.PRODUCT;
    }

    public static String getBuildSDKVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
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

    // be the same with WifiInfo.java
    private static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";

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
        if (!DEFAULT_MAC_ADDRESS.equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByNetworkInterface();
        if (!DEFAULT_MAC_ADDRESS.equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByFile();
        if (!DEFAULT_MAC_ADDRESS.equals(macAddress)) {
            return macAddress;
        }
        return null;
    }

    /**
     * 获取特定格式的mac地址字符串
     * 具体格式为：
     * 获取用户手机wifi网卡号设备的MAC，
     * 去除分隔符":"后转为大写,并取md5sum摘要值, 摘要小写
     * 例如：“900150983cd24fb0d6963f7d28e17f72”
     *
     * @param context
     * @return
     */
    public static String getFormatMac(Context context) {
        String mac = getMacStable(context);
        if (TextUtils.isEmpty(mac)) {
            return null;
        }

        String formatMac = mac.replaceAll(":", "").toUpperCase();
        return EncryptUtils.encryptMD5ToString(formatMac).toLowerCase();
    }

    /**
     * get wifi mac address stable need permission "android.permission.CHANGE_WIFI_STATE"
     *
     * @param context the context
     * @return wifi mac address
     */
    public synchronized static String getMacStable(Context context) {
        if (context == null)
            return null;

        String macAddress = getCacheMac(context);

        if (macAddress != null)
            return macAddress;

        macAddress = getMac(context);
        if (macAddress == null) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                boolean wifiOriginState = wifiManager.isWifiEnabled();

                wifiManager.setWifiEnabled(true);
                macAddress = getMac(context);
                wifiManager.setWifiEnabled(wifiOriginState);

                /* fix api init have no permission enable wifi,cause ui lock.
                * mac address maybe not have in never open wifi */
                if (macAddress == null) {
                    if (sMacThread == null)
                        sMacThread = new MacThread();
                    sMacThread.setContext(context.getApplicationContext());
                    sMacThread.setWifiManager(wifiManager);
                    if (!sMacThread.isAlive())
                        sMacThread.start();
                }
            }
        } else {
            storeWifiMac(context, macAddress);
        }
        return macAddress;
    }

    /**
     * store wifi mac address address in sdcard
     *
     * @param macAddress
     * @return
     */
    private static void storeWifiMac(Context context, String macAddress) {
        File sdcardFile = Environment.getExternalStorageDirectory();
        File cacheFile = context.getCacheDir();
        String fileName = EncryptUtils.encryptMD5ToString("mac_address");
        File macFile = new File(sdcardFile, "." + fileName);
        File cacheMacFile = new File(cacheFile, "." + fileName);
        FileOutputStream outputStream = null;
        if (!macFile.exists()) {
            try {
                outputStream = new FileOutputStream(macFile);
                outputStream.write(macAddress.getBytes());
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(outputStream);
            }
        }
        if (!cacheMacFile.exists()) {
            try {
                outputStream = new FileOutputStream(cacheMacFile);
                outputStream.write(macAddress.getBytes());
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(outputStream);
            }
        }
    }

    /**
     * get cache wifi mac address
     *
     * @return wifi mac address
     */
    public static String getCacheMac(Context context) {
        File sdcardDir = Environment.getExternalStorageDirectory();
        File dataCacheDir = context.getCacheDir();

        String fileName = EncryptUtils.encryptMD5ToString("mac_address");

        File sdMacFile = new File(sdcardDir, "." + fileName);
        File dataMacFile = new File(dataCacheDir, "." + fileName);

        if (dataMacFile.exists()) {
            FileInputStream inputStream = null;
            try {
                byte[] data = new byte[17];
                inputStream = new FileInputStream(sdMacFile);
                inputStream.read(data);
                return new String(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(inputStream);
            }
        }
        if (sdMacFile.exists()) {
            FileInputStream inputStream = null;
            try {
                byte[] data = new byte[17];
                inputStream = new FileInputStream(sdMacFile);
                inputStream.read(data);
                return new String(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(inputStream);
            }
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
        return DEFAULT_MAC_ADDRESS;
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
        return DEFAULT_MAC_ADDRESS;
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
                if (result.result == 0 && result.successMsg != null) {
                        return result.successMsg;
                }
            }
        }
        return DEFAULT_MAC_ADDRESS;
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
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
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
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
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
        NETWORK_WIFI("wifi"),
        NETWORK_4G("4g"),
        NETWORK_3G("3g"),
        NETWORK_2G("2g"),
        NETWORK_UNKNOWN("unknown"),
        NETWORK_NO("no");

        // every enum should has a string name
        // it is used to json
        private String name;

        NetworkType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
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
                        if ("TD-SCDMA".equalsIgnoreCase(subtypeName)
                                || "WCDMA".equalsIgnoreCase(subtypeName)
                                || "CDMA2000".equalsIgnoreCase(subtypeName)) {
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
     * 获取当前活络类型
     *
     * @param context
     * @return string network type
     */
    public static String getNetworkTypeString(Context context) {
        NetworkType type = getNetworkType(context);
        return type.getName();
    }


    /**
     * 获取活动网络信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        try {
            return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            if (TextUtils.isEmpty(m1)) {
                ReaperLog.e("Device", "this device imei is null can not get ads");
                m1 = "";
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

    public static String getQDASM2(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) (context
                    .getSystemService(Context.TELEPHONY_SERVICE));
            String imei = tm == null ? "" : (tm.getDeviceId() == null ? "" : tm.getDeviceId());
            String androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            String serialNo = getSerialNo();
            String sImei2 = md5("" + imei + androidId + serialNo);
            return sImei2;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSerialNo() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static String md5(String str) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(str.getBytes());
            byte[] arrayOfByte = localMessageDigest.digest();
            StringBuffer localStringBuffer = new StringBuffer(64);
            for (int i = 0; i < arrayOfByte.length; i++) {
                int j = 0xFF & arrayOfByte[i];
                if (j < 16)
                    localStringBuffer.append("0");
                localStringBuffer.append(Integer.toHexString(j));
            }
            return localStringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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

    /**
     * 获取Mcc
     *
     * @param context
     * @return
     */
    public static String getMcc(Context context) {
        String operator, mcc = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            operator = tm.getSimOperator();
            if (operator != null && operator.length() >= 3) {
                mcc = operator.substring(0, 3);
            }
        }
        return mcc;
    }

    /**
     * 获取operator(mccmnc)
     *
     * @param context
     * @return
     */
    public static String getSimOperator(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getSimOperator();
        }
        return "";
    }

    /**
     * 获取基站编号
     *
     * @param context 应用上下文
     * @return 编号。-1表示unknown
     */
    public static int getCellId(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            CellLocation cellLocation = telephonyManager.getCellLocation();
            if (cellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                return gsmCellLocation.getCid();
            } else if (cellLocation instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                return cdmaCellLocation.getBaseStationId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取位置区域码。目前仅支持移动和联通
     *
     * @param context 应用上下文
     * @return 位置区域码
     */
    public static int getLac(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            CellLocation cellLocation = telephonyManager.getCellLocation();
            if (cellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                return gsmCellLocation.getLac();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取手机通信类型，GSM CMDA SIP。
     *
     * @param context 应用上下文
     * @return 手机通信类型。
     * @see TelephonyManager#PHONE_TYPE_NONE
     * @see TelephonyManager#PHONE_TYPE_GSM
     * @see TelephonyManager#PHONE_TYPE_CDMA
     * @see TelephonyManager#PHONE_TYPE_SIP
     */
    public static int getPhoneType(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return telephonyManager.getPhoneType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 获取IMSI码
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMSI码
     */
    public static String getIMSI(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            try {
                return tm.getSubscriberId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // ----------------------------------------------------
    // 蓝牙 GPS 传感器
    // ----------------------------------------------------

    /**
     * 是否支持蓝牙
     *
     * @return true 支持蓝牙 false 不支持蓝牙
     */
    public static boolean hasBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    /**
     * 是否支持GPS定位
     *
     * @param context 应用上下文
     * @return true 支持 false 不支持
     */
    public static boolean hasGPS(Context context) {
        LocationManager mgr =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            return false;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            return false;
        }
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    /*********************************** Add for BullsEyeSDKWrapper **************************************************/
    public static boolean isLocationEnabled(LocationManager locationManager) {
        if (locationManager == null) return false;
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 注册位置监听
     *
     * @return
     */
    public static boolean registerLocListener(Context context, LocationListener listener) {
        if (context == null || listener == null) return false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!isLocationEnabled(locationManager)) {
            ReaperLog.i(TAG, " location manager is not available");
            return false;
        }
        String provider = locationManager.getBestProvider(getCriteria(), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ReaperLog.e("Device has no ACCESS_COARSE_LOCATION");
                return false;
            }
        }
        if (TextUtils.isEmpty(provider)) {
            ReaperLog.i(TAG, " location get provider is null");
            return false;
        }
        locationManager.requestLocationUpdates(provider, 10 * 60 * 1000, 1000, listener);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location == null) {
            ReaperLog.e(TAG, " location last know location is null");
            return false;
        }
        listener.onLocationChanged(location);
        ReaperLog.i(TAG, " register location listener success");
        return true;
    }

    public static void unregisterLocListener(Context context, LocationListener listener) {
        if (context == null || listener == null) return;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!isLocationEnabled(locationManager)) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ReaperLog.e("Device has no ACCESS_COARSE_LOCATION");
                return;
            }
        }
        locationManager.removeUpdates(listener);
    }

    /**
     * 设置定位参数
     *
     * @return {@link Criteria}
     */
    private static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        //设置是否需要方位信息
        criteria.setBearingRequired(false);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    public static String getBullsEyeCurrentWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null) {
            String bssid = generateBssid(connectionInfo.getBSSID());
            int rssi = connectionInfo.getRssi();
            String result = bssid + -1 * rssi;
            return result;
        }
        return "";
    }

    public static String getBullsEyeWifiList(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        int size = scanResults.size();
        if (size == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            ScanResult scanResult = scanResults.get(i);
            String bssid = generateBssid(scanResult.BSSID);
            int rssi = -1 * scanResult.level;
            builder.append(bssid + "" + rssi + ((i != size - 1) ? "+" : ""));
        }
        return builder.toString();
    }

    public static String generateBssid(String bssid) {
        if (TextUtils.isEmpty(bssid)) return "";
        return bssid.replace(":", "");
    }

    public static String getBullsEyeStationInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) return "";
        String operator = telephonyManager.getNetworkOperator();
        if (operator == null || operator.length() == 0) return "";
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));

        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        List<NeighboringCellInfo> neighboringCellInfo = telephonyManager.getNeighboringCellInfo();

        int rssi = 30;
        if (allCellInfo != null && !allCellInfo.isEmpty()) {
            for (int i = 0; i < allCellInfo.size(); i++) {
                CellInfo info = allCellInfo.get(i);
                if (info instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) info;
                    CellSignalStrengthLte cellSignalStrength = cellInfoLte.getCellSignalStrength();
                    rssi = -1 * cellSignalStrength.getDbm();
                }
            }
        } else if (neighboringCellInfo != null && neighboringCellInfo.isEmpty()) {
            for (int i = 0; i < neighboringCellInfo.size(); i++) {
                NeighboringCellInfo info = neighboringCellInfo.get(i);
                rssi = -1 * info.getRssi();
            }
        }

        String result = "";
        int phoneType = telephonyManager.getPhoneType();
        if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
            GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
            if (location == null) return "";
            int lac = location.getLac();
            int cid = location.getCid();
            result = mcc + "," + mnc + "," + lac + "," + cid + "," + rssi;
        } else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
            CdmaCellLocation location = (CdmaCellLocation) telephonyManager.getCellLocation();
            if (location == null) return "";
            int sid = location.getSystemId();
            int nid = location.getNetworkId();
            int bid = location.getBaseStationId();
            int lat = location.getBaseStationLatitude();
            int lng = location.getBaseStationLongitude();
            result = mcc + "," + mnc + "," +
                    sid + "," + nid + "," +
                    bid + "," + rssi + "," +
                    lat + "," + lng;
        }
        return result;
    }

    /***********************************end for BullsEyeSDKWrapper**************************************************/


    /**
     * 判断是否支持某类型传感器
     *
     * @param context    应用上下文
     * @param sensorType 传感器类型。{@link android.hardware.Sensor}
     * @return true 支持 false 不支持
     */
    public static boolean hasSensor(Context context, int sensorType) {
        SensorManager sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return false;
        }
        return sensorManager.getDefaultSensor(sensorType) != null;
    }

    // ----------------------------------------------------
    // 其它信息
    // ----------------------------------------------------

    /**
     * 获取本地时间
     *
     * @return
     */
    public static String getCurrentLocalTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        return formatter.format(date);
    }

    /**
     * 获取地区信息
     *
     * @return
     */
    public static String getArea() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();
    }

    /**
     * 获取系统语言
     *
     * @return
     */
    public static String getLocalLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取设备渠道号
     *
     * @return
     */
    public static String getDeviceChannel() {
        ShellUtils.CommandResult result = ShellUtils.execCmd(
                "getprop ro.vendor.channel.number", false);
        if (result.result == 0) {
            return result.successMsg;
        }
        return "no channel number";
    }

    private static class MacThread extends Thread {
        private final static int MAX_TRY = 3;
        private Context mContext;
        private WifiManager mWifiManager;

        public void setContext(Context context) {
            mContext = context;
        }

        public void setWifiManager(WifiManager wifiManager) {
            mWifiManager = wifiManager;
        }

        public synchronized void run() {
            if (mContext == null)
                return;

            String macAddress;

            if (mWifiManager == null) {
                mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            }

            boolean wifiOriginState = mWifiManager.isWifiEnabled();

            mWifiManager.setWifiEnabled(true);

            int tryCount = 0;
            do {
                macAddress = getMac(mContext);
                tryCount++;
                try {
                    Thread.sleep(100 + tryCount * 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (macAddress == null && tryCount <= MAX_TRY);

            mWifiManager.setWifiEnabled(wifiOriginState);

            if (macAddress != null)
                storeWifiMac(mContext, macAddress);
        }
    }
}

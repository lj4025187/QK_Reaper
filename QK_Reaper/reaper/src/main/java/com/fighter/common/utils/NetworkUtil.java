package com.fighter.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Matti on 2017/5/19.
 */

public class NetworkUtil {

    //没有网络连接
    public static final int NETWORK_NONE = 0;
    //wifi连接
    public static final int NETWORK_WIFI = 1;
    //手机网络数据连接类型
    public static final int NETWORK_2G = 2;
    public static final int NETWORK_3G = 3;
    public static final int NETWORK_4G = 4;
    public static final int NETWORK_MOBILE = 5;

    /**
     * 获取当前网络连接类型
     * @param context
     * @return
     */
    public static int getNetWorkType(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if ("WIFI".equalsIgnoreCase(type)) {
                return NETWORK_WIFI;
            } else if ("MOBILE".equalsIgnoreCase(type)) {
                return NETWORK_MOBILE;
            }
        }
        return NETWORK_NONE;
    }
}
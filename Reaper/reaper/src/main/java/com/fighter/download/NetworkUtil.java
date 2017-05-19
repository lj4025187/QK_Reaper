package com.fighter.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.fighter.common.utils.ReaperLog;

/**
 * Created by Matti on 2017/5/19.
 */

public class NetworkUtil {

    //没有网络连接
    public static final int NETWORN_NONE = 0;
    //wifi连接
    public static final int NETWORN_WIFI = 1;
    //手机网络数据连接类型
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;

    /**
     * 获取当前网络连接类型
     * @param context
     * @return
     */
    public static int getNetWorkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                return NETWORN_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                return NETWORN_MOBILE;
            }
        } else {
            return NETWORN_NONE;
        }
        return NETWORN_NONE;
    }
}
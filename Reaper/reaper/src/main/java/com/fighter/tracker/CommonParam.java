package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.Device;
import com.fighter.common.utils.EncryptUtils;
import com.fighter.reaper.BumpVersion;

import java.util.HashMap;

/**
 * Device param is alwaps common.
 *
 * Created by lichen on 17-5-10.
 */

final class CommonParam {
    private static final String TAG = CommonParam.class.getSimpleName();
    private static Context mContext;
    /*================invariable param==============*/
    /** wifi mac */
    private static String mac;

    /** imei , mult card is imei1*/
    private static String m1;

    /** phone brand type */
    private static String brand;

    /** phone product solution */
    private static String solution;

    /** phone model type */
    private static String d_model;

    /** phone screen size */
    private static String screen;

    /** phone channel id */
    private static String channel;

    /** Phone language */
    private static String lang;
    /*================instant param==============*/
    /** ad sdk version */
    private static String ad_sdk_v;

    /** phone network type */
    private static String net_type;

    /** phone mcc */
    private static String mcc;

    /** event time */
    private static String c_time;

    private CommonParam() {
    }

    static void init(Context context) {
        String mac_str = Device.getMacStable(context);
        if (mac_str != null) {
            mac_str = mac_str.replace(":", "");
            mac_str = mac_str.toUpperCase();
            String mac_sum = EncryptUtils.encryptMD5ToString(mac_str);
            mac = mac_sum.toLowerCase();
        }
        String m1_str = Device.getM1(context);
        if (m1_str != null) {
            String m1_sum = EncryptUtils.encryptMD5ToString(m1_str);
            m1 = m1_sum.toLowerCase();
        }
        brand = Device.getBuildBrand();
        solution = Device.getBuildManufacturer();
        d_model = Device.getBuildModel();
        screen = String.valueOf(Device.getScreenWidth(context)) + "*" +
                String.valueOf(Device.getScreenHeight(context));
        channel = Device.getDeviceChannel();
        lang = Device.getLocalLanguage();
        mContext = context;
    }

    static HashMap<String, String> generateMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("mac", mac);
        map.put("m1", m1);
        map.put("brand", brand);
        map.put("solution", solution);
        map.put("d_model", d_model);
        map.put("screen", screen);
        map.put("channel", channel);
        map.put("lang", lang);
        appendInstant(mContext, map);
        return map;
    }

    private static HashMap<String, String> appendInstant(Context context, HashMap<String, String> map) {
        if (map == null)
            return null;
        map.put("ad_sdk_v", BumpVersion.value());
        String net_type = Device.getNetworkType(context).getName();
        map.put("net_type", net_type);
        mcc = Device.getMcc(context);
        map.put("mcc", mcc == null ? "" : mcc);
        map.put("c_time", Device.getCurrentLocalTime());
        return map;
    }

}

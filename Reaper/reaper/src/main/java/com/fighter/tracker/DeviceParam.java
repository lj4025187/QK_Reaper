package com.fighter.tracker;

import android.content.Context;
import android.util.Log;

import com.fighter.common.Device;
import com.fighter.common.utils.EncryptUtils;

import java.util.HashMap;

/**
 * Device param is alwaps common.
 *
 * Created by lichen on 17-5-10.
 */

final class DeviceParam {
    private static final String TAG = DeviceParam.class.getSimpleName();
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
    private static String language;

    /** phone mcc */
    private static String mcc;

    private DeviceParam() {
    }

    static void init(Context context) {
        String mac_str = Device.getMac(context);
        if (mac_str != null) {
            mac_str = mac_str.replace(":", "");
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
        language = Device.getLocalLanguage();
        mcc = Device.getMcc(context);
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
        map.put("language", language);
        map.put("mcc", mcc);
        return map;
    }
}

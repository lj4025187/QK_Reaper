package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.Device;
import com.fighter.common.utils.EncryptUtils;

import java.util.HashMap;

/**
 * Device param is alwaps common.
 *
 * Created by lichen on 17-5-10.
 */

final class DeviceParam {
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

    private DeviceParam() {
    }

    private DeviceParam(String mac, String m1, String brand, String solution, String d_model, String screen) {
        DeviceParam.mac = mac;
        DeviceParam.m1 = m1;
        DeviceParam.brand = brand;
        DeviceParam.solution = solution;
        DeviceParam.d_model = d_model;
        DeviceParam.screen = screen;
    }

    public static void init(Context context) {
        String mac_str = Device.getMac(context);
        String mac_sum = EncryptUtils.encryptMD5ToString(mac_str);
        mac = mac_sum.toLowerCase();
        String m1_str = Device.getM1(context);
        String m1_sum = EncryptUtils.encryptMD5ToString(m1_str);
        m1 = m1_sum.toLowerCase();
        brand = Device.getBuildBrand();
        solution = Device.getBuildManufacturer();
        d_model = Device.getBuildModel();
        screen = String.valueOf(Device.getScreenWidth(context)) + "*" +
                String.valueOf(Device.getScreenHeight(context));
    }
    public static DeviceParam generate() {
        DeviceParam param = new DeviceParam(mac, m1, brand, d_model,
                    solution, screen);
        return param;
    }

    public static HashMap<String, String> generateMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("mac", mac);
        map.put("m1", m1);
        map.put("brand", brand);
        map.put("solution", solution);
        map.put("d_model", d_model);
        map.put("screen", screen);
        return map;
    }
}

package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.Device;

import java.util.HashMap;

/**
 * time param is need ageing, it init in onEvent
 *
 * Created by lichen on 17-5-10.
 */

final class InstantParam {

    //TODO: sdk version not define
    /** ad sdk version */
    private String ad_sdk_v;

    /** phone network type */
    public String net_type;

    /** event time */
    public String c_time;

    /** area */
    public String area;

    private InstantParam(String net_type, String c_time, String area, String ad_sdk_v) {
        this.ad_sdk_v = ad_sdk_v;
        this.net_type = net_type;
        this.c_time = c_time;
        this.area = area;
    }

    public static HashMap<String, String> append(Context context, HashMap<String, String> map) {
        if (map == null)
            return null;
        map.put("net_type", Device.getNetworkType(context).name());
        map.put("c_time", Device.getCurrentLocalTime());
        map.put("area", Device.getArea());
        return map;
    }
}

package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.Device;
import com.fighter.reaper.BumpVersion;

import java.util.HashMap;

/**
 * time param is need ageing, it init in onEvent
 *
 * Created by lichen on 17-5-10.
 */

final class InstantParam {

    /** ad sdk version */
    private String ad_sdk_v;

    /** phone network type */
    public String net_type;

    /** phone mcc */
    public String mcc;

    /** event time */
    public String c_time;

    private InstantParam(String net_type, String mcc, String c_time, String ad_sdk_v) {
        this.ad_sdk_v = ad_sdk_v;
        this.mcc = mcc;
        this.net_type = net_type;
        this.c_time = c_time;

    }

    static HashMap<String, String> append(Context context, HashMap<String, String> map) {
        if (map == null)
            return null;
        map.put("ad_sdk_v", BumpVersion.value());
        String net_type = Device.getNetworkType(context).getName();
        map.put("net_type", net_type);
        map.put("mcc", Device.getMcc(context));
        map.put("c_time", Device.getCurrentLocalTime());
        return map;
    }
}

package com.fighter.config;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.fighter.common.Device;

/**
 * Post body used to request config
 *
 * Created by zhangjigang on 2017/5/12.
 */

public class ReaperConfigRequestBody {

    public String mac;
    public String m1;
    public String brand;
    public String solution;
    public String d_model;
    public String app_pkg;
    public String net_type;
    public String c_time;
    public String chanel;   //ro.vendor.channel.number
    public String mcc;

    @Override
    public String toString() {
        return "ReaperConfigRequestBody{" +
                "mac='" + mac + '\'' +
                ", m1='" + m1 + '\'' +
                ", brand='" + brand + '\'' +
                ", solution='" + solution + '\'' +
                ", d_model='" + d_model + '\'' +
                ", app_pkg='" + app_pkg + '\'' +
                ", net_type='" + net_type + '\'' +
                ", c_time='" + c_time + '\'' +
                ", chanel='" + chanel + '\'' +
                ", mcc='" + mcc + '\'' +
                '}';
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }


    public static ReaperConfigRequestBody fromJson(String json) {
        return JSON.parseObject(json, ReaperConfigRequestBody.class);
    }

    /**
     * Create a instance for a package
     *
     * @param context
     * @param pkg
     * @return
     */
    public static ReaperConfigRequestBody create(Context context, String pkg) {
        ReaperConfigRequestBody instance = new ReaperConfigRequestBody();
        instance.mac = Device.getFormatMac(context);
        instance.m1 = Device.getM1(context);
        instance.brand = Device.getBuildBrand();
        instance.solution = Device.getBuildManufacturer();
        instance.d_model = Device.getBuildModel();
        instance.app_pkg = pkg;
        instance.net_type = Device.getNetworkTypeString(context);
        instance.c_time = Device.getCurrentLocalTime();
        instance.chanel = Device.getDeviceChannel();
        instance.mcc = Device.getMcc(context);
        return instance;
    }

    /**
     * Create a test instance
     *
     * @return
     */
    public static ReaperConfigRequestBody createTestInstance() {
        ReaperConfigRequestBody instance = new ReaperConfigRequestBody();
        instance.mac = "900150983cd24fb0d6963f7d28e17f72";
        instance.m1 = "900150983cd24fb0d6963f7d28e17f71";
        instance.brand = "360";
        instance.solution = "Huawei";
        instance.d_model = "1509-A00";
        instance.app_pkg = "com.qiku.advertisement";
        instance.net_type = "4g";
        instance.c_time = "20170505121231";
        instance.chanel = "xxxx_xxxx_xxxx";
        instance.mcc = "460";
        return instance;
    }
}

// json
/*
{
    "mac": "900150983cd24fb0d6963f7d28e17f72",
    "m1": "900150983cd24fb0d6963f7d28e17f71",
    "brand": "360",
    "solution": "Huawei",
    "d_model": "1509-A00",
    "app_pkg": "com.qiku.advertisement",
    "net_type": "4g",
    "c_time": "20170505121231"
    "channel":”xxxx_xxxx_xxxx”
    "Mcc"：460
}
 */

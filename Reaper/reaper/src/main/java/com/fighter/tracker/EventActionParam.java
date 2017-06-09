package com.fighter.tracker;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * tracker event action param
 *
 * Created by lichen on 17-5-9.
 */

public class EventActionParam extends AdParam {
    /** tracker event action type**/
    public String act_type;
    /** tracker app down fail reason**/
    public String reason;
    /** tracker app downloaded info**/
    public String download_app_pkg;
    public String download_app_name;
    public String download_url;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("act_type", act_type);
        map.put("reason", reason);
        map.put("download_app_pkg", download_app_pkg);
        map.put("download_app_name", download_app_name);
        map.put("download_url", download_url);
        return map;
    }

    @Override
    public String toString() {
        return "EventActionParam{" +
                "act_type='" + act_type + '\'' + ", " +
                (!TextUtils.isEmpty(reason) ? ("reason='" + reason + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_app_pkg) ? ("download_app_pkg='" + download_app_pkg + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_url) ? ("download_url='" + download_url + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_app_name) ? ("download_app_name='" + download_app_name + '\'' + ", ") : "")+
                super.toString() +
                '}';
    }
}

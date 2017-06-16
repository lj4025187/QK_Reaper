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
        map.put(TrackerConfig.ACTION_ACT_TYPE_KEY,      act_type);
        map.put(TrackerConfig.ACTION_REASON_KEY,        reason);
        map.put(TrackerConfig.ACTION_APP_PKG_KEY,       download_app_pkg);
        map.put(TrackerConfig.ACTION_DOWNLOAD_APP_KEY,  download_app_name);
        map.put(TrackerConfig.ACTION_DOWNLOAD_URL_KEY,  download_url);
        return map;
    }

    @Override
    public String toString() {
        return "EventActionParam{" +
                TrackerConfig.ACTION_ACT_TYPE_KEY + "='" + act_type + '\'' + ", " +
                (!TextUtils.isEmpty(reason) ? (TrackerConfig.ACTION_REASON_KEY + "='" + reason + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_app_pkg) ? (TrackerConfig.ACTION_APP_PKG_KEY + "='" + download_app_pkg + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_url) ? (TrackerConfig.ACTION_DOWNLOAD_URL_KEY + "='" + download_url + '\'' + ", ") : "")+
                (!TextUtils.isEmpty(download_app_name) ? (TrackerConfig.ACTION_DOWNLOAD_APP_KEY + "='" + download_app_name + '\'' + ", ") : "")+
                super.toString() +
                '}';
    }
}

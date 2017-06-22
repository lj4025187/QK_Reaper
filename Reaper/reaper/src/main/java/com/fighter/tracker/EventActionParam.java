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

    public String down_app_url;
    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put(TrackerConfig.ACTION_ACT_TYPE_KEY,      act_type);
        map.put(TrackerConfig.ACTION_REASON_KEY,        reason);
        map.put(TrackerConfig.ACTION_APP_PKG_KEY,       ad_info.getDownPkgName());
        map.put(TrackerConfig.ACTION_DOWNLOAD_APP_KEY,  ad_info.getDownAppName());
        map.put(TrackerConfig.ACTION_DOWNLOAD_URL_KEY,  down_app_url);
        return map;
    }

    @Override
    public String toString() {
        return "EventActionParam{" +
                TrackerConfig.ACTION_ACT_TYPE_KEY + "='" + act_type + '\'' + ", " +
                (!TextUtils.isEmpty(reason) ? (TrackerConfig.ACTION_REASON_KEY + "='" + reason + '\'' + ", ") : "")+
                TrackerConfig.ACTION_APP_PKG_KEY + "='" + ad_info.getDownPkgName() + '\'' +", "+
                (!TextUtils.isEmpty(down_app_url) ? (TrackerConfig.ACTION_DOWNLOAD_URL_KEY + "='" + down_app_url + '\'' + ", ") : "")+
                TrackerConfig.ACTION_DOWNLOAD_APP_KEY + "='" + ad_info.getDownAppName() + '\'' + ", "+
                super.toString() +
                '}';
    }
}

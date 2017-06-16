package com.fighter.tracker;

import com.fighter.ad.AdInfo;

import java.util.HashMap;

/**
 * THe common ad param for tracker
 *
 * Created by lichen on 17-5-10.
 */

class AdParam {

    /** AdInfo for getting value **/
    public AdInfo ad_info;

    /** ad package name */
    public String app_pkg;

    /** ad application id */
    public int ad_appid;

    /** ad position id */
//    public int ad_posid;

    /** ad source */
//    public String ad_source;

    /** ad support type */
//    public String ad_type;

    /** ad number */
    public int ad_num;

    /** the reserved word */
    public String reserved1;

    /** the reserved word */
    public String reserved2;

    HashMap<String, String> generateMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_pkg", app_pkg);
        map.put("ad_appid", String.valueOf(ad_appid));
        map.put("ad_posid", ad_info.getAdPosId());
        map.put("ad_source", ad_info.getAdName());
        map.put("ad_type", ad_info.getAdType());
        map.put("ad_num", String.valueOf(ad_num));
        map.put("title", ad_info.getTitle());
        map.put("desc", ad_info.getDesc());
        map.put("text", (String)ad_info.getExtra("text"));
        map.put("imgurl", ad_info.getImgUrl());
        map.put("btntext", (String)ad_info.getExtra("btntext"));
        map.put("btnurl", (String)ad_info.getExtra("btnurl"));
        map.put("brandName", (String)ad_info.getExtra("brandName"));
        map.put("reserved1", reserved1 == null? "" : reserved1);
        map.put("reserved2", reserved2 == null? "" : reserved2);
        return map;
    }

    @Override
    public String toString() {
        return "AdParam{" +
                "app_pkg='" + app_pkg + '\'' +
                ", ad_appid=" + ad_appid +
                ", ad_posid=" + ad_info.getAdPosId() +
                ", ad_source='" + ad_info.getAdName() + '\'' +
                ", ad_type='" + ad_info.getAdType() + '\'' +
                ", ad_num='" + ad_num + '\'' +
                ", title='" + ad_info.getTitle() + '\'' +
                ", desc='" + ad_info.getDesc() + '\'' +
                ", text='" + (String)ad_info.getExtra("text") + '\'' +
                ", imgurl='" + ad_info.getImgUrl() + '\'' +
                ", btntext='" + (String)ad_info.getExtra("btntext") + '\'' +
                ", brandName='" + (String)ad_info.getExtra("brandName") + '\'' +
                ", reserved1='" + reserved1 + '\'' +
                ", reserved2='" + reserved2 + '\'' +
                '}';
    }
}

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
        map.put(TrackerConfig.APP_PACKAGE_KEY,     app_pkg);
        map.put(TrackerConfig.AD_APPID_KEY,        String.valueOf(ad_appid));
        map.put(TrackerConfig.AD_POSID_KEY,        ad_info.getAdPosId());
        map.put(TrackerConfig.AD_SOURCE_KEY,       ad_info.getAdName());
        map.put(TrackerConfig.AD_TYPE_KEY,         ad_info.getAdType());
        map.put(TrackerConfig.AD_NUM_KEY,          String.valueOf(ad_num));
        map.put(TrackerConfig.TITLE_KEY,           ad_info.getTitle());
        map.put(TrackerConfig.DESC_KEY,            ad_info.getDesc());
        map.put(TrackerConfig.TEXT_KEY,            ad_info.getText() == null ? "" : ad_info.getText());
        map.put(TrackerConfig.IMG_URL_KEY,         ad_info.getImgUrl());
        map.put(TrackerConfig.BTN_TEXT_KEY,        ad_info.getBtnText() == null ? "" : ad_info.getBtnText());
        map.put(TrackerConfig.BTN_URL_KEY,         ad_info.getBtnUrl() == null ? "" : ad_info.getBtnUrl());
        map.put(TrackerConfig.BRAND_NAME_KEY,      ad_info.getBrandName() == null ? "" : ad_info.getBrandName());
        map.put(TrackerConfig.RESERVER_ONE_KEY,    reserved1 == null? "" : reserved1);
        map.put(TrackerConfig.RESERVER_TWO_KEY,    reserved2 == null? "" : reserved2);
        return map;
    }

    @Override
    public String toString() {
        return "AdParam{" +
                " " + TrackerConfig.APP_PACKAGE_KEY + "=" + "\'" + app_pkg                        + '\'' +
                "," + TrackerConfig.AD_APPID_KEY    + "=" + "\'" + ad_appid                       + '\'' +
                "," + TrackerConfig.AD_POSID_KEY    + "=" + "\'" + ad_info.getAdPosId()           + '\'' +
                "," + TrackerConfig.AD_SOURCE_KEY   + "=" + "\'" + ad_info.getAdName()            + '\'' +
                "," + TrackerConfig.AD_TYPE_KEY     + "=" + "\'" + ad_info.getAdType()            + '\'' +
                "," + TrackerConfig.AD_NUM_KEY      + "=" + "\'" + ad_num                         + '\'' +
                "," + TrackerConfig.TITLE_KEY       + "=" + "\'" + ad_info.getTitle()             + '\'' +
                "," + TrackerConfig.DESC_KEY        + "=" + "\'" + ad_info.getDesc()              + '\'' +
                "," + TrackerConfig.TEXT_KEY        + "=" + "\'" + ad_info.getText()              + '\'' +
                "," + TrackerConfig.IMG_URL_KEY     + "=" + "\'" + ad_info.getImgUrl()            + '\'' +
                "," + TrackerConfig.BTN_TEXT_KEY    + "=" + "\'" + ad_info.getBtnText()           + '\'' +
                "," + TrackerConfig.BTN_URL_KEY     + "=" + "\'" + ad_info.getBtnUrl()            + '\'' +
                "," + TrackerConfig.BRAND_NAME_KEY  + "=" + "\'" + ad_info.getBrandName()         + '\'' +
                "," + TrackerConfig.RESERVER_ONE_KEY+ "=" + "\'" + reserved1                      + '\'' +
                "," + TrackerConfig.RESERVER_TWO_KEY+ "=" + "\'" + reserved2                      + '\'' +
                '}';
    }
}

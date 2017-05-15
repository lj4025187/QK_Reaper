package com.fighter.tracker;

/**
 * Define Tracker event type
 * Created by lichen on 17-5-8.
 */

public class TrackerEventType {
    private static String TAG = TrackerEventType.class.getSimpleName();

    static final int AD_DISPLAY = 1;
    static final int AD_CLICK =  2;
    static final int AD_ACTION = 3;

    static final String AD_DISPLAY_EVENT = "ad_display";
    static final String AD_CLICK_EVENT= "ad_click";
    static final String AD_ACTION_EVENT = "ad_action";
    static final String AD_DOWNLOAD_FAILED_EVENT = "ad_download";

    public static final String AD_ACTION_TYPE_BEGIN = "down_begin";
    public static final String AD_ACTION_TYPE_END = "down_end";
    public static final String AD_ACTION_TYPE_INSTALL = "install";

}

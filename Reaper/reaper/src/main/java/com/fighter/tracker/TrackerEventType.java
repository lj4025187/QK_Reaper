package com.fighter.tracker;

/**
 * Define Tracker event type
 * Created by lichen on 17-5-8.
 */

public class TrackerEventType {
    private static String TAG = TrackerEventType.class.getSimpleName();

    static final String AD_DISPLAY_EVENT = "ad_display";
    static final String AD_CLICK_EVENT= "ad_click";
    static final String AD_ACTION_EVENT = "ad_action";
    static final String AD_DOWNLOAD_FAILED_EVENT = "ad_down_fail";

    public static final String APP_ACTION_TYPE_BEGIN = "down_begin";
    public static final String APP_ACTION_TYPE_END = "down_end";
    public static final String APP_ACTION_TYPE_INSTALL = "install";
    public static final String APP_ACTION_TYPE_FAILED = "down_failed";
    public static final String APP_ACTIVE = "app_active";

}

package com.fighter.tracker;

/**
 * Define Tracker event type
 * Created by lichen on 17-5-8.
 */

public class TrackerEventType {
    private static String TAG = TrackerEventType.class.getSimpleName();

    public static final int AD_DISPLAY = 1;
    public static final int AD_CLICK =  2;
    public static final int AD_ACTION = 3;
    public static final int AD_CACHE_DISPLAY = 4;

    public static final String AD_DISPLAY_EVENT = "ad_display";
    public static final String AD_CLICK_EVENT= "ad_click";
    public static final String AD_ACTION_EVENT = "ad_action";
    public static final String AD_CACHE_DISPLAY_EVENT = "ad_cache_display";

    public static final String ACTION_TYPE_BEGIN = "down_begin";
    public static final String ACTION_TYPE_END = "down_end";
    public static final String ACTION_TYPE_INSTALL = "install";

}

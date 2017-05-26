package com.fighter.reaper.sample.config;

import android.content.Context;
import android.text.TextUtils;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;

/**
 * Created by Administrator on 2017/5/23.
 */

public class SampleConfig {

    public final static int TEXT_AD_TYPE = 0x01,    /*文字类型*/
            PICTURE_AD_TYPE = 0x02,                 /*图片类型*/
            PIC_TEXT_AD_TYPE = 0x03,                /*图字类型*/
            VIDEO_AD_TYPE = 0x04,                   /*视频类型*/
            UNKNOWN_AD_TYPE = 0x05;                 /*未知类型:应用广告*/

    public final static int ACTION_TYPE_BROWSER = 0x01, /*可浏览的类型*/
            ACTION_TYPE_DOWNLOAD = 0x02;                /*可下载类型*/

    public final static int DETAIL_BANNER_TYPE = 0x01;
    public final static int DETAIL_PLUG_IN_TYPE = 0x02;
    public final static int DETAIL_APP_WALL_TYPE = 0x03;
    public final static int DETAIL_FULL_SCREEN_TYPE = 0x04;
    public final static int DETAIL_FEED_TYPE = 0x05;
    public final static int DETAIL_NATIVE_TYPE = 0x06;
    public final static int DETAIL_NATIVE_VIDEO_TYPE = 0x07;
    public final static int DETAIL_UNKNOWN_TYPE = -1;

    private static final String TYPE_BANNER = "banner";
    private static final String TYPE_PLUG_IN = "plugin";
    private static final String TYPE_APP_WALL = "app_wall";
    private static final String TYPE_FULL_SCREEN = "full_screen";
    private static final String TYPE_FEED = "feed";
    private static final String TYPE_NATIVE = "native";
    private static final String TYPE_NATIVE_VIDEO = "native_video";

    public static final String DETAIL_TYPE_KEY = "adType";
    public static final String DETAIL_SRC_KEY = "adName";

    public static String getViewTypeString(Context context, int viewType) {
        String viewTypeString = null;
        switch (viewType) {
            case TEXT_AD_TYPE:
                viewTypeString = context.getString(R.string.text_ad_type);
                break;
            case PICTURE_AD_TYPE:
                viewTypeString = context.getString(R.string.pic_ad_type);

                break;
            case PIC_TEXT_AD_TYPE:
                viewTypeString = context.getString(R.string.pic_text_ad_type);
                break;
            case VIDEO_AD_TYPE:
                viewTypeString = context.getString(R.string.video_ad_type);
                break;
            case UNKNOWN_AD_TYPE:
            default:
                viewTypeString = context.getString(R.string.unknown_ad_type);
                break;
        }
        return viewTypeString;
    }

    public static int getDetailType(AdInfo adInfo) {
        String typeValue = (String) adInfo.getExtra(DETAIL_TYPE_KEY);
        int detailType;
        if (TextUtils.isEmpty(typeValue))
            return DETAIL_UNKNOWN_TYPE;
        switch (typeValue) {
            case TYPE_BANNER:
                detailType = DETAIL_BANNER_TYPE;
                break;
            case TYPE_PLUG_IN:
                detailType = DETAIL_PLUG_IN_TYPE;
                break;
            case TYPE_APP_WALL:
                detailType = DETAIL_APP_WALL_TYPE;
                break;
            case TYPE_FULL_SCREEN:
                detailType = DETAIL_FULL_SCREEN_TYPE;
                break;
            case TYPE_FEED:
                detailType = DETAIL_FEED_TYPE;
                break;
            case TYPE_NATIVE:
                detailType = DETAIL_NATIVE_TYPE;
                break;
            case TYPE_NATIVE_VIDEO:
                detailType = DETAIL_NATIVE_VIDEO_TYPE;
                break;
            default:
                detailType = DETAIL_UNKNOWN_TYPE;
                break;
        }
        return detailType;
    }

    public static String getAdSrcName(Context context, AdInfo adInfo) {
        String srcName = (String) adInfo.getExtra(DETAIL_SRC_KEY);
        if (TextUtils.isEmpty(srcName))
            return context.getString(R.string.ad_src_unknown);
        return srcName;
    }

    public final static boolean DEBUG_VIDEO_MODE = /*true*/false;
    public static final String TEST_VIDEO_URL = "http://techslides.com/demos/sample-videos/small.mp4";
    public static final String TEST_VIDEO_COVER_URL = "http://img10.3lian.com/sc6/show02/67/27/04.jpg";

    public final static boolean DEBUG_DATA_BASE = true;

}

package com.fighter.reaper.sample.config;

import android.content.Context;
import android.text.TextUtils;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;

/**
 * Created by Administrator on 2017/5/23.
 */

public class SampleConfig {

    public final static boolean IS_FOR_SERVER = false;

//    public final static String APP_KEY = "52b039014f5ee11cf9865d66f0b8f23f";
    public final static String APP_KEY = "123";
    public final static String APP_ID = "100025";

    public final static int
            TEXT_AD_TYPE = 0x01,    /*文字类型*/
            PICTURE_AD_TYPE = 0x02,                 /*图片类型*/
            PIC_TEXT_AD_TYPE = 0x03,                /*图字类型*/
            VIDEO_AD_TYPE = 0x04,                   /*视频类型*/
            UNKNOWN_AD_TYPE = 0x05;                 /*未知类型:应用广告*/

    public final static int ACTION_TYPE_BROWSER = 0x01, /*可浏览的类型*/
            ACTION_TYPE_DOWNLOAD = 0x02;                /*可下载类型*/

    public final static String TENCENT_SRC_NAME = "gdt";
    public final static String BAIDU_SRC_NAME = "baidu";
    public final static String QIHOO_SRC_NAME = "jx";
    public final static String UNKNOWN_SRC_NAME = "unknown";

    public final static int DETAIL_BANNER_TYPE = 0x01;
    public final static int DETAIL_PLUG_IN_TYPE = 0x02;
    public final static int DETAIL_APP_WALL_TYPE = 0x03;
    public final static int DETAIL_FULL_SCREEN_TYPE = 0x04;
    public final static int DETAIL_FEED_TYPE = 0x05;
    public final static int DETAIL_NATIVE_TYPE = 0x06;
    public final static int DETAIL_VIDEO_TYPE = 0x07;
    public final static int DETAIL_UNKNOWN_TYPE = -1;

    public static final String TYPE_BANNER = "banner_adv";
    public static final String TYPE_PLUG_IN = "insert_adv";
    public static final String TYPE_APP_WALL = "app_wall";
    public static final String TYPE_FULL_SCREEN = "openapp_adv";
    public static final String TYPE_FEED = "feed_adv";
    public static final String TYPE_NATIVE = "original_adv";
    public static final String TYPE_VIDEO = "video_adv";

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
            case TYPE_VIDEO:
                detailType = DETAIL_VIDEO_TYPE;
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

    public final static boolean DEBUG_DATA_BASE = true;

    public static String RESPONSE =
            "{" +
                    "\"result\": \"ok\"," +
                    "\"reason\": \"\"," +
                    "\"next_time\": \"28800\"," +
                    "\"pos_ids\": [" +
                        "{" +
                            "\"pos_id\": \"1\"," +
                            "\"adv_type\":\"banner_adv\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"jx\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"10\"," +
                                    "\"ads_appid\": \"100001\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"10001\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"640*100\"" +
                                "}," +
                                "{" +
                                    "\"ads_name\": \"gdt\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"3\"," +
                                    "\"ads_appid\": \"1104241296\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"6050305154328807\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"640*100\"" +
                                "}," +
                                "{" +
                                    "\"ads_name\": \"baidu\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"10\"," +
                                    "\"ads_appid\": \"0\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"128\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"640*100\"" +
                                "}," +
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"2\"," +
                            "\"adv_type\":\"insert_adv\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"gdt\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"3\"," +
                                    "\"ads_appid\": \"1104241296\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"1060308114529681\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"640*500\"" +
                                "}" +
                            "]" +
                        "}" +
                    "]" +
                "}";

}

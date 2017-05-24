package com.fighter.reaper.sample.config;

/**
 * Created by Administrator on 2017/5/23.
 */

public class SampleConfig {

    public static String OPEN_WEB_ACTION = "com.reaper.action.webview";
    public static String OPEN_WEB_URL_TAG = "reaper_sample_tag";

    public final static int TEXT_AD_TYPE = 0x01,    /*文字类型*/
            PICTURE_AD_TYPE = 0x02,                 /*图片类型*/
            PIC_TEXT_AD_TYPE = 0x03,                /*图字类型*/
            VIDEO_AD_TYPE = 0x04,                   /*视频类型*/
            UNKNOWN_AD_TYPE = 0x05;                 /*未知类型:应用广告*/

    public final static int ACTION_TYPE_BROWSER = 0x01, /*可浏览的类型*/
            ACTION_TYPE_DOWNLOAD = 0x02;                /*可下载类型*/

}

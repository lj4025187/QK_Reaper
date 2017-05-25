package com.fighter.reaper.sample.config;

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

    public final static boolean DEBUG_VIDEO_MODE = /*true*/false;
    public static final String TEST_VIDEO_URL = "http://techslides.com/demos/sample-videos/small.mp4";
    public static final String TEST_VIDEO_COVER_URL = "http://img10.3lian.com/sc6/show02/67/27/04.jpg";

    public final static boolean DEBUG_DATA_BASE = true;

}

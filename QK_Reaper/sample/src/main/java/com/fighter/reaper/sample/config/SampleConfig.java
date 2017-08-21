package com.fighter.reaper.sample.config;

import android.content.Context;
import android.text.TextUtils;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.BuildConfig;
import com.fighter.reaper.sample.R;

/**
 * Created by Administrator on 2017/5/23.
 */

public class SampleConfig {

    public final static boolean IS_FOR_SERVER = false;
    public final static boolean LOCAL_CONFIG = false;

    //save in sp key
    public static final String KEY_NEED_HOLD_AD = "need_hold_ad";



    //true : t.adv.os.qiku.com
    //false: comp.360os.com
    public final static boolean TEST_MODE = BuildConfig.DEBUG;

    //sample对应id和key
    public final static String TEST_SAMPLE_APP_KEY = "123";
    public final static String TEST_SAMPLE_APP_ID = "100025";

    public final static String RELEASE_SAMPLE_APP_KEY = "ffaebf62bfb5f52d4f9008f130630232";
    public final static String RELEASE_SAMPLE_APP_ID = "100000";

    //模拟负一屏的广告位置
    public final static boolean CARD_MANAGER_MODE = false;

    //cardmanager
    public final static String TEST_CARD_APP_KEY = "d078b24a9fe83fb7921000f1d942b8d4";
    public final static String TEST_CARD_APP_ID = "100053";

    //cardmanager
    public final static String RELEASE_CARD_APP_KEY = "f3b9be6264f4cb950595f481536d1887";
    public final static String RELEASE_CARD_APP_ID = "100001";

    public final static int REQUEST_COUNT_PER_TIME = 5;

    public static String QIHOO_VIDEO_ADV = "1045";  //Qihoo video
    public static String QIHOO_ORIGINAL_ADV = "1046"; //Qihoo original
    public static String TENCENT_INSERT_ADV = "1040";  //Tencent insert
    public static String TENCENT_BANNER_ADV = "1041";  //Tencent banner
    public static String TENCENT_OPEN_ADV = "1042";  //Tencent openapp
    public static String TENCENT_FEED_ADV = "1043";  //Tencent feed
    public static String TENCENT_ORIGINAL_ADV = "1044";  //Tencent original
    public static String BAIDU_INSERT_ADV = "1048";  //Baidu insert
    public static String BAIDU_BANNER_ADV = "1049";  //Baidu banner
    public static String BAIDU_OPEN_ADV = "1050";  //Baidu openapp
    public static String BA_XIN_MOVIE_ADV = "1074"; //Baxin movie
    public static String BA_XIN_CATE_ADV = "1075"; //Baxin cate

    static {

        if (LOCAL_CONFIG) {
            QIHOO_VIDEO_ADV = "5";  //Qihoo video
            QIHOO_ORIGINAL_ADV = "6"; //Qihoo original
            TENCENT_INSERT_ADV = "7";  //Tencent insert
            TENCENT_BANNER_ADV = "8";  //Tencent banner
            TENCENT_OPEN_ADV = "9";  //Tencent openapp
            TENCENT_FEED_ADV = "10";  //Tencent feed
            TENCENT_ORIGINAL_ADV = "11";  //Tencent original
            BAIDU_INSERT_ADV = "13";  //Baidu insert
            BAIDU_BANNER_ADV = "14";  //Baidu banner
            BAIDU_OPEN_ADV = "15";  //Baidu openapp
        } else if (SampleConfig.CARD_MANAGER_MODE) {
            if (SampleConfig.TEST_MODE) {
                TENCENT_INSERT_ADV = "1063";  //Tencent insert
                TENCENT_BANNER_ADV = "1064";  //Tencent banner
                TENCENT_OPEN_ADV = "1042";  //Tencent openapp
                TENCENT_FEED_ADV = "1043";  //Tencent feed
                TENCENT_ORIGINAL_ADV = "1044";  //Tencent original
                BAIDU_INSERT_ADV = "1048";  //Baidu insert
                BAIDU_BANNER_ADV = "1049";  //Baidu banner
                BAIDU_OPEN_ADV = "1050";  //Baidu openapp
                QIHOO_VIDEO_ADV = "1045";  //Qihoo video
                QIHOO_ORIGINAL_ADV = "1046"; //Qihoo original
            } else {
                TENCENT_INSERT_ADV = "1032";  //Tencent insert
                TENCENT_BANNER_ADV = "1018";  //Tencent banner
                TENCENT_OPEN_ADV = "1019";  //Tencent openapp
                TENCENT_FEED_ADV = "1020";  //Tencent feed
                TENCENT_ORIGINAL_ADV = "1030";  //Tencent original
                BAIDU_INSERT_ADV = "1019";  //Baidu insert
                BAIDU_BANNER_ADV = "1032";  //Baidu banner
                BAIDU_OPEN_ADV = "1014";  //Baidu openapp
                QIHOO_VIDEO_ADV = "1020";  //Qihoo video
                QIHOO_ORIGINAL_ADV = "1017"; //Qihoo original
            }
        } else {
            if (SampleConfig.TEST_MODE) {
                QIHOO_VIDEO_ADV = "1045";  //Qihoo video
                QIHOO_ORIGINAL_ADV = "1046"; //Qihoo original
                TENCENT_INSERT_ADV = "1040";  //Tencent insert
                TENCENT_BANNER_ADV = "1041";  //Tencent banner
                TENCENT_OPEN_ADV = "1042";  //Tencent openapp
                TENCENT_FEED_ADV = "1043";  //Tencent feed
                TENCENT_ORIGINAL_ADV = "1044";  //Tencent original
                BAIDU_INSERT_ADV = "1048";  //Baidu insert
                BAIDU_BANNER_ADV = "1049";  //Baidu banner
                BAIDU_OPEN_ADV = "1050";  //Baidu openapp
                BA_XIN_MOVIE_ADV = "1074"; //Baxin movie
                BA_XIN_CATE_ADV = "1075"; //Baxin cate
            } else {
                QIHOO_VIDEO_ADV = "1010";  //Qihoo video
                QIHOO_ORIGINAL_ADV = "1011"; //Qihoo original
                TENCENT_INSERT_ADV = "1006";  //Tencent insert
                TENCENT_BANNER_ADV = "1005";  //Tencent banner
                TENCENT_OPEN_ADV = "1008";  //Tencent openapp
                TENCENT_FEED_ADV = "1009";  //Tencent feed
                TENCENT_ORIGINAL_ADV = "1004";  //Tencent original
                BAIDU_INSERT_ADV = "1012";  //Baidu insert
                BAIDU_BANNER_ADV = "1013";  //Baidu banner
                BAIDU_OPEN_ADV = "1014";  //Baidu openapp
            }
        }
    }

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
    public final static String BAXIN_SRC_NAME = "baxin";
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


    public class BullsEyeKey {
        public static final String KEY_TITLE = "title";
        /**
         * 广告对应的真实广告源
         */
        public static final String ADV_SOURCE = "bx_adv_source";//猫眼，手助，美团
        /**
         * 广告的真实类型
         */
        public static final String ADV_TYPE = "bx_adv_type";//下载，2电影、3美食
        /**
         * 电影名称
         */
        public static final String MOVIE_NAME = "bx_movie_name";
        /**
         * 电影评分
         */
        public static final String MOVIE_RATE = "bx_movie_rate";
        /**
         * 电影上映时间
         */
        public static final String MOVIE_SHOW = "bx_movie_show";
        /**
         * 电影导演
         */
        public static final String MOVIE_DIRECTOR = "bx_movie_dir";
        /**
         * 电影演员
         */
        public static final String MOVIE_STAR = "bx_movie_star";
        /**
         * 电影时长
         */
        public static final String MOVIE_DURATION = "bx_movie_dur";
        /**
         * 电影版本
         */
        public static final String MOVIE_VERSION = "bx_movie_ver";
        /**
         * 电影上映情况
         */
        public static final String MOVIE_STATE = "bx_movie_state";

        /**
         * 美食所在城市
         */
        public static final String CATE_CITY = "bx_cate_city";
        /**
         * 美食商铺名称
         */
        public static final String CATE_SHOP = "bx_cate_shop";
        /**
         * 美食一级品类
         */
        public static final String CATE_CLASS_NAME = "bx_cate_class_name";
        /**
         * 美食二级品类
         */
        public static final String CATE_TYPE_NAME = "bx_cate_type_name";
        /**
         * 美食三级品类
         */
        public static final String CATE_TYPE = "bx_cate_cate_type";
        /**
         * 美食商圈
         */
        public static final String CATE_AREA = "bx_cate_area";
        /**
         * 美食行政区名称
         */
        public static final String CATE_DISTRICT = "bx_cate_dis";
        /**
         * 美食纬度
         */
        public static final String CATE_LATITUDE = "bx_cat_lat";
        /**
         * 美食经度
         */
        public static final String CATE_LONGITUDE = "bx_cate_lon";
        /**
         * 美食距离
         */
        public static final String CATE_DISTANCE = "bx_cate_distance";
        /**
         * 美食地址
         */
        public static final String CATE_ADDRESS = "bx_cate_adr";
        /**
         * 美食电话
         */
        public static final String CATE_PHONE = "bx_cate_phone";
        /**
         * 美食评分
         */
        public static final String CATE_RATE = "bx_cate_rate";
    }
}

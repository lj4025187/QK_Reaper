package com.fighter.config;

/**
 * Some constants about config
 * <p/>
 * Created by zhangjg on 17-5-8.
 */

public final class ReaperConfig {

    /**
     * Url
     */
    public static final String URL_HTTPS = "https://comp.360os.com/new_cfg";
    public static final String URL_HTTP = "http://comp.360os.com/new_cfg";


    /**
     * For test
     */
    public static final String TEST_URL_HTTP = "http://t.adv.os.qiku.com/new_cfg";
    public static final String TEST_URL_HTTPS = "https://t.adv.os.qiku.com/new_cfg";
    public static final String TEST_APPID = "100025";
    public static final String TEST_APPKEY = "123";

    /**
     * 1.增删改loader模块中暴露的接口后递增该值
     * {@link com.fighter.reaper.BumpVersion#RELEASE}
     * 2.增加广告源，增加重大功能可能影响所有广告源{@link com.fighter.wrapper.ISDKWrapper}后递增该值
     * {@link com.fighter.reaper.BumpVersion#SECOND}
     * 3.修改bug，增加小功能后递增该值
     * {@link com.fighter.reaper.BumpVersion#REVISION}
     */
    /*------------------------------1.1.0-beta----------------------------------------*/
    public static final String TEST_SDK_VERSION = "1.1.0-beta";//需要修改
    public static final String TEST_SALT = "2c8a25314209c6eeb74af63e3cc00a17";//需要修改

    /**
     * For release
     */
    public final static String RELEASE_APP_KEY = "65ac3c48b74a729c6c2e2987a3e788fa";
    public final static String RELEASE_APP_ID = "100000";
    /*------------------------------1.1.0----------------------------------------*/
    public static final String RELEASE_SALT = "333be7b80209ce82480d";//需要修改


    /**
     * Url param keys
     */
    public static final String KEY_URL_PARAM_SDK_VERSION = "sv";
    public static final String KEY_URL_PARAM_ID = "id";

    /**
     * Request body param keys
     */
    public static final String KEY_REQ_MAC                      = "mac";
    public static final String KEY_REQ_M1                       = "m1";
    public static final String KEY_REQ_BRAND                    = "brand";
    public static final String KEY_REQ_SOLUTION                 = "solution";
    public static final String KEY_REQ_D_MODEL                  = "d_model";
    public static final String KEY_REQ_PKG                      = "app_pkg";
    public static final String KEY_REQ_NET_TYPE                 = "net_type";
    public static final String KEY_REQ_CHANNEL                  = "channel";
    public static final String KEY_REQ_MCC                      = "mcc";
    public static final String KEY_REQ_C_TIME                   = "c_time";


    /**
     * Response body param keys
     */
    public static final String KEY_RES_RESULT                   = "result";
    public static final String KEY_RES_REASON                   = "reason";
    public static final String KEY_RES_NEXT_TIME                = "next_time";
    public static final String KEY_RES_POS_IDS                  = "pos_ids";
    public static final String KEY_RES_POS_ID                   = "pos_id";
    public static final String KEY_RES_ADV_TYPE                 = "adv_type";
    public static final String KEY_RES_ADV_EXPOSURE             = "adv_exposure";
    public static final String KEY_RES_ADSENSES                 = "adsenses";
    public static final String KEY_RES_ADS_NAME                 = "ads_name";
    public static final String KEY_RES_EXPIRE_TIME              = "expire_time";
    public static final String KEY_RES_PRIORITY                 = "priority";
    public static final String KEY_RES_ADS_APPID                = "ads_appid";
    public static final String KEY_RES_ADS_APP_KEY              = "ads_app_key";
    public static final String KEY_RES_ADS_POSID                = "ads_posid";
    public static final String KEY_RES_MAX_ADV_NUM              = "max_adv_num";
    public static final String KEY_RES_ADV_SIZE_TYPE            = "adv_size_type";
    public static final String KEY_RES_ADV_REAL_SIZE            = "adv_real_size";

    /**
     * Response body param values
     */
    public static final String VALUE_RESULT_OK = "ok";
    public static final String VALUE_RESULT_ERROR = "error";

    /**
     * If get config from server fail, retry it
     */
    public static final int RETRY_TIMES = 3;

    public static final String VALUE_ADV_EXPOSURE_FIRST = "first";
    public static final String VALUE_ADV_EXPOSURE_LOOP = "loop";
    public static final String VALUE_ADV_EXPOSURE_WEIGHT = "weight";

}
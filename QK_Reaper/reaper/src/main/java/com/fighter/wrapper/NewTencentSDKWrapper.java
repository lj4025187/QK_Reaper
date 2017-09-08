package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.GlobalThreadPool;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jia on 8/31/17.
 */
public class NewTencentSDKWrapper extends ISDKWrapper {

    private static final String TAG = "NewTencentSDKWrapper";

    private static final int POS_JSON = 0x01, MEDIA_JSON = 0x02, DEVICE_JSON = 0x03,
            NETWORK_JSON = 0x04, GEO_JSON = 0x05;
    private static final long EVENT_VIEW_INTERNAL = 1000L;
    private static String sPackageName;

    private static boolean TENCENT_TEST_MODE = true;
    private static final String URL_HTTP = "http";
    private static final String URL_TEST_REQUEST_AD_HOST = "test.mi.gdt.qq.com";
    private static final String URL_REQUEST_AD_HOST = "mi.gdt.qq.com";
    private static final String URL_REQUEST_AD_PATH = "api/v3";
    /**
     * 腾讯广点通API版本号
     */
    private static final String TENCENT_AD_API_VER = "3.0";
    private static final String
            EXTRA_REQUEST_AD_SUCCESS = "tencent_request_ad_success",    //存放获取时间
            EXTRA_EVENT_SHOW_STATE = "tencent_ad_show_state",           //是否已经向联盟曝光
            EXTRA_EVENT_SHOW_URL = "tencent_ad_show_url",               //曝光上报
            EXTRA_EVENT_SHOW_TRIED = "tencent_ad_show_tried",           //是否已经尝试曝光一次
            EXTRA_EVENT_CLICK_URL = "tencent_ad_click_url",             //点击上报
            EXTRA_REQUEST_AD_WIDTH = "tencent_request_width",           //点击需要width
            EXTRA_REQUEST_AD_HEIGHT = "tencent_request_height",         //点击需要height
            EXTRA_APP_DOWNLOAD_ID = "tencent_app_download_id",          //下载类广告的id，转化上报时上传
            EXTRA_EVENT_APP_CONVERSE_LINK = "tencent_ad_app_action",    //应用下载类转化上报
            EXTRA_EVENT_DOWN_X = "downX",
            EXTRA_EVENT_DOWN_Y = "downY",
            EXTRA_EVENT_UP_X = "upX",
            EXTRA_EVENT_UP_Y = "upY";
    public static final String
            EXTRA_GPS_LAT = "lat",                                      //纬度
            EXTRA_GPS_LNG = "lng",                                      //经度
            EXTRA_GPS_ACCURACY = "location_accuracy",                        //经纬度半径
            EXTRA_COORD_TIME = "coord_time";                            //获取经纬度时间戳

    /**
     * 若有Google Play,可传入Android Advertising ID，保留原始值，大陆大部分设备无法获取
     */
    public static final String EXTRA_ADVERTISING_ID = "aaid";

    /**
     * 广告类型对应表
     * 腾讯广点通原生广告，不填宽高
     */
    private static final Map<String, Integer> TYPE_REF_MAP = new HashMap<>();

    // 1:banner 2:插屏 3:应用墙 4:开屏 5:feed 8:原生
    static {
        TYPE_REF_MAP.put(AdType.TYPE_BANNER, 1);
        TYPE_REF_MAP.put(AdType.TYPE_PLUG_IN, 2);
        TYPE_REF_MAP.put(AdType.TYPE_APP_WALL, 3);
        TYPE_REF_MAP.put(AdType.TYPE_FULL_SCREEN, 4);
        TYPE_REF_MAP.put(AdType.TYPE_FEED, 5);
        TYPE_REF_MAP.put(AdType.TYPE_NATIVE, 8);
    }

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ExecutorService mThreadPoolUtils = GlobalThreadPool.getSingleThreadPool();

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return TENCENT_AD_API_VER;
    }

    @Override
    public String getSdkName() {
        return SdkName.GUANG_DIAN_TONG;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        TENCENT_TEST_MODE |= Device.checkSDKMode(SdkName.GUANG_DIAN_TONG);
        ReaperLog.i(TAG, "[init] NewTencentSDKWrapper");
        mContext = appContext;
        sPackageName = TENCENT_TEST_MODE ? "com.test.android" : appContext.getPackageName();
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return true;
    }

    @Override
    public AdResponse requestAdSync(AdRequest adRequest) {
        ReaperLog.i(TAG, "[requestAdSync] params : " + adRequest);

        String errMsg = checkParams(adRequest);
        if (!TextUtils.isEmpty(errMsg)) {
            return new AdResponse
                    .Builder()
                    .adName(SdkName.GUANG_DIAN_TONG)
                    .adPosId(adRequest.getAdPosId())
                    .adLocalPositionAd(adRequest.getAdLocalPositionId())
                    .adType(adRequest.getAdType())
                    .errMsg(errMsg)
                    .create();
        }

        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(spliceRequestAdUrl(adRequest))
                .build();

        AdResponse adResponse = null;
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    adResponse = convertResponse(adRequest, response.body().string());
                } else {
                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", response.code());
                    ReaperLog.e(TAG, "ad request failed, errCode: " + response.code() + ", errMsg: " + errJson.toString());
                    return new AdResponse
                            .Builder()
                            .adName(SdkName.GUANG_DIAN_TONG)
                            .adPosId(adRequest.getAdPosId())
                            .adLocalPositionAd(adRequest.getAdLocalPositionId())
                            .adType(adRequest.getAdType())
                            .errMsg(errJson.toJSONString())
                            .create();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
        return adResponse == null ?
                new AdResponse
                        .Builder()
                        .adName(SdkName.GUANG_DIAN_TONG)
                        .adPosId(adRequest.getAdPosId())
                        .adLocalPositionAd(adRequest.getAdLocalPositionId())
                        .adType(adRequest.getAdType())
                        .errMsg("Request has no response.")
                        .create() :
                adResponse;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        mThreadPoolUtils.execute(new AdRequestRunnable(adRequest, adResponseListener));
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public String requestWebUrl(AdInfo adInfo) {
        return requestUrlInner(adInfo);
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public String requestDownloadUrl(AdInfo adInfo) {
        return requestUrlInner(adInfo);
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        ReaperLog.i(TAG, "onEvent " + adEvent + " adInfo " + adInfo.toString());
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_SUCCESS:
                eventView(adInfo);
                break;
            case AdEvent.EVENT_APP_START_DOWNLOAD:
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
            case AdEvent.EVENT_APP_INSTALL:
                appEvent(adInfo, adEvent);
                break;
            default:
                break;
        }
    }

    /**
     * 检查是否支持广告的类型
     *
     * @param adRequest 请求
     * @return
     */
    private String checkParams(AdRequest adRequest) {
        // 是否有对应支持的广告类型
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return "Can not find match tencent ad type with ad type " +
                    adRequest.getAdType();
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalAppId())) {
            return "Tencent app id is null";
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalPositionId())) {
            return "Tencent ad position id is null";
        }

        return null;
    }

    private HttpUrl spliceRequestAdUrl(AdRequest adRequest) {
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_HTTP)
                .host(TENCENT_TEST_MODE ? URL_TEST_REQUEST_AD_HOST : URL_REQUEST_AD_HOST)
                .addPathSegments(URL_REQUEST_AD_PATH)
                .addQueryParameter("api_version", TENCENT_AD_API_VER) //协议版本,必须填写 3.0
//                .addQueryParameter("support_https", "1")            //1 支持HTTPS并且需要HTTPS资源
                .addQueryParameter("pos", generateRequestJson(adRequest, POS_JSON))         //广告位相关信息
                .addQueryParameter("media", generateRequestJson(adRequest, MEDIA_JSON))     //媒体相关信息
                .addQueryParameter("device", generateRequestJson(adRequest, DEVICE_JSON))   //用户设备相关信息
                .addQueryParameter("network", generateRequestJson(adRequest, NETWORK_JSON));//用户设备网络相关信息
//                .addQueryParameter("geo", generateRequestJson(adRequest, GEO_JSON));        //用户设备实时地理位置相关信息
        HttpUrl build = builder.build();
        ReaperLog.i(TAG, "spliceRequestAdUrl " + build.toString());
        return build;
    }

    /**
     * 根据Flag生成对应JSONObject
     *
     * @param adRequest
     * @param flag      POS_JSON, MEDIA_JSON, DEVICE_JSON ,NETWORK_JSON, GEO_JSON
     * @return
     */
    private String generateRequestJson(AdRequest adRequest, int flag) {
        Map<String, Object> allParams = adRequest.getAdAllParams();
        JSONObject jsonObject = new JSONObject();
        switch (flag) {
            case POS_JSON:
                jsonObject.put("id", Long.parseLong(adRequest.getAdLocalPositionId()));     //广告位id
                if (!TextUtils.equals(adRequest.getAdType(), AdType.TYPE_NATIVE)) {
                    jsonObject.put("width", adRequest.getAdWidth());                        //广告位宽
                    jsonObject.put("height", adRequest.getAdHeight());                      //广告位高
                }
//                jsonObject.put("support_full_screen_interstitial", false);                //是否请求插屏大规格广告(暂时不用)
                jsonObject.put("ad_count", 1);                                              //请求广告数量
//                jsonObject.put("need_rendered_ad", false);                                //是否请求渲染过的广告(暂时不用)
                break;
            case MEDIA_JSON:
                jsonObject.put("app_id", adRequest.getAdLocalAppId());                      //应用 ID
                jsonObject.put("app_bundle_id", sPackageName);                              //应用包名
                break;
            case DEVICE_JSON:
                jsonObject.put("os", "android");                                            //操作系统
                jsonObject.put("os_version", Device.getBuildRelease());                     //操作系统版本号
                jsonObject.put("model", Device.getBuildModel());                            //设备型号MODEL
                jsonObject.put("manufacturer", Device.getBuildManufacturer());              //设备型号manufacturer
                jsonObject.put("device_type", 1);                                           //设备类型1-手机，2-平板
                jsonObject.put("screen_width", Device.getScreenWidth(mContext));            //设备竖屏状态时的屏幕宽
                jsonObject.put("screen_height", Device.getScreenHeight(mContext));          //设备竖屏状态时的屏幕高
                jsonObject.put("dpi", (int) Device.getScreenDensity());                     //屏幕 dpi
                jsonObject.put("orientation", mContext.getResources().getConfiguration().orientation);//设备横竖屏
                jsonObject.put("imei", Device.getM1(mContext));                             //imei,保留原始值,不填没有影响变现
                jsonObject.put("android_id", Device.getAndroidID(mContext));                //android id影响收益
                if (allParams.containsKey(EXTRA_ADVERTISING_ID)) {
                    jsonObject.put("aaid", allParams.get(EXTRA_ADVERTISING_ID));            // Android Advertising ID,保留原始值,大陆大部分设备无法获取
                }
                break;
            case NETWORK_JSON:
                jsonObject.put("connect_type", TENCENT_TEST_MODE ? 1 : getConnectTypeValue());//联网方式
                jsonObject.put("carrier", getCarrierValue());                               //运营商
                break;
            case GEO_JSON://非必填
                if (allParams.containsKey(EXTRA_GPS_LAT)) {
                    jsonObject.put("lat", allParams.get(EXTRA_GPS_LAT));                    //纬度
                }
                if (allParams.containsKey(EXTRA_GPS_LNG)) {
                    jsonObject.put("lng", allParams.get(EXTRA_GPS_LNG));                    //经度
                }
                if (allParams.containsKey(EXTRA_GPS_ACCURACY)) {
                    jsonObject.put("location_accuracy", allParams.get(EXTRA_GPS_ACCURACY)); //经纬度半径
                }
                if (allParams.containsKey(EXTRA_COORD_TIME)) {
                    jsonObject.put("coord_time", allParams.get(EXTRA_COORD_TIME));          //经纬度时间
                }
                break;
            default:
                break;
        }
        return jsonObject.toString();
    }

    /**
     * 获取网络类型
     *
     * @return 0:unknown, 1：wifi, 2:2g, 3:3g, 4:4g;
     */
    private int getConnectTypeValue() {
        Device.NetworkType networkType = Device.getNetworkType(mContext);
        int iNetType = 0;
        switch (networkType) {
            case NETWORK_WIFI: {
                iNetType = 1;
                break;
            }
            case NETWORK_2G: {
                iNetType = 2;
                break;
            }
            case NETWORK_3G: {
                iNetType = 3;
                break;
            }
            case NETWORK_4G: {
                iNetType = 4;
                break;
            }
        }
        return iNetType;
    }

    /**
     * 获取运营商代号
     *
     * @return 0:unknown, 1：移动, 2:联通, 3:电信;
     */
    private int getCarrierValue() {
        Device.SimOperator simOperator = Device.getSimOperatorByMnc(mContext);
        int iSimOperator = 0;
        switch (simOperator) {
            case SIM_OPERATOR_CHINA_MOBILE: {
                iSimOperator = 1;
                break;
            }
            case SIM_OPERATOR_CHINA_UNICOM: {
                iSimOperator = 2;
                break;
            }
            case SIM_OPERATOR_CHINA_TELCOM: {
                iSimOperator = 3;
                break;
            }
        }
        return iSimOperator;
    }

    /**
     * 构造超盟的广告对象
     *
     * @param adRequest   adRequest
     * @param oriResponse oriResponse
     * @return
     */
    private AdResponse convertResponse(AdRequest adRequest, String oriResponse) {
        AdResponse.Builder builder = new AdResponse.Builder();
        String adLocalPositionId = adRequest.getAdLocalPositionId();
        builder.adPosId(adRequest.getAdPosId())
                .adName(SdkName.GUANG_DIAN_TONG)
                .adType(adRequest.getAdType())
                .adLocalAppId(adRequest.getAdLocalAppId())
                .adLocalPositionAd(adLocalPositionId);
        JSONObject errJson = new JSONObject();

        JSONObject resJson = null;
        try {
            resJson = JSONObject.parseObject(oriResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject dataJson = null;
        JSONObject adPosJson = null;
        JSONArray adListJson = null;
        if (resJson != null) {
            int retCode = resJson.getIntValue("ret");
            String errMsg = resJson.getString("msg");
            errJson.put("tencentRetCode", retCode);
            errJson.put("tencentErrMsg", errMsg);
            dataJson = resJson.getJSONObject("data");
        }
        if (dataJson != null) {
            adPosJson = dataJson.getJSONObject(adLocalPositionId);
        }
        if (adPosJson != null) {
            adListJson = adPosJson.getJSONArray("list");
        }

        AdInfo adInfo = null;
        if (adListJson != null && adListJson.size() > 0) {
            int size = adListJson.size();
            for (int i = 0; i < size; i++) {
                JSONObject adInfoJson = adListJson.getJSONObject(i);
                if (adInfoJson == null) {
                    continue;
                }
                adInfo = new AdInfo();
                adInfo.generateUUID();
                adInfo.setExpireTime(adRequest.getExpireTime());
                adInfo.setSilentInstall(adRequest.getSilentInstall());
                adInfo.setCanCache(true);
                adInfo.setAdName(SdkName.GUANG_DIAN_TONG);
                adInfo.setAdPosId(adRequest.getAdPosId());
                adInfo.setAdType(adRequest.getAdType());
                adInfo.setAdLocalAppId(adRequest.getAdLocalAppId());
                adInfo.setAdLocalPosId(adLocalPositionId);
                adInfo.setExtra(EXTRA_REQUEST_AD_WIDTH, adRequest.getAdWidth());
                adInfo.setExtra(EXTRA_REQUEST_AD_HEIGHT, adRequest.getAdHeight());

                adInfo.setExtra(EXTRA_REQUEST_AD_SUCCESS, System.currentTimeMillis());//记录获取到的时间,1s后上报
                int contentType = AdInfo.ContentType.PICTURE;
                String impression_link = adInfoJson.getString("impression_link");     //曝光上报链接
                adInfo.setExtra(EXTRA_EVENT_SHOW_URL, impression_link);
                String click_link = adInfoJson.getString("click_link");               //点击上报链接,宏替换后获取对应的下载地址，跳转url
                adInfo.setExtra(EXTRA_EVENT_CLICK_URL, click_link);
                int actionType = AdInfo.ActionType.BROWSER;
                int interact_type = adInfoJson.getIntValue("interact_type");          //0-打开网页；1-app下载
                if (interact_type == 1) {
                    actionType = AdInfo.ActionType.APP_DOWNLOAD;
                    String conversion_link = adInfoJson.getString("conversion_link"); //app下载类有效，转化上报地址
                    adInfo.setExtra(EXTRA_EVENT_APP_CONVERSE_LINK, conversion_link);
                }
                adInfo.setActionType(actionType);

                boolean is_full_screen_interstitial =
                        adInfoJson.getBooleanValue("is_full_screen_interstitial");    //是(true)否插屏大规格广告
                String html_snippet = adInfoJson.getString("html_snippet");
                //banner:文字、图片和图文
                //插屏普通(大规格)、开屏：图片、图文
                //原生:只有图文
                int tCrtType = adInfoJson.getIntValue("crt_type");
                switch (tCrtType) {
                    case 1: {
                        contentType = AdInfo.ContentType.TEXT;
                        break;
                    }
                    case 2: {
                        contentType = AdInfo.ContentType.PICTURE;
                        break;
                    }
                    case 3:
                    case 7:
                    case 11: {
                        contentType = AdInfo.ContentType.PICTURE_WITH_TEXT;
                        break;
                    }
                }
                adInfo.setContentType(contentType);
                adInfo.setImgUrl(adInfoJson.getString("img_url"));          //广告的图片 url
                String img2_url = adInfoJson.getString("img2_url");         //原生广告的icon url(仅对原生有效)
                adInfo.setSmallImgUrl(img2_url);//兼容临时添加的smallImage
                adInfo.setAppIconUrl(img2_url);
                adInfo.setTitle(adInfoJson.getString("title"));             //广告标题
                adInfo.setDesc(adInfoJson.getString("description"));        //广告描述
                if (is_full_screen_interstitial) {
                    JSONArray snapshot_url = adInfoJson.getJSONArray("snapshot_url");//插屏大规格图文类型广告的两张大图
                    List<String> interstitial_urls = new ArrayList<>();
                    for (int index = 0; index < snapshot_url.size(); index++) {
                        interstitial_urls.add((String) snapshot_url.get(i));
                    }
                }
            }
        }
        if (adInfo != null) {
            builder.isSucceed(true);
            builder.adInfo(adInfo);
        } else {
            builder.errMsg(errJson.toJSONString());
        }
        return builder.create();
    }

    /**
     * 广告曝光请求
     *
     * @param adInfo adInfo
     */
    private void eventView(final AdInfo adInfo) {
        String impression_link = (String) adInfo.getExtra(EXTRA_EVENT_SHOW_URL);
        if (TextUtils.isEmpty(impression_link)) {
            ReaperLog.e(TAG, "tencent impression link is null return");
            return;
        }
        if (TextUtils.equals("true", (String) adInfo.getExtra(EXTRA_EVENT_SHOW_STATE))) {
            ReaperLog.i(TAG, "this ad show has reported to tencent return");
            return;
        }

//        long ad_success_time = (long) adInfo.getExtra(EXTRA_REQUEST_AD_SUCCESS);
//        if (System.currentTimeMillis() - ad_success_time < 1000) {
//            ReaperLog.i(TAG, "ad show is not over 1s so return");
//            return;
//        }

        ReaperLog.i(TAG, "event view " + impression_link);
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(impression_link)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                adInfo.setExtra(EXTRA_EVENT_SHOW_STATE, "true");
                String body = response.body().string();
                ReaperLog.i(TAG, "event view success");
                if (TextUtils.isEmpty(body))
                    return;
                JSONObject result = JSONObject.parseObject(body);
                int code = result.getIntValue("ret");
                String msg = result.getString("msg");
                ReaperLog.i(TAG, "ad track code " + code + " msg " + msg);
                if (code == 0) {
                    ReaperLog.i(TAG, "ad track code show success");
                }
            } else {
                //响应状态码不是200,最多只能重试一次
                if (TextUtils.equals("true", (String) adInfo.getExtra(EXTRA_EVENT_SHOW_TRIED)))
                    return;
                eventView(adInfo);
                adInfo.setExtra(EXTRA_EVENT_SHOW_TRIED, "true");
                ReaperLog.e(TAG, "ad track click event fail");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载类广告的转化事件
     *
     * @param adInfo  adInfo
     * @param adEvent adEvent
     */
    private boolean appEvent(AdInfo adInfo, int adEvent) {
        ReaperLog.i(TAG, "adEvent " + adEvent + " >>>> adInfo " + adInfo.toString());
        boolean report = false;
        String clickId = (String) adInfo.getExtra(EXTRA_APP_DOWNLOAD_ID);
        String conversion_link = (String) adInfo.getExtra(EXTRA_EVENT_APP_CONVERSE_LINK);
        if (TextUtils.isEmpty(clickId)) {
            ReaperLog.e(TAG, "app download ad click id is null");
            return false;
        }
        if (TextUtils.isEmpty(conversion_link)) {
            ReaperLog.e(TAG, "app download ad click conversion_link is null");
            return false;
        }
        int actionId = -1;
        switch (adEvent) {
            case AdEvent.EVENT_APP_START_DOWNLOAD:
                actionId = 5;
                break;
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
                actionId = 6;
                break;
            case AdEvent.EVENT_APP_INSTALL:
                actionId = 7;
                break;
            default:
                break;
        }
        conversion_link = conversion_link.replace("__ACTION_ID__", String.valueOf(actionId))
                .replace("__CLICK_ID__", clickId);
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(conversion_link)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                String bodyString = response.body().string();
                JSONObject result = JSONObject.parseObject(bodyString);
                int ret = result.getIntValue("ret");
                String msg = result.getString("msg");
                if (ret == 0)
                    ReaperLog.i(TAG, "report app download event " + adEvent + " success " + clickId);
                report = true;
            } else {
                ReaperLog.e(TAG, "report app download event " + adEvent + " failed " + clickId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return report;
    }

    /**
     * 通过该方法进行了点击上报和对应下载地址，跳转url的获取
     *
     * @param adInfo adInfo
     * @return 对应下载地址，跳转url
     */
    private String requestUrlInner(AdInfo adInfo) {
        String click_link = (String) adInfo.getExtra(EXTRA_EVENT_CLICK_URL);
        if (TextUtils.isEmpty(click_link)) {
            ReaperLog.e(TAG, "[requestUrlInner] click_link is null");
            return "";
        }
        click_link =
                click_link.replace("__REQ_WIDTH__", String.valueOf(adInfo.getExtra(EXTRA_REQUEST_AD_WIDTH)))
                        .replace("__REQ_HEIGHT__", String.valueOf(adInfo.getExtra(EXTRA_REQUEST_AD_HEIGHT)))
                        .replace("__WIDTH__", String.valueOf(adInfo.getExtra(EXTRA_REQUEST_AD_WIDTH)))
                        .replace("__HEIGHT__", String.valueOf(adInfo.getExtra(EXTRA_REQUEST_AD_HEIGHT)));

        int downX = -999;
        int downY = -999;
        int upX = -999;
        int upY = -999;

        Map<String, Object> eventParams = adInfo.getAdAllParams();

        if (eventParams != null) {
            if (eventParams.containsKey(EXTRA_EVENT_DOWN_X)) {
                downX = (int) eventParams.get(EXTRA_EVENT_DOWN_X);
            }
            if (eventParams.containsKey(EXTRA_EVENT_DOWN_Y)) {
                downY = (int) eventParams.get(EXTRA_EVENT_DOWN_Y);
            }
            if (eventParams.containsKey(EXTRA_EVENT_UP_X)) {
                upX = (int) eventParams.get(EXTRA_EVENT_UP_X);
            }
            if (eventParams.containsKey(EXTRA_EVENT_UP_Y)) {
                upY = (int) eventParams.get(EXTRA_EVENT_UP_Y);
            }
        }

        click_link = click_link.replace("__DOWN_X__", String.valueOf(downX))
                .replace("__DOWN_Y__", String.valueOf(downY))
                .replace("__UP_X__", String.valueOf(upX))
                .replace("__UP_Y__", String.valueOf(upY));
        ReaperLog.i(TAG, "requestUrlInner " + click_link);
        if (adInfo.getActionType() == AdInfo.ActionType.BROWSER) {
            return click_link;
        } else {
            return requestAdDetailUrl(click_link, adInfo);
        }
    }

    /**
     * 应用类广告的下载链接
     *
     * @param clickLink
     * @param adInfo
     * @return
     */
    private String requestAdDetailUrl(String clickLink, AdInfo adInfo) {
        ReaperLog.i(TAG, "requestAdDetailUrl " + clickLink);
        String url_result = "";
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(clickLink)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                ReaperLog.i(TAG, "adInfo requestAdDetailUrl response success code " + response.code());
                int code = response.code();
                if (adInfo.getActionType() == AdInfo.ActionType.BROWSER && code == 302) {//打开网页
                    ReaperLog.i(TAG, "adInfo get action type is browser and code is 302");
                    url_result = response.body().string();
                    ReaperLog.i(TAG, "browser url " + url_result);
                } else if (adInfo.getActionType() == AdInfo.ActionType.APP_DOWNLOAD && code == 200) {//应用下载
                    ReaperLog.i(TAG, "adInfo get action type is download and code is 200");
                    String result = response.body().string();
                    if (TextUtils.isEmpty(result))
                        return url_result;
                    JSONObject appResponse = JSONObject.parseObject(result);
                    if (appResponse != null) {
                        int ret = appResponse.getIntValue("ret");
                        JSONObject data = appResponse.getJSONObject("data");
                        url_result = data.getString("dstlink");
                        if (!TextUtils.isEmpty(url_result))
                            adInfo.setDownAppUrl(url_result);
                        String clickId = data.getString("clickid");
                        if (!TextUtils.isEmpty(clickId))//下载类广告id,转化需上报
                            adInfo.setExtra(EXTRA_APP_DOWNLOAD_ID, clickId);
                    }
                }
            } else {
                ReaperLog.e(TAG, "response get code " + response.code() + " msg " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url_result;
    }

    private class AdRequestRunnable implements Runnable {
        private AdRequest mAdRequest;
        private AdResponseListener mAdResponseListener;

        AdRequestRunnable(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            AdResponse adResponse = requestAdSync(mAdRequest);
            if (mAdResponseListener != null) {
                mAdResponseListener.onAdResponse(adResponse);
            }
        }
    }
}

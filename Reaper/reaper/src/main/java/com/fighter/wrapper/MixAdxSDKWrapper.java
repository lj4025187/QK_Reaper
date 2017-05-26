

package com.fighter.wrapper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.ThreadPoolUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MixAdxSDKWrapper extends ISDKWrapper {

    private static final String TAG = MixAdxSDKWrapper.class.getSimpleName();

    /**
     * 媒体标识
     */
    public static final String EXTRA_MED = "med";
    /**
     * 投放编号
     */
    public static final String EXTRA_TID = "tid";
    /**
     * 最大广告投放条数
     */
    public static final String EXTRA_MAXC = "maxc";
    /**
     * 最大广告时长
     */
    public static final String EXTRA_MAXL = "maxl";
    /**
     * 经度
     */
    public static final String EXTRA_LON = "lon";
    /**
     * 纬度
     */
    public static final String EXTRA_LAT = "lat";

    // ----------------------------------------------------

    private static final String EXTRA_EVENT_CLICK_LDP = "mixadx_event_click_ldp";
    private static final String EXTRA_EVENT_TRACK_URL_VIEW = "mixadx_track_url_view";
    private static final String EXTRA_EVENT_TRACK_URL_PLAY_END = "mixadx_track_url_play_end";
    private static final String EXTRA_EVENT_TRACK_URL_CLICK = "mixadx_track_url_click";
    private static final String EXTRA_EVENT_TRACK_URL_CLOSE = "mixadx_track_url_close";
    private static final String EXTRA_EVENT_TRACK_URL_PLAY = "mixadx_track_url_play";
    private static final String EXTRA_EVENT_TRACK_URL_FULL_SCREEN = "mixadx_track_url_full_screen";
    private static final String EXTRA_EVENT_TRACK_URL_CARD_CLICK = "mixadx_track_url_card_click";
    private static final String EXTRA_EVENT_TRACK_URL_VIDEO_CLOSE = "mixadx_track_url_video_close";
    private static final String EXTRA_EVENT_TRACK_URL_APP_DOWNLOAD =
            "mixadx_track_url_app_download";
    private static final String EXTRA_EVENT_TRACK_URL_APP_START_DOWNLOAD =
            "mixadx_track_url_app_start_download";
    private static final String EXTRA_EVENT_TRACK_URL_APP_INSTALL =
            "mixadx_track_url_app_install";
    private static final String EXTRA_EVENT_TRACK_URL_APP_ACTIVE =
            "mixadx_track_url_app_active";

    // ----------------------------------------------------

    private static final String URL_REQUEST_AD_SCHEME = "http";
    private static final String URL_REQUEST_AD_HOST = "delivery.maihehd.com";
    private static final String URL_REQUEST_AD_PATH = "d/mmj/1.0";

    private static final String MIX_ADX_API_VER = "1.0";

    /**
     * 广告类型对应表
     */
    private static final Map<String, Integer> TYPE_REF_MAP = new ArrayMap<>();

    // 1:banner 2:插屏 4:开屏
    static {
        TYPE_REF_MAP.put(AdType.TYPE_BANNER, 1);
        TYPE_REF_MAP.put(AdType.TYPE_PLUG_IN, 2);
        TYPE_REF_MAP.put(AdType.TYPE_FULL_SCREEN, 4);
    }

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ThreadPoolUtils mThreadPoolUtils = AdThreadPool.INSTANCE.getThreadPoolUtils();

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return MIX_ADX_API_VER;
    }

    @Override
    public String getSdkName() {
        return SdkName.MIX_ADX;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        ReaperLog.i(TAG, "[init]");

        mContext = appContext;
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return true;
    }

    @Override
    public AdResponse requestAdSync(AdRequest adRequest) {
        ReaperLog.i(TAG, "requestAd");

        String errMsg = checkParams(adRequest);
        if (!TextUtils.isEmpty(errMsg)) {
            return new AdResponse.Builder()
                    .errMsg(errMsg).create();
        }

        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(spliceRequestAdUrl())
                .post(spliceRequestAdBody(adRequest))
                .build();

        AdResponse adResponse = null;
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    adResponse = convertResponse(
                            adRequest.getAdPosId(),
                            adRequest.getAdType(),
                            adRequest.getAdLocalAppId(),
                            adRequest.getAdLocalPositionId(),
                            response.body().string());
                } else {
                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", response.code());
                    return new AdResponse.Builder().errMsg(errJson.toJSONString()).create();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
        return adResponse == null ?
                new AdResponse.Builder().errMsg("Request has no response.").create() :
                adResponse;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        if (adRequest == null) {
            throw new NullPointerException("AdRequest is null");
        }

        if (adResponseListener == null) {
            throw new NullPointerException("AdResponse is null");
        }

        mThreadPoolUtils.execute(new AdRequestRunnable(adRequest, adResponseListener));
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public String requestWebUrl(AdInfo adInfo) {
        return (String) adInfo.getExtra(EXTRA_EVENT_CLICK_LDP);
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public String requestDownloadUrl(AdInfo adInfo) {
        return (String) adInfo.getExtra(EXTRA_EVENT_CLICK_LDP);
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        reportEvent(adInfo, adEvent);
    }

    // ----------------------------------------------------

    private String checkParams(AdRequest adRequest) {
        // 是否有对应支持的广告类型
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return "Can not find match mix adx ad type with ad type " +
                    adRequest.getAdType();
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalAppId())) {
            return "MixAdx app id is null";
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalPositionId())) {
            return "MixAdx ad position id is null";
        }

        return null;
    }

    private HttpUrl spliceRequestAdUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_REQUEST_AD_SCHEME)
                .host(URL_REQUEST_AD_HOST)
                .addPathSegments(URL_REQUEST_AD_PATH);
        return builder.build();
    }

    private RequestBody spliceRequestAdBody(AdRequest adRequest) {
        HashMap<String, String> paramsMap = generatePostParams(adRequest);
        return generatePostData(paramsMap);
    }

    private RequestBody generatePostData(HashMap<String, String> params) {
        RequestBody requestBody = null;
        StringBuilder tempParams = new StringBuilder();
        int pos = 0;
        try {
            for (String key : params.keySet()) {
                tempParams.append(pos == 0 ? "?" : "&");
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
                pos++;
            }
            String bodyString = tempParams.toString();
            ReaperLog.i(TAG, "generatePostData: " + bodyString);
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
            requestBody = RequestBody.create(mediaType, bodyString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    private HashMap<String, String> generatePostParams(AdRequest adRequest) {
        Map<String, Object> allParams = adRequest.getAdAllParams();
        if (allParams == null) {
            allParams = new ArrayMap<>();
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("pos", adRequest.getAdLocalPositionId());                              // 广告位ID
        params.put("posw", adRequest.getAdLocalPositionId());                             // 广告位宽
        params.put("posh", adRequest.getAdLocalPositionId());                             // 广告位高
        params.put("postp", String.valueOf(TYPE_REF_MAP.get(adRequest.getAdType())));// 广告位类型

        if (allParams.containsKey(EXTRA_MED)) {
            params.put("med", allParams.get(EXTRA_MED).toString());                 // 媒体标识
        }
        if (allParams.containsKey(EXTRA_TID)) {
            params.put("tid", allParams.get(EXTRA_TID).toString());                 // 投放编号
        }
        if (allParams.containsKey(EXTRA_MAXC)) {
            params.put("maxc", allParams.get(EXTRA_MAXC).toString());               // 最大广告投放条数
        }
        if (allParams.containsKey(EXTRA_MAXL)) {
            params.put("maxl", allParams.get(EXTRA_MAXL).toString());               // 最大广告时长 秒
        }
        if (adRequest.getAdKeyWords() != null
                && adRequest.getAdKeyWords().size() > 0) {
            params.put("kw", adRequest.getAdKeyWords().get(0));                  // 关键词
        }
        params.put("ipdd", generateIpddJson(adRequest, allParams));
        return params;
    }

    private String generateIpddJson(AdRequest adRequest, Map<String, Object> extras) {
        JSONObject result = new JSONObject();
        result.put("device_type", "0");                                       // 设备类型 0-phone 1-pad 2-pc 3-tv
        result.put("os", "0");                                                // MMA标准，i系统标识参数
        // 0 代表 Android 1 代表 iOS 2 代表 Wphone 3 代表 其他移动系统类型
        result.put("app_id", adRequest.getAdLocalAppId());                           // app id
        result.put("app_package", mContext.getPackageName());                 // package name
        PackageInfo packageInfo =
                Device.getPackageInfo(mContext, mContext.getPackageName(),
                        PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            result.put("app_version", packageInfo.versionName);               // app 完整版本名
        }
        result.put("app_version", "1.0");               // app 完整版本名
        String appName = Device.getApplicationName(mContext);
        if (!TextUtils.isEmpty(appName)) {
            result.put("app_name", appName);                                  // app 名称
        }
        result.put("androidid", Device.getAndroidID(mContext));               // android id
        result.put("mac", Device.getMac(mContext));                           // mac
        result.put("cell_id", String.valueOf(Device.getCellId(mContext)));    // 基站编号
        result.put("is_mobile_device", true);                                 // 是否是移动设备
        result.put("have_wifi",
                Device.getNetworkType(mContext) ==
                        Device.NetworkType.NETWORK_WIFI);                     // 是否是WIFI网络
        result.put("sr", Device.getScreenWidth(mContext) + "x" +
                Device.getScreenHeight(mContext));                            // 分辨率 宽x高
        result.put("lac", String.valueOf(Device.getLac(mContext)));           // 位置区域码
        int connectionType = 0;
        switch (Device.getNetworkType(mContext)) {
            case NETWORK_2G: {
                connectionType = 2;
                break;
            }
            case NETWORK_3G: {
                connectionType = 3;
                break;
            }
            case NETWORK_4G: {
                connectionType = 4;
                break;
            }
            case NETWORK_WIFI: {
                connectionType = 100;
                break;
            }
        }
        result.put("connection_type", String.valueOf(connectionType));        // 网络类型
        int operatorType = 0;
        switch (Device.getSimOperatorByMnc(mContext)) {
            case SIM_OPERATOR_CHINA_MOBILE: {
                operatorType = 1;
                break;
            }
            case SIM_OPERATOR_CHINA_UNICOM: {
                operatorType = 3;
                break;
            }
            case SIM_OPERATOR_CHINA_TELCOM: {
                operatorType = 2;
                break;
            }
        }
        result.put("operator_type", String.valueOf(operatorType));      // 运营商
        result.put("have_bt", Device.hasBluetooth());                   // 是否有蓝牙
        result.put("phone_type", Device.getPhoneType(mContext));        // 手机类型
        result.put("have_gps", Device.hasGPS(mContext));                // 是否有GPS
        result.put("device_name", Device.getBuildManufacturer() +
                " " + Device.getBuildModel());                          // 硬件制造+版本
        result.put("model", Device.getBuildModel());                    // 手机机型
        result.put("manufacturer", Device.getBuildManufacturer());      // 手机厂商
        result.put("producer", Device.getBuildProduct());               // 手机制造商
        result.put("mccmnc", Device.getSimOperator(mContext));          // mcc mnc
        if (extras.containsKey(EXTRA_LON)) {
            result.put("lon", String.valueOf(extras.get(EXTRA_LON)));   // 经度
        }
        if (extras.containsKey(EXTRA_LAT)) {
            result.put("lat", String.valueOf(extras.get(EXTRA_LAT)));   // 纬度
        }
        result.put("imsi", Device.getIMSI(mContext));                   // imsi
        result.put("lang", Device.getLocalLanguage());                  // 当前系统语言
        result.put("have_gravity",
                Device.hasSensor(mContext, Sensor.TYPE_GRAVITY));       // 是否支持重力感应
        result.put("os_version", Device.getBuildRelease());             // 操作系统版本
        return result.toJSONString();
    }

    private AdResponse convertResponse(String adPosId,
                                       String adType,
                                       String adLocalAppId,
                                       String adLocalPositionId,
                                       String oriResponse) {
        AdResponse.Builder builder = new AdResponse.Builder();
        builder.adPosId(adPosId).adName(SdkName.MIX_ADX).adType(adType)
                .adLocalAppId(adLocalAppId).adLocalPositionAd(adLocalPositionId);

        JSONObject resJson = null;
        try {
            resJson = JSONObject.parseObject(oriResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject adJson = null;
        if (resJson != null) {
            JSONArray adsJson = resJson.getJSONArray("ads");
            if (adsJson != null && adsJson.size() > 0) {
                adJson = adsJson.getJSONObject(0);
            }
        }

        if (adJson != null) {
            JSONArray creativesJson = adJson.getJSONArray("creatives");
            if (creativesJson != null && creativesJson.size() > 0) {
                int size = creativesJson.size();
                AdInfo adInfo = null;
                for (int i = 0; i < size; i++) {
                    JSONObject creativeJson = creativesJson.getJSONObject(i);
                    if (creativeJson == null) {
                        continue;
                    }
                    JSONObject metaInfoJson = creativeJson.getJSONObject("metaInfo");
                    if (metaInfoJson == null) {
                        continue;
                    }

                    adInfo = new AdInfo();
                    adInfo.generateUUID();
                    adInfo.setCanCache(true);
                    adInfo.setAdName(SdkName.MIX_ADX);
                    adInfo.setAdPosId(adPosId);
                    adInfo.setAdType(adType);
                    adInfo.setAdLocalAppId(adLocalAppId);
                    adInfo.setAdLocalPosId(adLocalPositionId);

                    int contentType = AdInfo.ContentType.PICTURE;
                    String creativeType = metaInfoJson.getString("creativeType");
                    switch (creativeType) {
                        case "TEXT": {
                            contentType = AdInfo.ContentType.TEXT;
                            break;
                        }
                        case "TEXT_ICON": {
                            contentType = AdInfo.ContentType.TEXT;
                            break;
                        }
                        case "IMAGE": {
                            contentType = AdInfo.ContentType.PICTURE;
                            break;
                        }
                        case "VIDEO": {
                            contentType = AdInfo.ContentType.VIDEO;
                            break;
                        }
                    }
                    adInfo.setContentType(contentType);

                    int actionType = AdInfo.ActionType.BROWSER;
                    String interactionType = metaInfoJson.getString("interactionType");
                    if ("DOWNLOAD".equalsIgnoreCase(interactionType)) {
                        actionType = AdInfo.ActionType.APP_DOWNLOAD;
                    }
                    adInfo.setActionType(actionType);
                    String ldpUrl = metaInfoJson.getString("ldp");
                    adInfo.setExtra(EXTRA_EVENT_CLICK_LDP, ldpUrl);

                    adInfo.setImgUrl(metaInfoJson.getString("imageUrl"));
                    adInfo.setVideoUrl(metaInfoJson.getString("videoUrl"));
                    adInfo.setTitle(metaInfoJson.getString("title"));
                    adInfo.setDesc(metaInfoJson.getString("description"));
                    JSONArray iconUrlsJson = metaInfoJson.getJSONArray("iconUrls");
                    if (iconUrlsJson != null && iconUrlsJson.size() > 0) {
                        adInfo.setAppIconUrl(iconUrlsJson.getString(0));
                    }
                    adInfo.setAppName(metaInfoJson.getString("brandName"));
                    adInfo.setAppPackageName(metaInfoJson.getString("appPackage"));

                    JSONArray trackingEventsJson = creativeJson.getJSONArray("trackingEvents");
                    if (trackingEventsJson != null) {
                        for (int trackingIndex = 0; trackingIndex < trackingEventsJson.size(); trackingIndex++) {
                            JSONObject eventJson = trackingEventsJson.getJSONObject(trackingIndex);
                            String event = eventJson.getString("event");
                            ArrayList<String> urls = new ArrayList<>();
                            JSONArray urlsJson = eventJson.getJSONArray("urls");
                            for (int urlIndex = 0; urlIndex < urlsJson.size(); urlIndex++) {
                                urls.add(urlsJson.getString(urlIndex));
                            }
                            if (!TextUtils.isEmpty(event) && urls.size() > 0) {
                                switch (event) {
                                    case "VIEW": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_VIEW, urls);
                                        break;
                                    }
                                    case "PLAY_END": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_PLAY_END, urls);
                                        break;
                                    }
                                    case "CLICK": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_CLICK, urls);
                                        break;
                                    }
                                    case "CLOSE": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_CLOSE, urls);
                                        break;
                                    }
                                    case "PLAY": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_PLAY, urls);
                                        break;
                                    }
                                    case "FULL_SCREEN": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_FULL_SCREEN, urls);
                                        break;
                                    }
                                    case "CARD_CLICK": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_CARD_CLICK, urls);
                                        break;
                                    }
                                    case "VIDEO_CLOSE": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_VIDEO_CLOSE, urls);
                                        break;
                                    }
                                    case "APP_DOWNLOAD": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_APP_DOWNLOAD, urls);
                                        break;
                                    }
                                    case "APP_START_DOWNLOAD": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_APP_START_DOWNLOAD, urls);
                                        break;
                                    }
                                    case "APP_INSTALL": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_APP_INSTALL, urls);
                                        break;
                                    }
                                    case "APP_ACTIVE": {
                                        adInfo.setExtra(EXTRA_EVENT_TRACK_URL_APP_ACTIVE, urls);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    break;
                }

                if (adInfo != null) {
                    builder.isSucceed(true);
                    builder.adInfo(adInfo);
                }
            }
        }

        return builder.create();
    }

    @SuppressWarnings("unchecked")
    private void reportEvent(AdInfo adInfo, int adEvent) {
        ArrayList<String> urls = null;
        switch (adEvent) {
            case AdEvent.EVENT_VIEW: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_VIEW);
                break;
            }
            case AdEvent.EVENT_CLICK: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_CLICK);
                break;
            }
            case AdEvent.EVENT_CLOSE: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_CLOSE);
                break;
            }
            case AdEvent.EVENT_APP_START_DOWNLOAD: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_APP_START_DOWNLOAD);
                break;
            }
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_APP_DOWNLOAD);
                break;
            }
            case AdEvent.EVENT_APP_INSTALL: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_APP_INSTALL);
                break;
            }
            case AdEvent.EVENT_APP_ACTIVE: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_APP_ACTIVE);
                break;
            }
            case AdEvent.EVENT_VIDEO_CARD_CLICK: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_CARD_CLICK);
                break;
            }
            case AdEvent.EVENT_VIDEO_START_PLAY: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_PLAY);
                break;
            }
            case AdEvent.EVENT_VIDEO_PLAY_COMPLETE: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_PLAY_END);
                break;
            }
            case AdEvent.EVENT_VIDEO_FULLSCREEN: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_FULL_SCREEN);
                break;
            }
            case AdEvent.EVENT_VIDEO_EXIT: {
                urls = (ArrayList) adInfo.getExtra(EXTRA_EVENT_TRACK_URL_VIDEO_CLOSE);
                break;
            }
        }

        if (urls == null || urls.size() == 0) {
            ReaperLog.i(TAG, "ignore event type " + adEvent);
            return;
        }

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            ReaperLog.i(TAG, "event report with url " + url);

            Request request = new Request.Builder()
                    .addHeader("content-type", "application/json;charset:utf-8")
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = mClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    ReaperLog.i(TAG, "event report succeed : " + adEvent);
                } else {
                    ReaperLog.e(TAG, "Event report failed : " + adEvent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(response);
            }
        }
    }

    // ----------------------------------------------------

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


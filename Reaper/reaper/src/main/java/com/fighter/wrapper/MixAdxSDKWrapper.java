

package com.fighter.wrapper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.common.Device;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.wrapper.download.OkHttpDownloader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MixAdxSDKWrapper implements ISDKWrapper, ICacheConvert {

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

    private static final String URL_REQUEST_AD_SCHEME = "http";
    private static final String URL_REQUEST_AD_HOST = "delivery.maihehd.com";
    private static final String URL_REQUEST_AD_PATH = "d/mmj/1.0";

    private static final String MIX_ADX_API_VER = "1.0";

    /**
     * 广告类型对应表
     */
    private static final Map<Integer, Integer> TYPE_REF_MAP = new ArrayMap<>();

    // 1:banner 2:插屏 4:开屏
    static {
        TYPE_REF_MAP.put(AdType.TYPE_BANNER, 1);
        TYPE_REF_MAP.put(AdType.TYPE_PLUG_IN, 2);
        TYPE_REF_MAP.put(AdType.TYPE_FULL_SCREEN, 4);
    }

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ThreadPoolUtils mThreadPoolUtils = AdThreadPool.INSTANCE.getThreadPoolUtils();
    private OkHttpDownloader mOkHttpDownloader = new OkHttpDownloader(mClient);
    private String mDownloadPath;

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return MIX_ADX_API_VER;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        mContext = appContext.getApplicationContext();
        mDownloadPath = mContext.getCacheDir().getAbsolutePath()
                + File.separator + "reaper_ad";
    }

    @Override
    public void requestAd(AdRequest adRequest, AdResponseListener adResponseListener) {
        if (adRequest == null) {
            throw new NullPointerException("AdRequest is null");
        }

        if (adResponseListener == null) {
            throw new NullPointerException("AdResponse is null");
        }

        mThreadPoolUtils.execute(new AdRequestRunnable(adRequest, adResponseListener));
    }

    @Override
    public void onEvent(int adEvent, AdResponse adResponse, Map<String, Object> eventParams) {

    }

    @Override
    public String convertToString(AdResponse adResponse) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appId", adResponse.getAppId());
            jsonObject.put("adPositionId", adResponse.getAdPositionId());
            jsonObject.put("oriResponse", JSON.parseObject(adResponse.getOriResponse()));

            ArrayMap<String, String> fileMap = new ArrayMap<>();

            for (AdInfo adInfo : adResponse.getAdInfos()) {
                String imgUrl = adInfo.getImgUrl();
                File f = adInfo.getImgFile();
                if (!TextUtils.isEmpty(imgUrl) &&
                        f != null) {
                    fileMap.put(imgUrl, f.getAbsolutePath());
                }
            }
            jsonObject.put("fileMap", fileMap);
            return jsonObject.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AdResponse convertFromString(String cachedResponse) {
        try {
            JSONObject jsonObject = JSON.parseObject(cachedResponse);
            String appId = jsonObject.getString("appId");
            String adPositionAd = jsonObject.getString("adPositionId");
            String oriResponse = jsonObject.getString("oriResponse");

            Map<String, String> fileMap = jsonObject.getObject("fileMap", Map.class);

            return convertResponse(appId, adPositionAd, oriResponse, fileMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------

    private AdResponse requestAdSync(AdRequest adRequest) {
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
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    adResponse = convertResponse(
                            adRequest.getAppId(), adRequest.getAdPositionId(),
                            response.body().string());
                } else {
                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", response.code());
                    return new AdResponse.Builder().errMsg(errJson.toJSONString()).create();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adResponse == null ?
                new AdResponse.Builder().errMsg("Request has no response.").create() :
                adResponse;
    }

    private String checkParams(AdRequest adRequest) {
        // 是否有对应支持的广告类型
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return "Can not find match mix adx ad type with ad type " +
                    adRequest.getAdType();
        }
        if (EmptyUtils.isEmpty(adRequest.getAppId())) {
            return "MixAdx app id is null";
        }
        if (EmptyUtils.isEmpty(adRequest.getAdPositionId())) {
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
        RequestBody body = generatePostData(paramsMap);
        return body;
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
        params.put("pos", adRequest.getAdPositionId());                              // 广告位ID
        params.put("posw", adRequest.getAdPositionId());                             // 广告位宽
        params.put("posh", adRequest.getAdPositionId());                             // 广告位高
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
        result.put("app_id", adRequest.getAppId());                           // app id
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

    private AdResponse convertResponse(String appId,
                                       String adPositionId,
                                       String oriResponse) {
        return convertResponse(appId, adPositionId, oriResponse, null);
    }

    private AdResponse convertResponse(String appId,
                                       String adPositionId,
                                       String oriResponse,
                                       Map<String, String> fileMap) {
        AdResponse.Builder builder = new AdResponse.Builder();
        builder.adFrom(AdFrom.FROM_MIX_ADX).canCache(true)
                .appId(appId).adPositionAd(adPositionId);

        builder.oriResponse(oriResponse);
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
                List<AdInfo> adInfos = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    JSONObject creativeJson = creativesJson.getJSONObject(i);
                    if (creativeJson == null) {
                        continue;
                    }
                    JSONObject metaInfoJson = creativeJson.getJSONObject("metaInfo");
                    if (metaInfoJson == null) {
                        continue;
                    }

                    AdInfo adInfo = new AdInfo();

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
                    if ("DOWNLOAD".equals(interactionType)) {
                        actionType = AdInfo.ActionType.APP_DOWNLOAD;
                    }
                    adInfo.setActionType(actionType);

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

                    String imgUrl = adInfo.getImgUrl();
                    if (!TextUtils.isEmpty(imgUrl)) {
                        File imgFile = null;
                        if (fileMap != null && fileMap.containsKey(imgUrl)) {
                            String filePath = fileMap.get(imgUrl);
                            if (!TextUtils.isEmpty(filePath)) {
                                File f = new File(filePath);
                                if (f.exists()) {
                                    imgFile = f;
                                }
                            }
                        }
                        if (imgFile == null) {
                            imgFile = mOkHttpDownloader.downloadSync(
                                    new Request.Builder().url(adInfo.getImgUrl()).build(),
                                    mDownloadPath,
                                    UUID.randomUUID().toString(),
                                    true
                            );
                        }
                        if (imgFile == null || !imgFile.exists()) {
                            continue;
                        }
                        adInfo.setImgFile(imgFile);
                    }

                    adInfos.add(adInfo);
                }

                if (adInfos.size() > 0) {
                    builder.isSucceed(true);
                    builder.adInfos(adInfos);
                }
            }
        }

        return builder.create();
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


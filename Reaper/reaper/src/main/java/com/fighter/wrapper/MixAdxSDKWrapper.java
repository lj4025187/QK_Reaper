package com.fighter.wrapper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONObject;
import com.fighter.common.Device;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.ThreadPoolUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MixAdxSDKWrapper implements ISDKWrapper {
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

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return MIX_ADX_API_VER;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        mContext = appContext.getApplicationContext();
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
                adResponse = convertResponse(response);
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
        Map<String, Object> extras = adRequest.getAdExtras();
        if (extras == null) {
            extras = new ArrayMap<>();
        }

        JSONObject json = new JSONObject();
        json.put("pos", adRequest.getAdPositionId());               // 广告位ID
        json.put("posw", String.valueOf(adRequest.getAdWidth()));   // 广告位宽
        json.put("posh", String.valueOf(adRequest.getAdHeight()));  // 广告位高
        json.put("postp", String.valueOf(
                TYPE_REF_MAP.get(adRequest.getAdType())));          // 广告位类型

        if (extras.containsKey(EXTRA_MED)) {
            json.put("med", extras.get(EXTRA_MED));                 // 媒体标识
        }
        if (extras.containsKey(EXTRA_TID)) {
            json.put("tid", extras.get(EXTRA_TID));                 // 投放编号
        }
        if (extras.containsKey(EXTRA_MAXC)) {
            json.put("maxc", extras.get(EXTRA_MAXC));               // 最大广告投放条数
        }
        if (extras.containsKey(EXTRA_MAXL)) {
            json.put("maxl", extras.get(EXTRA_MAXL));               // 最大广告时长 秒
        }
        if (adRequest.getAdKeyWords() != null
                && adRequest.getAdKeyWords().size() > 0) {
            json.put("kw", adRequest.getAdKeyWords().get(0));       // 关键词
        }

        JSONObject ipddJson = new JSONObject();
        ipddJson.put("device_type", "0");       // 设备类型 0-phone 1-pad 2-pc 3-tv
        ipddJson.put("os", "0");                // MMA标准，i系统标识参数
        // 0 代表 Android 1 代表 iOS 2 代表 Wphone
        // 3 代表 其他移动系统类型
        ipddJson.put("app_id", adRequest.getAppId());   // app id
        ipddJson.put("app_package", mContext.getPackageName()); // package name
        PackageInfo packageInfo =
                Device.getPackageInfo(mContext, mContext.getPackageName(),
                        PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ipddJson.put("app_version", packageInfo.versionName);   // app 完整版本名
        }
        ApplicationInfo appInfo =
                Device.getApplicationInfo(mContext, PackageManager.GET_ACTIVITIES);
        if (appInfo != null) {
            ipddJson.put("app_name", appInfo.name);     // app 名称
        }
        ipddJson.put("imei", Device.getM1(mContext));   // imei
        ipddJson.put("androidid", Device.getAndroidID(mContext));   // android id
        ipddJson.put("mac", Device.getMac(mContext));   // mac
        ipddJson.put("cell_id", String.valueOf(Device.getCellId(mContext)));    // 基站编号
        ipddJson.put("is_mobile_device", true);         // 是否是移动设备
        ipddJson.put("have_wifi",
                Device.getNetworkType(mContext) ==
                        Device.NetworkType.NETWORK_WIFI);    // 是否是WIFI网络
        ipddJson.put("sr", Device.getScreenWidth(mContext) + "x" +
                Device.getScreenHeight(mContext));      // 分辨率 宽x高
        ipddJson.put("lac", String.valueOf(Device.getLac(mContext)));   // 位置区域码
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
        ipddJson.put("connection_type", String.valueOf(connectionType));    // 网络类型
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
        ipddJson.put("operator_type", String.valueOf(operatorType));    // 运营商
        ipddJson.put("have_bt", Device.hasBluetooth());                 // 是否有蓝牙
        ipddJson.put("phone_type", Device.getPhoneType(mContext));      // 手机类型
        ipddJson.put("have_gps", Device.hasGPS(mContext));              // 是否有GPS
        ipddJson.put("device_name", Device.getBuildManufacturer() +
                " " + Device.getBuildModel());                          // 硬件制造+版本
        ipddJson.put("model", Device.getBuildModel());                  // 手机机型
        ipddJson.put("manufacturer", Device.getBuildManufacturer());    // 手机厂商
        ipddJson.put("producer", Device.getBuildProduct());             // 手机制造商
        ipddJson.put("mccmnc", Device.getSimOperator(mContext));        // mcc mnc
        if (extras.containsKey(EXTRA_LON)) {
            ipddJson.put("lon", String.valueOf(extras.get(EXTRA_LON))); // 经度
        }
        if (extras.containsKey(EXTRA_LAT)) {
            ipddJson.put("lat", String.valueOf(extras.get(EXTRA_LAT))); // 纬度
        }
        ipddJson.put("imsi", Device.getIMSI(mContext));                 // imsi
        ipddJson.put("lang", Device.getLocalLanguage());                // 当前系统语言
        ipddJson.put("have_gravity",
                Device.hasSensor(mContext, Sensor.TYPE_GRAVITY));       // 是否支持重力感应
        ipddJson.put("os_version", Device.getBuildRelease());           // 操作系统版本
        // 是否越狱 屏幕色彩深度 无法获取，不上传

        json.put("ipdd", ipddJson.toJSONString());

        MediaType type = MediaType.parse("application/json; charset=utf-8");
        return RequestBody.create(type, json.toJSONString());
    }

    private AdResponse convertResponse(Response response) {
        AdResponse.Builder builder = new AdResponse.Builder();
        JSONObject errJson = new JSONObject();
        errJson.put("httpResponseCode", response.code());
        if (response.isSuccessful()) {
            try {
                builder.oriResponse(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        builder.errMsg(errJson.toJSONString());
        return builder.create();
    }

    // ----------------------------------------------------

    private class AdRequestRunnable implements Runnable {
        private AdRequest mAdRequest;
        private WeakReference<AdResponseListener> mRef;

        AdRequestRunnable(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mRef = new WeakReference<>(adResponseListener);
        }

        @Override
        public void run() {
            AdResponse adResponse = requestAdSync(mAdRequest);
            AdResponseListener adResponseListener = mRef.get();
            if (adResponseListener != null) {
                adResponseListener.onAdResponse(adResponse);
            }
        }
    }
}

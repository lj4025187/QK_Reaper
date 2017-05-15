package com.fighter.wrapper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONObject;
import com.fighter.common.Device;
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
        mContext = appContext;
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
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return new AdResponse.Builder()
                    .errMsg("MixAdx can not deal ad type with " + adRequest.getAdType())
                    .create();
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

    private HttpUrl spliceRequestAdUrl() {
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_REQUEST_AD_SCHEME)
                .host(URL_REQUEST_AD_HOST)
                .addPathSegments(URL_REQUEST_AD_PATH);

        return builder.build();
    }

    private RequestBody spliceRequestAdBody(AdRequest adRequest) {
        JSONObject json = new JSONObject();
        json.put("pos", adRequest.getAdPositionId());
        json.put("posw", String.valueOf(adRequest.getAdWidth()));
        json.put("posh", String.valueOf(adRequest.getAdHeight()));
        json.put("postp", String.valueOf(TYPE_REF_MAP.get(adRequest.getAdType())));
        if (adRequest.getAdKeyWords() != null
                && adRequest.getAdKeyWords().size() > 0) {
            json.put("kw", adRequest.getAdKeyWords().get(0));
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
            ipddJson.put("app_version", packageInfo.versionName);
        }
        ApplicationInfo appInfo =
                Device.getApplicationInfo(mContext, PackageManager.GET_ACTIVITIES);
        if (appInfo != null) {
            ipddJson.put("app_name", appInfo.name);
        }
        ipddJson.put("imei", Device.getM1(mContext));
        ipddJson.put("androidid", Device.getAndroidID(mContext));
        ipddJson.put("mac", Device.getMac(mContext));
        ipddJson.put("cell_id", String.valueOf(Device.getCellId(mContext)));
        ipddJson.put("is_mobile_device", true);
        ipddJson.put("have_wifi",
                Device.getNetworkType(mContext) == Device.NetworkType.NETWORK_WIFI);
        ipddJson.put("sr", Device.getScreenWidth(mContext) + "x" +
                Device.getScreenHeight(mContext));
        ipddJson.put("lac", String.valueOf(Device.getLac(mContext)));
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
        ipddJson.put("connection_type", String.valueOf(connectionType));
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
        ipddJson.put("operator_type", String.valueOf(operatorType));
        ipddJson.put("device_name", Device.getBuildManufacturer() + " " + Device.getBuildModel());
        ipddJson.put("model", Device.getBuildModel());
        ipddJson.put("manufacturer", Device.getBuildManufacturer());
        ipddJson.put("producer", Device.getBuildProduct());
        ipddJson.put("mccmnc", Device.getSimOperator(mContext));
        ipddJson.put("imsi", Device.getIMSI(mContext));
        ipddJson.put("lang", Device.getLocalLanguage());
        ipddJson.put("os_version", Device.getBuildRelease());

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

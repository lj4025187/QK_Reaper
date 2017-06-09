package com.fighter.loader;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.utils.LoaderLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 广告SDK API接口类
 */

public class ReaperApi {

    private static final java.lang.String TAG = ReaperApi.class.getSimpleName();

    //Instance of ReaperApi in reaper.rr or Reaper.apk
    private Object mInstance;
    private ReaperVersionManager mVersionManager;
    private Map<String, Method> mMethodCache;

    // ----------------------------------------------------

    public ReaperApi(Object instance, String version) {
        LoaderLog.e(TAG, "mInstance : " + instance);
        mInstance = instance;
        mMethodCache = new ArrayMap<>();
    }

    public boolean isValid() {
        return mInstance != null;
    }

    public Object requestSplashAds(String name, int time) {
        try {
            Method requestSplashAdsMethod =
                    mInstance.getClass().getDeclaredMethod("requestSplashAds", String.class, int.class);
            if (requestSplashAdsMethod == null)
                return null;
            requestSplashAdsMethod.setAccessible(true);
            return requestSplashAdsMethod.invoke(mInstance, name, time);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------

    /**
     * 初始化广告SDK。
     *
     * @param appContext 应用上下文
     * @param appId      360OS广告平台申请的APP id
     * @param appKey     360OS广告平台申请的APP key
     * @param testMode 是否是测试模式，测试模式支持设置配置文件
     */
    public void init(Context appContext, String appId,
                     String appKey, boolean testMode) {
        Map<String, Object> params = new ArrayMap<>();

        if (appContext != null) {
            putParam(params, "appContext", appContext.getApplicationContext());
        }
        putParam(params, "appId", appId);
        putParam(params, "appKey", appKey);
        putParam(params, "testMode", testMode);
        invokeReaperApi("init", params);
    }

    /**
     * 设置测试模式使用的Json配置数据
     *
     * @param configJson 测试的目标测试数据
     */
    public void setTagetConfig(String configJson) {
        Map<String, Object> params = new ArrayMap<>();
        putParam(params, "config", configJson);
        invokeReaperApi("setTargetConfig", params);
    }

    /**
     * 获取某广告位的广告请求句柄{@link AdRequester}，可通过句柄请求广告
     *
     * @param adPositionId      360OS广告平台申请的广告位ID
     * @param adRequestCallback 广告请求回调
     * @param needHoldAd 是否在无网络或其他异常情况下返回保底广告
     * @return
     */
    @SuppressWarnings("unchecked")
    public AdRequester getAdRequester(String adPositionId,
                                      AdRequester.AdRequestCallback adRequestCallback, boolean needHoldAd) {
        Map<String, Object> params = new ArrayMap<>();
        putParam(params, "adPositionId", adPositionId);
        putParam(params, "adRequestCallback", new AdResponse(adRequestCallback));
        putParam(params, "needHoldAd", needHoldAd);

        invokeReaperApi("setNeedHoldAd", params);
        AdRequester adRequester = new AdRequester(this);
        adRequester.mParams = params;
        return adRequester;
    }

    /**
     * 获取设备wifi mac地址
     *
     * @param context  Context上下文
     * @return
     */
    public String getMacAddress(Context context) {
        Map<String, Object> params = new ArrayMap<>();
        putParam(params, "appContext", context);
        return (String)invokeReaperApi("getMacAddress", params);
    }

    // ----------------------------------------------------

    Object invokeReaperApi(String methodName, Map<String, Object> params) {
        try {
            Method method = mMethodCache.get(methodName);

            if (method == null) {
                method = mInstance.getClass().getDeclaredMethod(methodName, Map.class);
                if (method != null) {
                    mMethodCache.put(methodName, method);
                }
            }

            if (method != null) {
                method.setAccessible(true);
                return method.invoke(mInstance, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void putParam(Map<String, Object> params, String key, Object value) {
        if (params != null && !TextUtils.isEmpty(key) && value != null) {
            params.put(key, value);
        }
    }

    static void putParam(Map<String, Object> params, Map<String, Object> extraParams) {
        if (params != null && extraParams != null) {
            params.putAll(extraParams);
        }
    }

    // ----------------------------------------------------

    private class AdResponse {
        private AdRequester.AdRequestCallback mAdRequestCallback;

        public AdResponse(AdRequester.AdRequestCallback adRequestCallback) {
            mAdRequestCallback = adRequestCallback;
        }

        @SuppressWarnings("unchecked")
        public void onResponse(Map<String, Object> params) {
            if (mAdRequestCallback == null) {
                return;
            }

            Object objIsSucceed = params.get("isSucceed");
            boolean isSucceed = objIsSucceed != null && (boolean) objIsSucceed;
            String errMsg = (String) params.get("errMsg");
            if (!isSucceed) {
                mAdRequestCallback.onFailed(errMsg);
                return;
            }

            Map<String, Object> adInfoMap = (Map<String, Object>) params.get("adInfo");

            if (adInfoMap != null) {
                AdInfo adInfo = new AdInfo(ReaperApi.this);
                adInfo.mParams = adInfoMap;
                mAdRequestCallback.onSuccess(adInfo);
            } else {
                mAdRequestCallback.onFailed("Request succeed but contains no ad");
            }
        }
    }
}

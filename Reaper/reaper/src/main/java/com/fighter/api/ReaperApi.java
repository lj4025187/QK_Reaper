package com.fighter.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.cache.AdCacheManager;
import com.fighter.common.Device;
import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.config.ReaperConfigManager;
import com.fighter.download.ReaperEnv;
import com.fighter.reaper.R;
import com.fighter.wrapper.ISDKWrapper;
import com.qiku.proguard.annotations.NoProguard;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wxthon on 5/5/17.
 */

@NoProguard
public class ReaperApi {
    private static final String TAG = ReaperApi.class.getSimpleName();

    private static final String SALT = "salt_not_define";

    private static final String METHOD_ON_RESPONSE = "onResponse";

    private Context mContext;
    private String mAppId;
    private String mAppKey;
    private AtomicBoolean mIsInitSucceed = new AtomicBoolean(false);
    private AdCacheManager mAdCacheManager;
    private ThreadPoolUtils mThreadPoolUtils;
    private Map<String, ISDKWrapper> mSdkWrappers;
    private Map<String, Integer> mAdTypeMap;
    private Map<String, Method> mMethodMap;

    // ----------------------------------------------------

    //test start

    @NoProguard
    public String requestSplashAds(String name, int time) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int id = ReaperEnv.sContextProxy.getResources().getIdentifier("app_name", "string", "com.fighter.reaper");
        String string = ReaperEnv.sContextProxy.getString(id);
        String s = ReaperEnv.sContextProxy.getString(R.string.app_name);
        return "Requested an Ad for you, your params are : " + name + "; " + time + "; string : " + s;
    }

    //test end

    // ----------------------------------------------------

    @NoProguard
    public void init(Map<String, Object> params) {
        ReaperLog.i(TAG, "[init]");
        if (params == null) {
            ReaperLog.e(TAG, "[init] params is null");
            return;
        }
        if (mIsInitSucceed.get()) {
            return;
        }

        if (mContext == null) {
            ReaperLog.e(TAG, "[init] appContext is null");
            return;
        }

        if (TextUtils.isEmpty(mAppId)) {
            ReaperLog.e(TAG, "[init] appId is null");
            return;
        }

        if (TextUtils.isEmpty(mAppKey)) {
            ReaperLog.e(TAG, "[init] app key is null");
            return;
        }

        mAdCacheManager = AdCacheManager.getInstance();
        mAdCacheManager.init(params);

        mIsInitSucceed.set(true);
    }

    @NoProguard
    public void requestAd(Map<String, Object> params) {
        ReaperLog.i(TAG, "[requestAd] params: " + params);

        String adPositionId = (String) params.get("adPositionId");
        Object adRequestCallback = params.get("adRequestCallback");

        if (adRequestCallback == null) {
            ReaperLog.e(TAG, "[requestAd] AdRequestCallback is null");
            return;
        }

        if (!mIsInitSucceed.get()) {
            onRequestAdError(adRequestCallback,
                    "ReaperApi not initialized, please call init() first");
            return;
        }

        if (TextUtils.isEmpty(adPositionId)) {
            onRequestAdError(adRequestCallback,
                    "Can not request ad with empty position id");
            return;
        }
        mAdCacheManager.requestAdCache(adPositionId, adRequestCallback);
    }

    /**
     * 广告事件
     *
     * @param params
     */
    @NoProguard
    public void onEvent(Map<String, Object> params) {
        ReaperLog.i(TAG, "[onEvent] params: " + params);

    }

    // ----------------------------------------------------

    private void updateConfig() {
        Device.NetworkType networkType = Device.getNetworkType(mContext);
        if (networkType != Device.NetworkType.NETWORK_NO) {
            mThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                            mContext.getPackageName(), SALT, mAppKey, mAppId);
                }
            });
        }
    }

    private void onRequestAd(Object receiver, Map<String, Object> params) {
        if (receiver == null) {
            return;
        }
        Method methodOnResponse = mMethodMap.get(METHOD_ON_RESPONSE);
        if (methodOnResponse == null) {
            try {
                methodOnResponse = receiver.getClass().getDeclaredMethod(
                        METHOD_ON_RESPONSE, Map.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (methodOnResponse != null) {
                mMethodMap.put(METHOD_ON_RESPONSE, methodOnResponse);
            }
        }

        if (methodOnResponse == null) {
            return;
        }

        try {
            methodOnResponse.invoke(receiver, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRequestAdError(Object receiver, String errMsg) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("isSucceed", false);
        params.put("errMsg", errMsg);
        onRequestAd(receiver, params);
    }
}

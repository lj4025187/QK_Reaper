package com.fighter.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.ContextProxy;
import com.fighter.cache.AdCacheManager;
import com.fighter.common.Device;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;
import com.fighter.download.ReaperEnv;
import com.fighter.reaper.BumpVersion;
import com.fighter.reaper.R;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.fighter.wrapper.AdRequest;
import com.fighter.wrapper.AdResponse;
import com.fighter.wrapper.AdResponseListener;
import com.fighter.wrapper.AdType;
import com.fighter.wrapper.ISDKWrapper;
import com.fighter.wrapper.MixAdxSDKWrapper;
import com.fighter.wrapper.TencentSDKWrapper;
import com.qiku.proguard.annotations.NoProguard;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wxthon on 5/5/17.
 */

@NoProguard
public class ReaperApi {
    private static final String SALT = "salt_not_define";

    private static final String METHOD_ON_RESPONSE = "onResponse";

    private Context mContext;
    private String mAppId;
    private String mAppKey;
    private AtomicBoolean mIsInitSucceed;
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
//        if (params == null) {
//            return;
//        }
//
//        mContext = (Context) params.get("appContext");
//        mAppId = (String) params.get("appId");
//        mAppKey = (String) params.get("appKey");
//        if (mContext == null || TextUtils.isEmpty(mAppId) ||
//                TextUtils.isEmpty(mAppKey)) {
//            return;
//        }
//
        mIsInitSucceed = new AtomicBoolean(true);
//
//        mSdkWrappers = new ArrayMap<>();
//        ISDKWrapper akAdWrapper = new AKAdSDKWrapper();
//        ISDKWrapper tencentWrapper = new TencentSDKWrapper();
//        ISDKWrapper mixAdxWrapper = new MixAdxSDKWrapper();
//        akAdWrapper.init(new ContextProxy(mContext) /*mContext*/, null);
//        tencentWrapper.init(mContext, null);
//        mixAdxWrapper.init(mContext, null);
//        mSdkWrappers.put("juxiao", akAdWrapper);
//        mSdkWrappers.put("guangdiantong", tencentWrapper);
//        mSdkWrappers.put("baidu", mixAdxWrapper);
//
//        mAdTypeMap = new ArrayMap<>();
//        mAdTypeMap.put("banner", AdType.TYPE_BANNER);
//        mAdTypeMap.put("plugin", AdType.TYPE_PLUG_IN);
//        mAdTypeMap.put("app_wall", AdType.TYPE_APP_WALL);
//        mAdTypeMap.put("full_screen", AdType.TYPE_FULL_SCREEN);
//        mAdTypeMap.put("feed", AdType.TYPE_FEED);
//        mAdTypeMap.put("native", AdType.TYPE_NATIVE);
//        mAdTypeMap.put("native_video", AdType.TYPE_NATIVE_VIDEO);
//
//        mMethodMap = new ArrayMap<>();
//
//        mThreadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.CachedThread, 1);
//        updateConfig();
        mAdCacheManager = AdCacheManager.getInstance();
        mAdCacheManager.init(params);
    }

    @NoProguard
    public void requestAd(Map<String, Object> params) {
        String adPositionId = (String) params.get("adPositionId");
        Object adRequestCallback = params.get("adRequestCallback");

        if (adRequestCallback == null) {
            return;
        }

        if (!mIsInitSucceed.get()) {
            onRequestAdError(adRequestCallback,
                    "ReaperApi not inited, please call init() first");
            return;
        }

        if (TextUtils.isEmpty(adPositionId)) {
            onRequestAdError(adRequestCallback,
                    "Can not request ad with empty position id");
            return;
        }
        mAdCacheManager.requestAdCache(adPositionId, adRequestCallback);
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

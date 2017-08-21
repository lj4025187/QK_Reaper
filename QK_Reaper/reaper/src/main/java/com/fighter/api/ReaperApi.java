package com.fighter.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fighter.ad.AdInfo;
import com.fighter.cache.AdCacheManager;
import com.fighter.common.Device;
import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;
import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfig;
import com.fighter.config.ReaperConfigFetcher;
import com.fighter.config.ReaperConfigHttpHelper;
import com.fighter.config.db.ReaperConfigDB;
import com.fighter.reaper.ReaperEnv;
import com.fighter.hook.ReaperActivityThreadHook;
import com.fighter.hook.ReaperGlobal;
import com.fighter.hook.ReaperHookProvider;
import com.fighter.reaper.R;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.qiku.proguard.annotations.KeepAll;
import com.qiku.proguard.annotations.NoProguard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wxthon on 5/5/17.
 */

@KeepAll
public class ReaperApi {
    private static final String TAG = "reaper.ReaperApi";

    private Context mContext;
    private String mAppId;
    private String mAppKey;
    private boolean isTestMode;
    private AtomicBoolean mIsInitSucceed = new AtomicBoolean(false);
    private AdCacheManager mAdCacheManager;

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
        if (mIsInitSucceed.get()) {
            return;
        }

        if (params == null) {
            ReaperLog.e(TAG, "[init] params is null");
            return;
        }

        mContext = (Context) params.get("appContext");

        mAppId = (String) params.get("appId");
        mAppKey = (String) params.get("appKey");
        isTestMode = (boolean) params.get("testMode");

//        ReaperGlobal.setApplication((Application) mContext);
        ReaperGlobal.setContext(mContext);
        String packageName = mContext.getPackageName();
        ReaperLog.i(TAG, "init in reaper " + packageName);

        if (mContext == null) {
            ReaperLog.e(TAG, "[init] app context is null");
            return;
        }
        if (TextUtils.isEmpty(mAppId)) {
            ReaperLog.e(TAG, "[init] app id is null");
            return;
        }
        try {
            Integer integer = Integer.valueOf(mAppId);
        } catch (NumberFormatException nfe) {
            ReaperLog.e(TAG, "[init] app id is Illegal");
            nfe.printStackTrace();
            return;
        }
        if (TextUtils.isEmpty(mAppKey)) {
            ReaperLog.e(TAG, "[init] app key is null");
            return;
        }
        ReaperActivityThreadHook.wrapInstrumentation();
        ReaperHookProvider.hookReaperProxyProvider(mContext);

        mAdCacheManager = AdCacheManager.getInstance();
        mAdCacheManager.init(mContext, mAppId, mAppKey);

        mIsInitSucceed.set(true);
    }

    @NoProguard
    public void setTargetConfig(Map<String, Object> params) {
        if (params == null)
            return;
        String config = (String) params.get("config");
        if (isTestMode) {
            ReaperConfigHttpHelper.recordLastSuccessTime(mContext);
            String key = ReaperConfig.TEST_SALT + ReaperConfig.TEST_APPKEY;
            IRC4 rc4 = RC4Factory.create(key);
            byte[] encrypt = rc4.encrypt(config.getBytes());
            List<ReaperAdvPos> reaperAdvPoses = ReaperConfigHttpHelper.parseResponseBody(mContext, encrypt, key);
            if (reaperAdvPoses != null) {
                ReaperConfigDB.getInstance(mContext).saveReaperAdvPos(reaperAdvPoses);
            }
        }
    }

    @NoProguard
    public void initConfigValue(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return;
        String logModeKey = "LOG_SWITCH";
        if (params.containsKey(logModeKey)) {
            Object logMode = params.get(logModeKey);
            if (logMode != null && logMode instanceof Boolean)
                ReaperLog.LOG_SWITCH = (boolean) logMode;
            Log.i("Reaper", "ReaperLog.LOG_SWITCH " + ReaperLog.LOG_SWITCH);
        }
        String serverModeKey = "SERVER_TEST";
        if (params.containsKey(serverModeKey)) {
            Object serverMode = params.get(serverModeKey);
            if (serverMode != null && serverMode instanceof Boolean)
                ReaperConfigFetcher.SERVER_TEST_MODE = (boolean) serverMode;
            Log.i("Reaper", "ReaperConfigFetcher.SERVER_TEST_MODE " + ReaperConfigFetcher.SERVER_TEST_MODE);
        }
        String akadModeKey = "AKAD_TEST";
        if (params.containsKey(akadModeKey)) {
            Object akadMode = params.get(akadModeKey);
            if (akadMode != null && akadMode instanceof Boolean)
                AKAdSDKWrapper.AKAD_TEST_MODE = (boolean) akadMode;
            Log.i("Reaper", "AKAdSDKWrapper.AKAD_TEST_MODE " + AKAdSDKWrapper.AKAD_TEST_MODE);
        }
    }

    @NoProguard
    public void requestAd(Map<String, Object> params) {
        ReaperLog.i(TAG, "[requestAd] params: " + params);

        String adPositionId = (String) params.get("adPositionId");
        Object adRequestCallback = params.get("adRequestCallback");
        int adCount = (int) params.get("adCount");

        if (adRequestCallback == null) {
            ReaperLog.e(TAG, "[requestAd] AdRequestCallback is null");
            return;
        }

        if (!mIsInitSucceed.get()) {
            while (adCount > 0) {
                mAdCacheManager.onRequestAdError(adRequestCallback,
                        "ReaperApi not initialized, please call init() first");
                adCount--;
            }
            return;
        }

        if (TextUtils.isEmpty(adPositionId)) {
            while (adCount > 0) {
                mAdCacheManager.onRequestAdError(adRequestCallback,
                        "Can not request ad with empty position id");
                adCount--;
            }
            return;
        }
        mAdCacheManager.requestAdCache(adCount, adPositionId, adRequestCallback);
    }

    @NoProguard
    public String getMacAddress(Map<String, Object> params) {
        Context context = (Context) params.get("appContext");
        return Device.getMacStable(context);
    }

    @NoProguard
    public void setNeedHoldAd(Map<String, Object> params) {
        boolean needHoldAd = false;
        if (params.containsKey("needHoldAd")) {
            needHoldAd = (boolean) params.get("needHoldAd");
        }
        if (mAdCacheManager != null) {
            mAdCacheManager.setNeedHoldAd(needHoldAd);
        }
    }

    /**
     * 广告事件
     *
     * @param params
     */
    @NoProguard
    public void onEvent(Map<String, Object> params) {
        ReaperLog.i(TAG, "[onEvent] params: " + params);

        if (!mIsInitSucceed.get()) {
            ReaperLog.e(TAG, "ReaperApi has not initialized");
            return;
        }

        if (params.containsKey("event")) {
            AdInfo adInfo = new AdInfo();
            adInfo.setExtras(params);

            int adEvent = (int) params.get("event");
            mAdCacheManager.onEvent(adEvent, adInfo);
        }
    }
}

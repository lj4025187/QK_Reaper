package com.fighter.api;

import android.content.Context;
import android.text.TextUtils;

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
import com.fighter.hook.ReaperActivityThreadHook;
import com.fighter.hook.ReaperGlobal;
import com.fighter.hook.ReaperHookProvider;
import com.fighter.reaper.R;
import com.fighter.reaper.ReaperEnv;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.fighter.wrapper.BullsEyeSDKWrapper;
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
    private boolean mIsTestMode;
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
        mIsTestMode = (boolean) params.get("testMode");

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

        if (!TextUtils.isDigitsOnly(mAppId)) {
            ReaperLog.e(TAG, "[init] app id is Illegal");
            return;
        }

        if (TextUtils.isEmpty(mAppKey)) {
            ReaperLog.e(TAG, "[init] app key is null");
            return;
        }
        ReaperActivityThreadHook.hookInstrumentation();
        ReaperHookProvider.hookReaperProxyProvider(mContext);

        mAdCacheManager = AdCacheManager.getInstance();
        mAdCacheManager.init(mContext, mAppId, mAppKey);

        mIsInitSucceed.set(true);
    }

    @NoProguard
    public void setTargetConfig(Map<String, Object> params) {
        if (params == null) {
            ReaperLog.e(TAG, "[setTargetConfig] params is null");
            return;
        }
        if (!mIsTestMode) {
            ReaperLog.i(TAG, "[setTargetConfig] is not test mode");
            return;
        }
        Object configObj = params.get("config");
        if (configObj == null || !(configObj instanceof String)) {
            ReaperLog.e(TAG, "[setTargetConfig] config is null or not String");
            return;
        }

        String config = (String) configObj;
        ReaperConfigHttpHelper.recordLastSuccessTime(mContext);
        String key = ReaperConfig.TEST_SALT + ReaperConfig.TEST_APPKEY;
        IRC4 rc4 = RC4Factory.create(key);
        byte[] encrypt = rc4.encrypt(config.getBytes());
        List<ReaperAdvPos> reaperAdvPoses =
                ReaperConfigHttpHelper.parseResponseBody(mContext, encrypt, key);
        if (reaperAdvPoses != null) {
            ReaperConfigDB.getInstance(mContext).saveReaperAdvPos(reaperAdvPoses);
        }
    }

    @NoProguard
    public void initConfigValue(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            ReaperLog.e(TAG, "[initConfigValue] params is null");
            return;
        }

        Object logMode = params.get("LOG_SWITCH");
        if (logMode != null && logMode instanceof Boolean) {
            ReaperLog.LOG_SWITCH = (boolean) logMode;
        }
        ReaperLog.i(TAG, "[initConfigValue] ReaperLog.LOG_SWITCH " + ReaperLog.LOG_SWITCH);

        Object serverMode = params.get("SERVER_TEST");
        if (serverMode != null && serverMode instanceof Boolean) {
            ReaperConfigFetcher.SERVER_TEST_MODE = (boolean) serverMode;
        }
        ReaperLog.i(TAG, "[initConfigValue] ReaperConfigFetcher.SERVER_TEST_MODE "
                + ReaperConfigFetcher.SERVER_TEST_MODE);

        Object akadMode = params.get("AKAD_TEST");
        if (akadMode != null && akadMode instanceof Boolean) {
            AKAdSDKWrapper.AKAD_TEST_MODE = (boolean) akadMode;
        }
        ReaperLog.i(TAG, "AKAdSDKWrapper.AKAD_TEST_MODE " + AKAdSDKWrapper.AKAD_TEST_MODE);

        Object bullEyeMode = params.get("BULL_EYE_TEST");
        if (bullEyeMode != null && bullEyeMode instanceof Boolean) {
            BullsEyeSDKWrapper.BETA_SERVER = (boolean) bullEyeMode;
        }
        ReaperLog.i(TAG, "BullsEyeSDKWrapper.BETA_SERVER " + BullsEyeSDKWrapper.BETA_SERVER);
    }

    @NoProguard
    public void requestAd(Map<String, Object> params) {
        ReaperLog.i(TAG, "[requestAd] params: " + params);
        if (params == null) {
            ReaperLog.i(TAG, "[requestAd] params is null");
            return;
        }

        Object adPositionIdObj = params.get("adPositionId");
        String adPositionId = (adPositionIdObj != null && adPositionIdObj instanceof String) ?
                (String)adPositionIdObj : null;

        Object adRequestCallback = params.get("adRequestCallback");

        Object adCountObj = params.get("adCount");
        int adCount = (adCountObj != null && adCountObj instanceof Integer) ? (int)adCountObj : 0;

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
        if (params == null) {
            ReaperLog.e(TAG, "[getMacAddress] params is null");
            return "";
        }
        Object contextObj = params.get("appContext");
        if (contextObj == null) {
            ReaperLog.e(TAG, "[getMacAddress] contextObj is null");
            return "";
        }
        Context context = (contextObj instanceof Context) ? (Context) contextObj : null;
        if (context == null) {
            ReaperLog.e(TAG, "[getMacAddress] context is null");
            return "";
        }
        return Device.getMacStable(context);
    }

    @NoProguard
    public void setNeedHoldAd(Map<String, Object> params) {
        if (params == null) {
            ReaperLog.e(TAG, "[setNeedHoldAd] params is null");
            return;
        }

        Object needHoldAdObj = params.get("needHoldAd");
        boolean needHoldAd = (needHoldAdObj != null && needHoldAdObj instanceof Boolean) ?
                (boolean) needHoldAdObj : false;

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

        if (params == null) {
            ReaperLog.i(TAG, "[onEvent] params is null ");
            return;
        }

        Object eventObj = params.get("event");
        if (eventObj != null && eventObj instanceof Integer) {
            AdInfo adInfo = new AdInfo();
            adInfo.setExtras(params);

            int adEvent = (int) eventObj;
            mAdCacheManager.onEvent(adEvent, adInfo);
        }
    }
}

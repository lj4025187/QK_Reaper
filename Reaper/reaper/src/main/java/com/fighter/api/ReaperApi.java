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
import com.fighter.reaper.BumpVersion;
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
        return "Requested an Ad for you, your params are : " + name + "; " + time;
    }

    //test end

    // ----------------------------------------------------

    @NoProguard
    public void init(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        mContext = (Context) params.get("appContext");
        mAppId = (String) params.get("appId");
        mAppKey = (String) params.get("appKey");
        if (mContext == null || TextUtils.isEmpty(mAppId) ||
                TextUtils.isEmpty(mAppKey)) {
            return;
        }

        mIsInitSucceed = new AtomicBoolean(true);

        mSdkWrappers = new ArrayMap<>();
        ISDKWrapper akAdWrapper = new AKAdSDKWrapper();
        ISDKWrapper tencentWrapper = new TencentSDKWrapper();
        ISDKWrapper mixAdxWrapper = new MixAdxSDKWrapper();
        akAdWrapper.init(new ContextProxy(mContext) /*mContext*/, null);
        tencentWrapper.init(mContext, null);
        mixAdxWrapper.init(mContext, null);
        mSdkWrappers.put("juxiao", akAdWrapper);
        mSdkWrappers.put("guangdiantong", tencentWrapper);
        mSdkWrappers.put("baidu", mixAdxWrapper);

        mAdTypeMap = new ArrayMap<>();
        mAdTypeMap.put("banner", AdType.TYPE_BANNER);
        mAdTypeMap.put("plugin", AdType.TYPE_PLUG_IN);
        mAdTypeMap.put("app_wall", AdType.TYPE_APP_WALL);
        mAdTypeMap.put("full_screen", AdType.TYPE_FULL_SCREEN);
        mAdTypeMap.put("feed", AdType.TYPE_FEED);
        mAdTypeMap.put("native", AdType.TYPE_NATIVE);
        mAdTypeMap.put("native_video", AdType.TYPE_NATIVE_VIDEO);

        mMethodMap = new ArrayMap<>();

        mThreadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.CachedThread, 1);
        mAdCacheManager = new AdCacheManager(mContext);
        updateConfig();
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

        mThreadPoolUtils.execute(new RequestAdRunner(adPositionId, adRequestCallback));
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

    // ----------------------------------------------------

    private class RequestAdRunner implements Runnable {
        private String mAdPosition;
        private Object mCallback;

        public RequestAdRunner(String adPosition, Object adRequestCallback) {
            mAdPosition = adPosition;
            mCallback = adRequestCallback;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // 首先查找缓存是否存在

            // 获取配置信息
            boolean fetchSucceed = true;
                    /*ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                            mContext.getPackageName(), SALT, mAppKey, mAppId);*/

            if (!fetchSucceed) {
                onRequestAdError(mCallback, "Can not fetch reaper config from server");
                return;
            }

            ReaperAdvPos reaperAdvPos =
                    ReaperConfigManager.getReaperAdvPos(mContext, mAdPosition);
            if (reaperAdvPos == null) {
                /*onRequestAdError(mCallback,
                        "Can not find config info with ad position id [" + mAdPosition + "]");
                return;*/
                Random random = new Random(System.currentTimeMillis());
                reaperAdvPos = new ReaperAdvPos();
                reaperAdvPos.pos_id = mAdPosition;
                reaperAdvPos.adv_type = "banner";
                ReaperAdSense tencentSense = new ReaperAdSense();
                tencentSense.ads_name = "guangdiantong";
                tencentSense.ads_appid = "1104241296";
                tencentSense.ads_posid = "5060504124524896";
                tencentSense.adv_size_type = "pixel";
                tencentSense.adv_real_size = "640x100";
                tencentSense.max_adv_num = "10";
                tencentSense.priority = String.valueOf(random.nextInt(10));
                ReaperAdSense baiduSense = new ReaperAdSense();
                baiduSense.ads_name = "baidu";
                baiduSense.ads_appid = "0";
                baiduSense.ads_posid = "128";
                baiduSense.adv_size_type = "pixel";
                baiduSense.adv_real_size = "600x300";
                baiduSense.max_adv_num = "10";
                baiduSense.priority = String.valueOf(random.nextInt(10));

                // reaperAdvPos.addAdSense(tencentSense);
                reaperAdvPos.addAdSense(baiduSense);
            }
            List<ReaperAdSense> reaperAdSenses = reaperAdvPos.getAdSenseList();
            if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
                onRequestAdError(mCallback,
                        "Config get 0 ad sense with ad position id [" + mAdPosition + "]");
                return;
            }
            Collections.sort(reaperAdSenses);

            // 依据配置信息获取广告
            mThreadPoolUtils.execute(new RequestSingleAdRunner(
                    reaperAdSenses,
                    reaperAdvPos.adv_type,
                    mCallback
            ));
        }
    }

    private class RequestSingleAdRunner implements Runnable {
        private Object mCallback;
        private String mAdvType;
        private List<ReaperAdSense> mReaperAdSenses;

        public RequestSingleAdRunner(List<ReaperAdSense> reaperAdSenses,
                                     String advType,
                                     Object adRequestCallback) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            mCallback = adRequestCallback;
        }

        @Override
        public void run() {
            Iterator<ReaperAdSense> it = mReaperAdSenses.iterator();
            ReaperAdSense reaperAdSense = it.next();
            it.remove();
            String adsName = reaperAdSense.ads_name;
            ISDKWrapper sdkWrapper = mSdkWrappers.get(adsName);
            if (sdkWrapper == null) {
                onRequestAdError(mCallback,
                        "Can not find " + adsName + "'s sdk implements, may need " +
                                "upgrade reaper jar, current version " + BumpVersion.value());
                return;
            }

            int adType = 0;
            if (mAdTypeMap.containsKey(mAdvType)) {
                adType = mAdTypeMap.get(mAdvType);
            } else {
                onRequestAdError(mCallback, "Can not find match ad type with type name " +
                        mAdvType);
                return;
            }

            AdRequest.Builder builder = new AdRequest.Builder()
                    .appId(reaperAdSense.ads_appid)
                    .adPositionId(reaperAdSense.ads_posid)
                    .adType(adType)
                    .adCount(1);

            if ("pixel".equalsIgnoreCase(reaperAdSense.adv_size_type)) {
                String realSize = reaperAdSense.adv_real_size;
                String[] size = realSize.split("x");
                int width = 0;
                int height = 0;

                if (size.length == 2) {
                    try {
                        width = Integer.valueOf(size[0]);
                        height = Integer.valueOf(size[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (width > 0 || height > 0) {
                    builder.adWidth(width);
                    builder.adHeight(height);
                }
            } else if ("ratio".equalsIgnoreCase(reaperAdSense.adv_size_type)) {
                onRequestAdError(mCallback, "[TEST] Not support adv size type " +
                        reaperAdSense.adv_size_type);
                return;
            } else {
                onRequestAdError(mCallback, "Not support adv size type " +
                        reaperAdSense.adv_size_type);
                return;
            }

            sdkWrapper.requestAd(builder.create(),
                    new AdResponseCallback(mReaperAdSenses, mAdvType, mCallback));
        }
    }

    private class AdResponseCallback implements AdResponseListener {

        private List<ReaperAdSense> mReaperAdSenses;
        private String mAdvType;
        private Object mCallback;

        public AdResponseCallback(List<ReaperAdSense> reaperAdSenses,
                                  String advType,
                                  Object adRequestCallback) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            mCallback = adRequestCallback;
        }

        @Override
        public void onAdResponse(AdResponse adResponse) {
            if (adResponse == null) {
                if (!nextRequest()) {
                    onRequestAdError(mCallback, "Response with no result");
                }
                return;
            }

            if (adResponse.isSucceed() || !nextRequest()) {
                onRequestAd(mCallback, adResponse.getAdAllParams());
            }
        }

        private boolean nextRequest() {
            if (mReaperAdSenses != null &&
                    mReaperAdSenses.size() > 0) {
                mThreadPoolUtils.execute(
                        new RequestSingleAdRunner(mReaperAdSenses, mAdvType, mCallback));
                return true;
            } else {
                return false;
            }
        }
    }
}

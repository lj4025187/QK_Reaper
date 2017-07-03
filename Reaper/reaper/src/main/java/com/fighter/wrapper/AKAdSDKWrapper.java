package com.fighter.wrapper;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LruCache;
import android.view.View;

import com.ak.android.engine.download.ApkListener;
import com.ak.android.engine.nav.NativeAd;
import com.ak.android.engine.nav.NativeAdLoaderListener;
import com.ak.android.engine.navbase.AdSpace;
import com.ak.android.engine.navbase.NativeAdLoader;
import com.ak.android.engine.navvideo.NativeVideoAd;
import com.ak.android.engine.navvideo.NativeVideoAdLoaderListener;
import com.ak.android.shell.AKAD;
import com.alibaba.fastjson.JSONObject;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.download.ReaperEnv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 360 聚效广告SDK Wrapper
 */
public class AKAdSDKWrapper extends ISDKWrapper {
    private static final String TAG = "AKAdSDKWrapper";

    public static final String PARAMS_KEY_VIEW = "view";
    public static final String PARAMS_KEY_ACTIVITY = "activity";

    // ----------------------------------------------------

    private static final String EVENT_POSITION = "position";

    // ----------------------------------------------------

    private static final String AK_AD_API_VER = "3.8.3031_0302";

    private static final String EXTRA_EVENT_NATIVE_AD = "akad_event_native_ad";
    private static final String EXTRA_EVENT_NATIVE_VIDEO_AD = "akad_event_native_video_ad";

    private static final Map<Integer, Integer> VIDEO_STATUS_MAP = new ArrayMap<>();

    static {
        VIDEO_STATUS_MAP.put(AdEvent.EVENT_VIDEO_START_PLAY, NativeVideoAd.VIDEO_START);
        VIDEO_STATUS_MAP.put(AdEvent.EVENT_VIDEO_PAUSE, NativeVideoAd.VIDEO_PAUSE);
        VIDEO_STATUS_MAP.put(AdEvent.EVENT_VIDEO_CONTINUE, NativeVideoAd.VIDEO_CONTINUE);
        VIDEO_STATUS_MAP.put(AdEvent.EVENT_VIDEO_EXIT, NativeVideoAd.VIDEO_EXIT);
        VIDEO_STATUS_MAP.put(AdEvent.EVENT_VIDEO_PLAY_COMPLETE, NativeVideoAd.VIDEO_COMPLETE);
    }

    // ----------------------------------------------------

    private Context mContext;
    private ThreadPoolUtils mThreadPoolUtils = AdThreadPool.INSTANCE.getThreadPoolUtils();
    private DownloadCallback mDownloadCallback;
    private LruCache<String, AdInfo> mDownloadMap;

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return AK_AD_API_VER;
    }

    @Override
    public String getSdkName() {
        return SdkName.AKAD;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        ReaperLog.i(TAG, "[init]");
        mContext = ReaperEnv.sContextProxy;
        mDownloadMap = new LruCache<>(200);
        //if second param set true should see "AKAD" tag
        AKAD.initSdk(mContext, false, false);
        AKAD.setApkListener(mContext, new ApkDownloadListener());
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return false;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        ReaperLog.i(TAG, "[requestAdAsync]");
        new AKAdRequester(adRequest, adResponseListener).request();
    }

    @Override
    public boolean isOpenWebOwn() {
        return true;
    }

    @Override
    public boolean isDownloadOwn() {
        return true;
    }

    @Override
    public void setDownloadCallback(DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        ReaperLog.i(TAG, "[onEvent] " + adEvent +
                "\nAdInfo " + adInfo);
        Map<String, Object> eventParams = adInfo.getAdAllParams();
        NativeAd nativeAd = null;
        if(adInfo.getContentType() == AdInfo.ContentType.VIDEO) {
            Object videoAd = adInfo.getExtra(EXTRA_EVENT_NATIVE_VIDEO_AD);
            if(videoAd instanceof NativeVideoAd) nativeAd = (NativeAd) videoAd;
        } else {
            nativeAd = (NativeAd) adInfo.getExtra(EXTRA_EVENT_NATIVE_AD);
        }
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_SUCCESS: {
                if (eventParams != null && eventParams.containsKey(PARAMS_KEY_VIEW)) {
                    eventAdShow(nativeAd, (View) eventParams.get(PARAMS_KEY_VIEW));
                }
                break;
            }
            case AdEvent.EVENT_CLICK: {
                if (eventParams != null && eventParams.containsKey(PARAMS_KEY_ACTIVITY) &&
                        eventParams.containsKey(PARAMS_KEY_ACTIVITY)) {
                    eventAdClick(adInfo,
                            nativeAd,
                            (Activity) eventParams.get(PARAMS_KEY_ACTIVITY),
                            (View) eventParams.get(PARAMS_KEY_VIEW));
                }
                break;
            }
            case AdEvent.EVENT_CLOSE: {
                eventAdClose(nativeAd);
            }
            case AdEvent.EVENT_VIDEO_START_PLAY:
            case AdEvent.EVENT_VIDEO_PAUSE:
            case AdEvent.EVENT_VIDEO_CONTINUE:
            case AdEvent.EVENT_VIDEO_EXIT:
            case AdEvent.EVENT_VIDEO_PLAY_COMPLETE: {
                if (nativeAd instanceof NativeVideoAd) {
                    int position = 0;
                    if (eventParams.containsKey(EVENT_POSITION)) {
                        position = (int) eventParams.get(EVENT_POSITION);
                    }

                    int status = VIDEO_STATUS_MAP.get(adEvent);
                    ReaperLog.i("ForTest", " video status comment START = 81,PAUSE = 82,CONTINUE = 83,EXIT = 84,COMPLETE = 85");
                    ReaperLog.i("ForTest", "srcName: " + adInfo.getExtra("adName") + " posId: " + adInfo.getAdPosId() + " localPosId: " + adInfo.getExtra("adLocalPosId")
                            + " uuid: " + adInfo.getUUID().substring(30) + " status " + status );
                    eventAdVideoChanged((NativeVideoAd) nativeAd,
                            status,
                            position);
                }
                break;
            }
        }
    }

    // ----------------------------------------------------

    private void eventAdShow(NativeAd nativeAd, View v) {
        if (nativeAd == null || v == null) {
            return;
        }
        nativeAd.onAdShowed(v);
    }

    private void eventAdClick(AdInfo adInfo, NativeAd nativeAd, Activity activity, View v) {
        if (nativeAd == null || activity == null || v == null) {
            return;
        }
        if (nativeAd.getActionType() == 1) {
            // 下载类广告
            org.json.JSONObject json = nativeAd.getAPPInfo();
            String key = json.optString("key");
            if (!TextUtils.isEmpty(key)) {
                mDownloadMap.put(key, adInfo);
            }
        }
        nativeAd.onAdClick(activity, v);
    }

    private void eventAdClose(NativeAd nativeAd) {
        if (nativeAd == null) {
            return;
        }
        nativeAd.onAdClosed();
    }

    private void eventAdVideoChanged(NativeVideoAd nativeVideoAd, int status, int currentPosition) {
        if (nativeVideoAd == null) {
            return;
        }
        nativeVideoAd.onVideoChanged(status, currentPosition);
    }

    private void notifyDownloadCallback(String key, int adEvent) {
        AdInfo adInfo = mDownloadMap.get(key);
        if (mDownloadCallback != null && adInfo != null) {
            mDownloadCallback.onDownloadEvent(adInfo, adEvent);
        }
    }

    // ----------------------------------------------------

    private class AKAdRequester {
        private AdRequest mAdRequest;
        private AdResponseListener mAdResponseListener;

        AKAdRequester(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAdResponseListener = adResponseListener;
        }

        public void request() {
            switch (mAdRequest.getAdType()) {
                case AdType.TYPE_NATIVE: {
                    requestNativeAd();
                    break;
                }
                case AdType.TYPE_VIDEO: {
                    requestNativeVideoAd();
                    break;
                }
                default:
                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", 1);
                    errJson.put("akAdErrCode", 1);
                    errJson.put("akAdErrMsg", "the AKAD source not suppoort ad type [" + mAdRequest.getAdType() + "]");
                    AdResponse adResponse = new AdResponse.Builder()
                            .errMsg(errJson.toString())
                            .create();

                    if (mAdResponseListener != null) {
                        mAdResponseListener.onAdResponse(adResponse);
                    }
            }
        }

        private void requestNativeAd() {
            AdSpace adSpace = new AdSpace(mAdRequest.getAdLocalPositionId());
            adSpace.setAdNum(mAdRequest.getAdCount());
            if (mAdRequest.getAdWidth() > 0 &&
                    mAdRequest.getAdHeight() > 0) {
                adSpace.addAdSize(mAdRequest.getAdWidth(), mAdRequest.getAdHeight());
            }

            NativeAdLoader adLoader =
                    AKAD.getNativeAdLoader(mContext, new NativeAdLoaderListener() {
                        @Override
                        public void onAdLoadSuccess(ArrayList<NativeAd> ads) {
                            if (mAdResponseListener != null) {
                                mThreadPoolUtils.execute(new AKAdNativeAdRunnable(
                                        mAdRequest,
                                        ads,
                                        mAdResponseListener
                                ));
                            }
                        }

                        @Override
                        public void onAdLoadFailed(int errCode, String errMsg) {
                            JSONObject errJson = new JSONObject();
                            errJson.put("httpResponseCode", 0);
                            errJson.put("akAdErrCode", errCode);
                            errJson.put("akAdErrMsg", errMsg);

                            AdResponse adResponse = new AdResponse.Builder()
                                    .errMsg(errJson.toJSONString())
                                    .create();

                            if (mAdResponseListener == null) {
                                return;
                            }

                            mAdResponseListener.onAdResponse(adResponse);
                        }
                    }, adSpace);

            if (adLoader != null) {
                List<String> keyWords = mAdRequest.getAdKeyWords();
                if (keyWords != null && keyWords.size() > 0) {
                    HashSet<String> akAdKeyWords = new HashSet<>();
                    akAdKeyWords.addAll(keyWords);
                    adLoader.setKeyWords(akAdKeyWords);
                }
                adLoader.loadAds();
            } else {
                JSONObject errJson = new JSONObject();
                errJson.put("httpResponseCode", 0);
                errJson.put("akAdErrCode", 0);
                errJson.put("akAdErrMsg", "can not create AKAD loader");
                AdResponse adResponse = new AdResponse.Builder()
                        .errMsg(errJson.toString())
                        .create();

                if (mAdResponseListener != null) {
                    mAdResponseListener.onAdResponse(adResponse);
                }
            }
        }

        private void requestNativeVideoAd() {
            AdSpace adSpace = new AdSpace(mAdRequest.getAdLocalPositionId());
            adSpace.setAdNum(mAdRequest.getAdCount());
            if (mAdRequest.getAdWidth() > 0 &&
                    mAdRequest.getAdHeight() > 0) {
                adSpace.addAdSize(mAdRequest.getAdWidth(), mAdRequest.getAdHeight());
            }

            NativeAdLoader adLoader =
                    AKAD.getNativeVideoAdLoader(mContext, new NativeVideoAdLoaderListener() {
                        @Override
                        public void onAdLoadSuccess(ArrayList<NativeVideoAd> ads) {
                            if (mAdResponseListener != null) {
                                mThreadPoolUtils.execute(new AKAdNativeVideoAdRunnable(
                                        mAdRequest,
                                        ads,
                                        mAdResponseListener
                                ));
                            }
                        }

                        @Override
                        public void onAdLoadFailed(int errCode, String errMsg) {
                            JSONObject errJson = new JSONObject();
                            errJson.put("httpResponseCode", 0);
                            errJson.put("akAdErrCode", errCode);
                            errJson.put("akAdErrMsg", errMsg);

                            AdResponse adResponse = new AdResponse.Builder()
                                    .errMsg(errJson.toJSONString())
                                    .create();

                            if (mAdResponseListener == null) {
                                return;
                            }

                            mAdResponseListener.onAdResponse(adResponse);
                        }
                    }, adSpace);

            if (adLoader != null) {
                List<String> keyWords = mAdRequest.getAdKeyWords();
                if (keyWords != null && keyWords.size() > 0) {
                    HashSet<String> akAdKeyWords = new HashSet<>();
                    akAdKeyWords.addAll(keyWords);
                    adLoader.setKeyWords(akAdKeyWords);
                }
                adLoader.loadAds();
            } else {
                JSONObject errJson = new JSONObject();
                errJson.put("httpResponseCode", 0);
                errJson.put("akAdErrCode", 0);
                errJson.put("akAdErrMsg", "can not create AKAD loader");
                AdResponse adResponse = new AdResponse.Builder()
                        .errMsg(errJson.toString())
                        .create();

                if (mAdResponseListener != null) {
                    mAdResponseListener.onAdResponse(adResponse);
                }
            }
        }
    }

    private class AKAdNativeAdRunnable implements Runnable {
        private AdRequest mAdRequest;
        private List<NativeAd> mAds;
        private AdResponseListener mAdResponseListener;

        public AKAdNativeAdRunnable(AdRequest adRequest,
                                    List<NativeAd> ads,
                                    AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAds = ads;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            JSONObject errJson = new JSONObject();
            errJson.put("httpResponseCode", 200);

            AdResponse.Builder builder = new AdResponse.Builder();
            builder
                    .adPosId(mAdRequest.getAdPosId())
                    .adName(SdkName.AKAD)
                    .adType(mAdRequest.getAdType())
                    .adLocalAppId(mAdRequest.getAdLocalAppId())
                    .adLocalPositionAd(mAdRequest.getAdLocalPositionId());
            if (mAds != null && mAds.size() > 0) {
                AdInfo adInfo = null;
                for (NativeAd ad : mAds) {
                    JSONObject akAdJson = JSONObject.parseObject(ad.getContent().toString());
                    if (akAdJson == null) {
                        continue;
                    }
                    String akAdTitle = akAdJson.getString("title");                 //标题
                    String akAdExtText = akAdJson.getString("ext_text");            //扩展字段，多为副标题
                    String akAdDesc = akAdJson.getString("desc");                   //描述
                    String akAdBtnText = akAdJson.getString("btntext");             //按钮文案
                    String akAdImgUrl = akAdJson.getString("contentimg");           //大图URL
                    String akAdLogo = akAdJson.getString("logo");                   //图标URL
                    String akAdWidth = String.valueOf(akAdJson.getIntValue("w"));   //素材宽
                    String akAdHeight = String.valueOf(akAdJson.getIntValue("h"));  //素材高

                    adInfo = new AdInfo();
                    adInfo.generateUUID();
                    adInfo.setExpireTime(mAdRequest.getExpireTime());
                    adInfo.setCanCache(false);
                    adInfo.setAdName(SdkName.AKAD);
                    adInfo.setAdPosId(mAdRequest.getAdPosId());
                    adInfo.setAdType(mAdRequest.getAdType());
                    adInfo.setAdLocalAppId(mAdRequest.getAdLocalAppId());
                    adInfo.setAdLocalPosId(mAdRequest.getAdLocalPositionId());
                    if (TextUtils.isEmpty(akAdImgUrl)) {
                        adInfo.setContentType(AdInfo.ContentType.TEXT);
                    } else {
                        if (TextUtils.isEmpty(akAdTitle) &&
                                TextUtils.isEmpty(akAdDesc)) {
                            adInfo.setContentType(AdInfo.ContentType.PICTURE);
                        } else {
                            adInfo.setContentType(AdInfo.ContentType.PICTURE_WITH_TEXT);
                        }
                    }
                    if (ad.getActionType() == 1) {
                        adInfo.setActionType(AdInfo.ActionType.APP_DOWNLOAD);

                        JSONObject akAppJson = JSONObject.parseObject(ad.getAPPInfo().toString());
                        if (akAppJson != null) {
                            String akAppPkgName = akAppJson.getString("app_pkg");
                            String akAppName = akAppJson.getString("app_name");
                            adInfo.setDownAppName(akAppName);
                            adInfo.setDownPkgName(akAppPkgName);
                            adInfo.setBrandName(akAppName);
                        }
                    } else {
                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    }
                    adInfo.setExtra(EXTRA_EVENT_NATIVE_AD, ad);

                    adInfo.setTitle(akAdTitle);
                    adInfo.setText(akAdExtText);
                    adInfo.setDesc(akAdDesc);
                    adInfo.setBtnText(akAdBtnText);
                    adInfo.setImgUrl(akAdImgUrl);
                    adInfo.setAppIconUrl(akAdLogo);
                    break;
                }
                if (adInfo != null) {
                    builder.isSucceed(true);
                    builder.adInfo(adInfo);
                } else {
                    errJson.put("akAdErrCode", 0);
                    errJson.put("akAdErrMsg", "no mAds");
                }
            } else {
                errJson.put("akAdErrCode", 0);
                errJson.put("akAdErrMsg", "no mAds");
            }
            builder.errMsg(errJson.toJSONString());

            if (mAdResponseListener == null) {
                return;
            }

            mAdResponseListener.onAdResponse(builder.create());
        }
    }

    private class AKAdNativeVideoAdRunnable implements Runnable {
        private AdRequest mAdRequest;
        private List<NativeVideoAd> mAds;
        private AdResponseListener mAdResponseListener;

        public AKAdNativeVideoAdRunnable(AdRequest adRequest,
                                         List<NativeVideoAd> ads,
                                         AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAds = ads;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            JSONObject errJson = new JSONObject();
            errJson.put("httpResponseCode", 200);

            AdResponse.Builder builder = new AdResponse.Builder();
            builder
                    .adPosId(mAdRequest.getAdPosId())
                    .adName(SdkName.AKAD)
                    .adType(mAdRequest.getAdType())
                    .adLocalAppId(mAdRequest.getAdLocalAppId())
                    .adLocalPositionAd(mAdRequest.getAdLocalPositionId());
            if (mAds != null && mAds.size() > 0) {
                AdInfo adInfo = null;
                for (NativeVideoAd ad : mAds) {
                    JSONObject akAdJson = JSONObject.parseObject(ad.getContent().toString());
                    if (akAdJson == null) {
                        continue;
                    }
                    String akAdTitle = akAdJson.getString("title");                 //标题
                    String akAdExtText = akAdJson.getString("ext_text");            //扩展字段，多为副标题
                    String akAdDesc = akAdJson.getString("desc");                   //描述
                    String akAdBtnText = akAdJson.getString("btntext");             //按钮文案
                    String akAdImgUrl = akAdJson.getString("contentimg");           //大图URL
                    String akAdIconUrl = akAdJson.getString("logo");                //图标URL
                    String akVideoUrl = akAdJson.getString("video");                //视频URL
                    String akAdDuration = akAdJson.getString("duration");           //视频时长
                    String akAdWidth = String.valueOf(akAdJson.getIntValue("w"));   //素材宽
                    String akAdHeight = String.valueOf(akAdJson.getIntValue("h"));  //素材高

                    adInfo = new AdInfo();
                    adInfo.generateUUID();
                    adInfo.setExpireTime(mAdRequest.getExpireTime());
                    adInfo.setCanCache(true);
                    adInfo.setAdName(SdkName.AKAD);
                    adInfo.setAdPosId(mAdRequest.getAdPosId());
                    adInfo.setAdType(mAdRequest.getAdType());
                    adInfo.setAdLocalAppId(mAdRequest.getAdLocalAppId());
                    adInfo.setAdLocalPosId(mAdRequest.getAdLocalPositionId());
                    if (ad.hasVideo()) {
                        adInfo.setContentType(AdInfo.ContentType.VIDEO);
                    } else if (!TextUtils.isEmpty(akAdImgUrl)) {
                        if (TextUtils.isEmpty(akAdTitle) &&
                                TextUtils.isEmpty(akAdDesc)) {
                            adInfo.setContentType(AdInfo.ContentType.PICTURE);
                        } else {
                            adInfo.setContentType(AdInfo.ContentType.PICTURE_WITH_TEXT);
                        }
                    } else {
                        adInfo.setContentType(AdInfo.ContentType.TEXT);
                    }
                    if (ad.getActionType() == 1) {
                        adInfo.setActionType(AdInfo.ActionType.APP_DOWNLOAD);

                        JSONObject akAppJson = JSONObject.parseObject(ad.getAPPInfo().toString());
                        if (akAppJson != null) {
                            String akAppLogo = akAdJson.getString("logo");
                            String akAppPkgName = akAppJson.getString("app_pkg");
                            String akAppName = akAppJson.getString("app_name");
                            adInfo.setAppIconUrl(akAppLogo);
                            adInfo.setDownAppName(akAppName);
                            adInfo.setDownPkgName(akAppPkgName);
                            adInfo.setBrandName(akAppName);
                        }
                    } else {
                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    }
//                    adInfo.setExtra(EXTRA_EVENT_NATIVE_AD, ad);
                    adInfo.setExtra(EXTRA_EVENT_NATIVE_VIDEO_AD, ad);
                    adInfo.setTitle(akAdTitle);
                    adInfo.setText(akAdExtText);
                    adInfo.setDesc(akAdDesc);
                    adInfo.setBtnText(akAdBtnText);
                    adInfo.setImgUrl(akAdImgUrl);
                    adInfo.setAppIconUrl(akAdIconUrl);
                    adInfo.setVideoUrl(akVideoUrl);

                    break;
                }
                if (adInfo != null) {
                    builder.isSucceed(true);
                    builder.adInfo(adInfo);
                } else {
                    errJson.put("akAdErrCode", 0);
                    errJson.put("akAdErrMsg", "no mAds");
                }
            } else {
                errJson.put("akAdErrCode", 0);
                errJson.put("akAdErrMsg", "no mAds");
            }
            builder.errMsg(errJson.toJSONString());

            if (mAdResponseListener == null) {
                return;
            }

            mAdResponseListener.onAdResponse(builder.create());
        }
    }

    private class ApkDownloadListener implements ApkListener {

        @Override
        public void onApkDownloadStart(String s) {
            notifyDownloadCallback(s, AdEvent.EVENT_APP_START_DOWNLOAD);
        }

        @Override
        public void onApkDownloadProgress(String s, int i) {

        }

        @Override
        public void onApkDownloadCompleted(String s) {
            notifyDownloadCallback(s, AdEvent.EVENT_APP_DOWNLOAD_COMPLETE);
        }

        @Override
        public void onApkDownloadFailed(String s) {
            notifyDownloadCallback(s, AdEvent.EVENT_APP_DOWNLOAD_FAILED);
        }

        @Override
        public void onApkDownloadCanceled(String s) {
            notifyDownloadCallback(s, AdEvent.EVENT_APP_DOWNLOAD_CANCELED);
        }

        @Override
        public void onApkDownloadPaused(String s) {

        }

        @Override
        public void onApkDownloadContinued(String s) {

        }

        @Override
        public void onApkInstallCompleted(String s, String s1) {
            notifyDownloadCallback(s, AdEvent.EVENT_APP_INSTALL);
        }
    }
}

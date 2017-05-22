package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;

import com.ak.android.engine.nav.NativeAd;
import com.ak.android.engine.nav.NativeAdLoaderListener;
import com.ak.android.engine.navbase.AdSpace;
import com.ak.android.engine.navbase.NativeAdLoader;
import com.ak.android.engine.navvideo.NativeVideoAd;
import com.ak.android.engine.navvideo.NativeVideoAdLoaderListener;
import com.ak.android.shell.AKAD;
import com.alibaba.fastjson.JSONObject;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.wrapper.download.OkHttpDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 360 聚效广告SDK Wrapper
 */
public class AKAdSDKWrapper implements ISDKWrapper {


    private static final String AK_AD_API_VER = "3.8.3031_0302";

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ThreadPoolUtils mThreadPoolUtils = AdThreadPool.INSTANCE.getThreadPoolUtils();
    private OkHttpDownloader mOkHttpDownloader = new OkHttpDownloader(mClient);
    private String mDownloadPath;

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return AK_AD_API_VER;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        mContext = appContext.getApplicationContext();
        mDownloadPath = mContext.getCacheDir().getAbsolutePath()
                + File.separator + "reaper_ad";
        AKAD.initSdk(appContext, true, true);
    }

    @Override
    public void requestAd(AdRequest adRequest, AdResponseListener adResponseListener) {
        new AKAdRequester(adRequest, adResponseListener).request();
    }

    @Override
    public void onEvent(int adEvent, AdResponse adResponse, Map<String, Object> eventParams) {

    }

    // ----------------------------------------------------

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
                case AdType.TYPE_NATIVE_VIDEO: {
                    requestNativeVideoAd();
                    break;
                }
            }
        }

        private void requestNativeAd() {
            AdSpace adSpace = new AdSpace(mAdRequest.getAdPositionId());
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
            AdSpace adSpace = new AdSpace(mAdRequest.getAdPositionId());
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
        private List<NativeAd> mAds;
        private AdResponseListener mAdResponseListener;

        public AKAdNativeAdRunnable(List<NativeAd> ads,
                                    AdResponseListener adResponseListener) {
            mAds = ads;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            JSONObject errJson = new JSONObject();
            errJson.put("httpResponseCode", 200);

            AdResponse.Builder builder = new AdResponse.Builder();
            builder.adFrom(AdFrom.FROM_AKAD).canCache(false);
            if (mAds != null && mAds.size() > 0) {
                List<AdInfo> adInfos = new ArrayList<>(mAds.size());
                for (NativeAd ad : mAds) {
                    JSONObject akAdJson = JSONObject.parseObject(ad.getContent().toString());
                    if (akAdJson == null) {
                        continue;
                    }
                    String akAdTitle = akAdJson.getString("title");
                    String akAdDesc = akAdJson.getString("desc");
                    String akAdImgUrl = akAdJson.getString("contentimg");

                    AdInfo adInfo = new AdInfo();
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
                        if (akAppJson == null) {
                            continue;
                        }
                        String akAppLogo = akAdJson.getString("logo");
                        String akAppPkgName = akAppJson.getString("app_pkg");
                        String akAppName = akAppJson.getString("app_name");
                        if (TextUtils.isEmpty(akAppPkgName)) {
                            continue;
                        } else {
                            adInfo.setAppIconUrl(akAppLogo);
                            adInfo.setAppName(akAppName);
                            adInfo.setAppPackageName(akAppPkgName);
                            adInfo.setExtra("nativeAd", ad);
                        }
                    } else {
                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    }
                    adInfo.setImgUrl(akAdImgUrl);
                    adInfo.setTitle(akAdTitle);
                    adInfo.setDesc(akAdDesc);

                    if (!TextUtils.isEmpty(adInfo.getImgUrl())) {
                        File imgFile = mOkHttpDownloader.downloadSync(
                                new Request.Builder().url(adInfo.getImgUrl()).build(),
                                mDownloadPath,
                                UUID.randomUUID().toString(),
                                true
                        );
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

        private List<NativeVideoAd> mAds;
        private AdResponseListener mAdResponseListener;

        public AKAdNativeVideoAdRunnable(List<NativeVideoAd> ads,
                                         AdResponseListener adResponseListener) {
            mAds = ads;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            JSONObject errJson = new JSONObject();
            errJson.put("httpResponseCode", 200);

            AdResponse.Builder builder = new AdResponse.Builder();
            builder.adFrom(AdFrom.FROM_AKAD).canCache(false);
            if (mAds != null && mAds.size() > 0) {
                List<AdInfo> adInfos = new ArrayList<>(mAds.size());
                for (NativeVideoAd ad : mAds) {
                    JSONObject akAdJson = JSONObject.parseObject(ad.getContent().toString());
                    if (akAdJson == null) {
                        continue;
                    }
                    String akAdTitle = akAdJson.getString("title");
                    String akAdDesc = akAdJson.getString("desc");
                    String akAdImgUrl = akAdJson.getString("contentimg");
                    String akVideoUrl = akAdJson.getString("video");

                    AdInfo adInfo = new AdInfo();
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
                        if (akAppJson == null) {
                            continue;
                        }
                        String akAppLogo = akAdJson.getString("logo");
                        String akAppKey = akAppJson.getString("key");
                        String akAppPkgName = akAppJson.getString("app_pkg");
                        String akAppName = akAppJson.getString("app_name");
                        if (TextUtils.isEmpty(akAppKey) ||
                                TextUtils.isEmpty(akAppPkgName)) {
                            continue;
                        } else {
                            adInfo.setAppIconUrl(akAppLogo);
                            adInfo.setAppName(akAppName);
                            adInfo.setAppPackageName(akAppPkgName);
                            adInfo.setExtra("nativeVideoAd", ad);
                        }
                    } else {
                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    }
                    adInfo.setImgUrl(akAdImgUrl);
                    adInfo.setTitle(akAdTitle);
                    adInfo.setDesc(akAdDesc);
                    adInfo.setVideoUrl(akVideoUrl);

                    if (!TextUtils.isEmpty(adInfo.getImgUrl())) {
                        File imgFile = mOkHttpDownloader.downloadSync(
                                new Request.Builder().url(adInfo.getImgUrl()).build(),
                                mDownloadPath,
                                UUID.randomUUID().toString(),
                                true
                        );
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
}

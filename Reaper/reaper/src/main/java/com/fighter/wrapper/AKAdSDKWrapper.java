package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.ak.android.engine.nav.NativeAd;
import com.ak.android.engine.nav.NativeAdLoaderListener;
import com.ak.android.engine.navbase.AdSpace;
import com.ak.android.engine.navbase.NativeAdLoader;
import com.ak.android.shell.AKAD;
import com.alibaba.fastjson.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 360 聚效广告SDK Wrapper
 */
public class AKAdSDKWrapper implements ISDKWrapper {


    private static final String AK_AD_API_VER = "3.8.3031_0302";

    private Context mContext;

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return AK_AD_API_VER;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        mContext = appContext;
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
        private WeakReference<AdResponseListener> mRef;

        AKAdRequester(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mRef = new WeakReference<>(adResponseListener);
        }

        public void request() {
            switch (mAdRequest.getAdType()) {
                case AdType.TYPE_NATIVE: {
                    requestNativeAd();
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
                            JSONObject errJson = new JSONObject();
                            errJson.put("httpResponseCode", 200);

                            AdResponse.Builder builder = new AdResponse.Builder();
                            builder.adPositionId(mAdRequest.getAdPositionId());
                            if (ads != null && ads.size() > 0) {
                                Map<String, Object> adExtras = new ArrayMap<>();
                                adExtras.put("oriResponseAds", ads);
                                builder.adExtras(adExtras);

                                List<AdInfo> adInfos = new ArrayList<>(ads.size());
                                for (NativeAd ad : ads) {
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
                                            Map<String, Object> adInfoExtras = new ArrayMap<>();
                                            adInfoExtras.put("akAdAppKey", akAppKey);
                                            adInfo.setExtras(adInfoExtras);
                                        }
                                    } else {
                                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                                    }
                                    adInfo.setImgUrl(akAdImgUrl);
                                    adInfo.setTitle(akAdTitle);
                                    adInfo.setDesc(akAdDesc);
                                    adInfos.add(adInfo);
                                }
                                if (adInfos.size() > 0) {
                                    builder.isSucceed(true);
                                    builder.adInfos(adInfos);
                                } else {
                                    errJson.put("akAdErrCode", 0);
                                    errJson.put("akAdErrMsg", "no ads");
                                }
                            } else {
                                errJson.put("akAdErrCode", 0);
                                errJson.put("akAdErrMsg", "no ads");
                            }
                            builder.errMsg(errJson.toJSONString());

                            AdResponseListener listener = mRef.get();
                            if (listener == null) {
                                return;
                            }

                            listener.onAdResponse(builder.create());
                        }

                        @Override
                        public void onAdLoadFailed(int errCode, String errMsg) {
                            JSONObject errJson = new JSONObject();
                            errJson.put("httpResponseCode", 0);
                            errJson.put("akAdErrCode", errCode);
                            errJson.put("akAdErrMsg", errMsg);

                            AdResponse adResponse = new AdResponse.Builder()
                                    .adPositionId(mAdRequest.getAdPositionId())
                                    .errMsg(errJson.toJSONString())
                                    .create();

                            AdResponseListener listener = mRef.get();
                            if (listener == null) {
                                return;
                            }

                            listener.onAdResponse(adResponse);
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

                AdResponseListener listener = mRef.get();
                if (listener != null) {
                    listener.onAdResponse(adResponse);
                }
            }
        }
    }
}

package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;

import com.android.aserver.ads.MainSDK;
import com.android.aserver.ads.nativead.NativeAdBean;
import com.android.aserver.ads.nativead.NativeAdBean.ImgListBean;
import com.android.aserver.ads.nativead.NativeAdBean.TitleBean;
import com.android.aserver.ads.nativead.NativeAdManager;
import com.android.aserver.ads.nativead.NativeAdViewCallBack;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jia on 8/21/17.
 */
public class QKHuaYiWrapper extends ISDKWrapper {

    private static final String TAG = "QKHuaYiWrapper";

    private static final String VERSION_CODE = "1.0";
    private static final String KEY_ACTUAL_SOURCE = "hy_actual_source";

    private static final String EXTRA_EVENT_SCAN_URL     = "hua_yi_scan_url";
    private static final String EXTRA_EVENT_VIEW_SUCCESS = "hua_yi_show_urls";
    private static final String EXTRA_EVENT_VIEW_CLICK   = "hua_yi_click_urls";

    private static String sAppId;
    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private NativeAdManager mNativeAdManager;

    @Override
    public String getSdkVersion() {
        return VERSION_CODE;
    }

    @Override
    public String getSdkName() {
        return SdkName.QIKU_HUA_YI;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        if (appContext == null) return;
        mContext = appContext;
        MainSDK.init(mContext.getApplicationContext());
        MainSDK.setShowLog(true);
        mNativeAdManager = NativeAdManager.getInstance();
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return false;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        ReaperLog.i(TAG, "[requestAdAsync]");
        new QKHuaYiRequester(adRequest, adResponseListener).request();
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public String requestWebUrl(AdInfo adInfo) {
        ReaperLog.i(TAG, "request web url ");
        return (String) adInfo.getExtra(EXTRA_EVENT_SCAN_URL);
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        //HUA YI do not need report event
        ReaperLog.i(TAG, "adEvent " + adEvent + " adInfo " + adInfo);
        reportEvent(adEvent, adInfo);
    }

    @SuppressWarnings("unchecked")
    private void reportEvent(int adEvent, AdInfo adInfo) {
        List<String> urls = null;
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_SUCCESS:
                urls = (List) adInfo.getExtra(EXTRA_EVENT_VIEW_SUCCESS);
                break;
            case AdEvent.EVENT_CLICK:
                urls = (List) adInfo.getExtra(EXTRA_EVENT_VIEW_CLICK);
                break;
        }
        if (urls == null || urls.size() == 0) {
            ReaperLog.i(TAG, "ignore event type " + adEvent);
            return;
        }
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            ReaperLog.i(TAG, "event report with url " + url);
            Request request = new Request.Builder()
                    .addHeader("content-type", "application/json;charset:utf-8")
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = mClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    ReaperLog.e(TAG, url + " event report succeed : " + adEvent);
                } else {
                    ReaperLog.e(TAG, "Event report failed : " + adEvent);
                }
            } catch (IOException e) {
                ReaperLog.e(TAG, "report event failed " + e.toString());
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(response);
            }
        }
    }


    private class QKHuaYiRequester {
        private AdRequest mAdRequest;
        private AdResponseListener mAdResponseListener;

        QKHuaYiRequester(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAdResponseListener = adResponseListener;
        }

        public void request() {
            HashMap<String, String> configMap = new HashMap<>();
            configMap.put(MainSDK.KEY_QK_APPID, mAdRequest.getAdLocalAppId());     //应用ID
            configMap.put(MainSDK.KEY_QK_ADID, mAdRequest.getAdLocalPositionId()); //广告位ID
            mNativeAdManager.initContext(mContext.getApplicationContext())
                    .init(configMap);
            final AdResponse.Builder builder = new AdResponse.Builder();
            builder.adName(SdkName.QIKU_HUA_YI)
                    .adPosId(mAdRequest.getAdPosId())
                    .adType(mAdRequest.getAdType())
                    .adLocalAppId(mAdRequest.getAdLocalAppId())
                    .adLocalPositionAd(mAdRequest.getAdLocalPositionId());
            ReaperLog.e(TAG, "start request ad " + Device.getCurrentLocalTime());
            mNativeAdManager.setAdLoadCallBack(new NativeAdViewCallBack() {
                @Override
                public void onAdPresent(String s, NativeAdBean nativeAdBean) {
                    if (!TextUtils.isEmpty(s))
                        ReaperLog.i(TAG, "onAdPresent request ad " +
                                Device.getCurrentLocalTime() + " s " + s);

                    AdInfo adInfo = generateAdInfo(mAdRequest);
                    // 广告类型 INFOFLOW_ONEPIC(11)大图广告;INFOFLOW_THREEPIC(12)三图广告
                    int adType = nativeAdBean.getAdType();
                    adInfo.setContentType(getAdInfoType(adType));

                    //按钮文字
                    String buttonTxt = nativeAdBean.getButtonTxt();
                    if (!TextUtils.isEmpty(buttonTxt))
                        adInfo.setBtnText(buttonTxt);
                    //图片bean
                    List<ImgListBean> imgList = nativeAdBean.getImgList();
                    if (imgList != null && !imgList.isEmpty()) {
                        List<String> imgUrls = new ArrayList<>();
                        for (ImgListBean imgBean : imgList) {
                            String url = imgBean.getUrl();
                            int width = imgBean.getWidth();
                            int height = imgBean.getHeight();
                            imgUrls.add(url);
                        }
                        if (imgList.size() == 1) {
                            adInfo.setImgUrl(imgUrls.get(0));
                        } else if (imgList.size() >= 3) {
                            adInfo.setImgUrls(imgUrls);
                        }
                    }
                    //广告源
                    String sourceTxt = nativeAdBean.getSourceTxt();
                    if (!TextUtils.isEmpty(sourceTxt))
                        adInfo.setExtra(KEY_ACTUAL_SOURCE, sourceTxt);
                    //标题
                    TitleBean title = nativeAdBean.getTitle();
                    if (title != null && !TextUtils.isEmpty(title.getText()))
                        adInfo.setTitle(title.getText());
                    //点击链接
                    String squeezePageUrl = nativeAdBean.getSqueezePageUrl();
                    if (!TextUtils.isEmpty(squeezePageUrl)) {
                        adInfo.setExtra(EXTRA_EVENT_SCAN_URL, squeezePageUrl);
                        adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    }
                    //展示曝光上报链接
                    List<String> showUrlList = nativeAdBean.getShowUrlList();
                    if (showUrlList != null && !showUrlList.isEmpty()) {
                        adInfo.setExtra(EXTRA_EVENT_VIEW_SUCCESS, showUrlList);
                    }
                    //点击链接
                    List<String> clickUrlList = nativeAdBean.getClickUrlList();
                    if (clickUrlList != null && !clickUrlList.isEmpty()) {
                        adInfo.setExtra(EXTRA_EVENT_VIEW_CLICK, clickUrlList);
                    }
                    ReaperLog.i(TAG, "adInfo " + adInfo.toString());
                    mAdResponseListener.onAdResponse(
                            builder.isSucceed(true)
                                    .adInfo(adInfo)
                                    .create());
                }

                @Override
                public void onAdFailed(String s, String s1) {
                    ReaperLog.e(TAG, "onAdFailed request ad " + Device.getCurrentLocalTime());
                    String errMsg = ("on ad present fail local pos id is " +
                            (TextUtils.isEmpty(s) ? "unKnown" : s) +
                            " errMsg " + (TextUtils.isEmpty(s1) ? "unKnown" : s1));
                    ReaperLog.e(TAG, errMsg);
                    mAdResponseListener.onAdResponse(
                            builder.isSucceed(false)
                                    .errMsg(errMsg)
                                    .create());
                }
            });
        }

        private AdInfo generateAdInfo(AdRequest adRequest) {
            AdInfo adInfo = new AdInfo();
            adInfo.generateUUID();
            adInfo.setExpireTime(adRequest.getExpireTime());
            adInfo.setSilentInstall(adRequest.getSilentInstall());
            adInfo.setCanCache(true);
            adInfo.setAdName(SdkName.QIKU_HUA_YI);
            adInfo.setAdPosId(adRequest.getAdPosId());
            adInfo.setAdType(adRequest.getAdType());
            adInfo.setAdLocalAppId(adRequest.getAdLocalAppId());
            adInfo.setAdLocalPosId(adRequest.getAdLocalPositionId());
            return adInfo;
        }

        private int getAdInfoType(int adType) {
            switch (adType) {
                case 11://INFOFLOW_ONEPIC(11)大图广告;
                    return AdInfo.ContentType.PICTURE_WITH_TEXT;
                case 12://INFOFLOW_THREEPIC(12)三图广告
                    return AdInfo.ContentType.MULTI_PICTURES;
                default:
                    return -1;
            }
        }
    }
}

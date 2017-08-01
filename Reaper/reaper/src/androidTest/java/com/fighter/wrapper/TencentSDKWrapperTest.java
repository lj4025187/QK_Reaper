package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.common.utils.ReaperLog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TencentSDKWrapperTest {
    private static final String TAG = TencentSDKWrapperTest.class.getSimpleName();

    @Test
    public void testNativeAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
                .adLocalAppId("1104241296")
//                .adLocalPositionId("6050305154328807")/*Banner240*38*/
                .adLocalPositionId("9060201144323878")/*Banner320*50*/
//                .adLocalPositionId("3000800184026889")/*Banner480*75*/
//                .adLocalPositionId("5060504124524896")/*Banner640*100*/
//                .adLocalPositionId("1060308114529681")/*Insert600*500*/
//                .adType(AdType.TYPE_BANNER)
                .adType(AdType.TYPE_PLUG_IN)
                .adCount(1)
                .adWidth(320)
                .adHeight(50)
                .adExtra(TencentSDKWrapper.EXTRA_LAT, 0)
                .adExtra(TencentSDKWrapper.EXTRA_LNG, 0)
                .adExtra(TencentSDKWrapper.EXTRA_COORDTIME, System.currentTimeMillis())
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new TencentSDKWrapper();
        sdkWrapper.init(context, null);

        AdResponse adResponse = sdkWrapper.requestAdSync(adRequest);

        ReaperLog.i(TAG, "response " + adResponse);

        if (adResponse != null &&
                adResponse.isSucceed()) {
            AdInfo adInfo = adResponse.getAdInfo();
            if (adInfo != null && adInfo.canCache()) {
                String cacheJson = AdInfo.convertToString(adInfo);
                ReaperLog.i(TAG, "ad info cache json " + cacheJson);
                AdInfo cacheAdInfo = AdInfo.convertFromString(cacheJson);
                ReaperLog.i(TAG, "ad info cache obj " + cacheAdInfo);
                Assert.assertNotNull(cacheAdInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_VIEW_SUCCESS, adInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_CLICK, adInfo);

                if (adInfo.getActionType() == AdInfo.ActionType.BROWSER) {
                    ReaperLog.i(TAG, "Browser url " + sdkWrapper.requestWebUrl(adInfo));
                } else {
                    ReaperLog.i(TAG, "App url " + sdkWrapper.requestDownloadUrl(adInfo));
                }
            }
        }
    }
}

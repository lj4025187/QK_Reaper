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
public class MixAdxSDKWrapperTest {
    private static final String TAG = MixAdxSDKWrapperTest.class.getSimpleName();

    @Test
    public void testNativeAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
                .adLocalAppId("0")
                .adLocalPositionId("128")
                .adType(AdType.TYPE_BANNER)
                .adCount(1)
                .adWidth(600)
                .adHeight(300)
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new MixAdxSDKWrapper();
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
                sdkWrapper.onEvent(AdEvent.EVENT_VIEW_SUCCESS, cacheAdInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_CLICK, cacheAdInfo);

                if (cacheAdInfo.getActionType() == AdInfo.ActionType.BROWSER) {
                    ReaperLog.i(TAG, "Browser url " + sdkWrapper.requestWebUrl(cacheAdInfo));
                } else {
                    ReaperLog.i(TAG, "App url " + sdkWrapper.requestDownloadUrl(cacheAdInfo));
                }
            }
        }
    }
}

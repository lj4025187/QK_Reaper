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

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TencentSDKWrapperTest {
    private static final String TAG = TencentSDKWrapperTest.class.getSimpleName();

    @Test
    public void testNativeAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("")
                .adLocalAppId("1104241296")
                .adLocalPositionId("5060504124524896")
                .adType(AdType.TYPE_BANNER)
                .adCount(1)
                .adWidth(640)
                .adHeight(100)
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
                adResponse.isSucceed() &&
                adResponse.canCache()) {
            String responseJson = sdkWrapper.convertToString(adResponse);
            ReaperLog.i(TAG, "response cache json " + responseJson);
            AdResponse cacheAdResponse = sdkWrapper.convertFromString(responseJson);
            ReaperLog.i(TAG, "response cache obj " + cacheAdResponse);
            Assert.assertNotNull(cacheAdResponse);

            sdkWrapper.onEvent(AdEvent.EVENT_VIEW, adResponse.getAdInfos().get(0));
            sdkWrapper.onEvent(AdEvent.EVENT_CLICK, adResponse.getAdInfos().get(0));

            List<AdInfo> adInfoList = adResponse.getAdInfos();
            for (AdInfo adInfo : adInfoList) {
                if (adInfo.getActionType() == AdInfo.ActionType.BROWSER) {
                    ReaperLog.i(TAG, "Browser url " + sdkWrapper.requestWebUrl(adInfo));
                } else {
                    ReaperLog.i(TAG, "App url " + sdkWrapper.requestDownloadUrl(adInfo));
                }
            }
        }
    }
}

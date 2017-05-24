package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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
                .appId("1104241296")
                .adPositionId("5060504124524896")
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

        sdkWrapper.requestAdAsync(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                ReaperLog.i(TAG, "response " + adResponse);

                if (adResponse != null &&
                        adResponse.isSucceed() &&
                        adResponse.canCache()) {
                    ICacheConvert convert = (ICacheConvert) sdkWrapper;
                    String responseJson = convert.convertToString(adResponse);
                    ReaperLog.i(TAG, "response cache json " + responseJson);
                    AdResponse cacheAdResponse = convert.convertFromString(responseJson);
                    ReaperLog.i(TAG, "response cache obj " + cacheAdResponse);
                    Assert.assertNotNull(cacheAdResponse);

                    sdkWrapper.onEvent(AdEvent.EVENT_VIEW, adResponse.getAdInfos().get(0), null);
                    sdkWrapper.onEvent(AdEvent.EVENT_CLICK, adResponse.getAdInfos().get(0), null);
                }

            }
        });

        try {
            Thread.sleep(120 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

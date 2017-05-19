package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

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
        Log.d(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new TencentSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
        sdkWrapper.requestAd(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                Log.d(TAG, "response " + adResponse);

                if (adResponse != null &&
                        adResponse.isSucceed() &&
                        adResponse.canCache()) {
                    ICacheConvert convert = (ICacheConvert) sdkWrapper;
                    String responseJson = convert.convertToString(adResponse);
                    Log.d(TAG, "response cache json " + responseJson);
                    AdResponse cacheAdResponse = convert.convertFromString(responseJson);
                    Log.d(TAG, "response cache obj " + cacheAdResponse);
                    Assert.assertNotNull(cacheAdResponse);
                }

                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

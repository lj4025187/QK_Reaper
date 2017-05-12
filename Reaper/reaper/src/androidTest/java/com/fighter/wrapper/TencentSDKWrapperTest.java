package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

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
        Log.d(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getContext();
        ISDKWrapper sdkWrapper = new TencentSDKWrapper();
        sdkWrapper.init(context, null);
        AdResponse adResponse = sdkWrapper.requestAd(adRequest);
        Log.d(TAG, "response " + adResponse);
    }
}

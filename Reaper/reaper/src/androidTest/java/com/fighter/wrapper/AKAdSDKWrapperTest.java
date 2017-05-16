package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class AKAdSDKWrapperTest {
    private static final String TAG = AKAdSDKWrapperTest.class.getSimpleName();

    @Test
    public void testRequestAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .appId("1104241296")
                .adPositionId("5060504124524896")
                .adType(AdType.TYPE_NATIVE)
                .adCount(1)
                .adWidth(640)
                .adHeight(100)
                .create();
        Log.d(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        ISDKWrapper sdkWrapper = new AKAdSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
        sdkWrapper.requestAd(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                Log.d(TAG, "response " + adResponse);
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestVideoAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .appId("1104241296")
                .adPositionId("5060504124524896")
                .adType(AdType.TYPE_NATIVE_VIDEO)
                .adCount(1)
                .create();
        Log.d(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        ISDKWrapper sdkWrapper = new AKAdSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
        sdkWrapper.requestAd(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                Log.d(TAG, "response " + adResponse);
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

package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.common.utils.ReaperLog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class AKAdSDKWrapperTest {
    private static final String TAG = AKAdSDKWrapperTest.class.getSimpleName();

    @Test
    public void testRequestAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
                .adLocalAppId("1104241296")
                .adLocalPositionId("5060504124524896")
                .adType(AdType.TYPE_NATIVE)
                .adCount(1)
                .adWidth(640)
                .adHeight(100)
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        final Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new AKAdSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
        sdkWrapper.requestAdAsync(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                ReaperLog.i(TAG, "response " + adResponse);

                if (adResponse != null && adResponse.isSucceed()) {
                    AdInfo adInfo = adResponse.getAdInfo();
                    sdkWrapper.onEvent(AdEvent.EVENT_VIEW, adInfo);
                    sdkWrapper.onEvent(AdEvent.EVENT_CLICK, adInfo);
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

    @Test
    public void testRequestVideoAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
                .adLocalAppId("1104241296")
                .adLocalPositionId("5060504124524896")
                .adType(AdType.TYPE_NATIVE_VIDEO)
                .adCount(1)
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        ISDKWrapper sdkWrapper = new AKAdSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
        sdkWrapper.requestAdAsync(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                ReaperLog.i(TAG, "response " + adResponse);
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

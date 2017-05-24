package com.fighter.wrapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.common.utils.ReaperLog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class MixAdxSDKWrapperTest {
    private static final String TAG = MixAdxSDKWrapperTest.class.getSimpleName();

    @Test
    public void testNativeAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .appId("0")
                .adPositionId("128")
                .adType(AdType.TYPE_BANNER)
                .adCount(1)
                .adWidth(600)
                .adHeight(300)
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new MixAdxSDKWrapper();
        sdkWrapper.init(context, null);

        final CountDownLatch signal = new CountDownLatch(1);
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

                    ReaperLog.i(TAG, "response ori " + cacheAdResponse.getOriResponse());

                    sdkWrapper.onEvent(AdEvent.EVENT_VIEW,
                            cacheAdResponse.getAdInfos().get(0),
                            null);

                    sdkWrapper.onEvent(AdEvent.EVENT_CLICK,
                            cacheAdResponse.getAdInfos().get(0),
                            null);

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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

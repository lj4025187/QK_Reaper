package com.fighter.loader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ReaperApiTest {
    private static final String TAG = ReaperApiTest.class.getSimpleName();

    @Test
    public void testInit() {
        Context context = InstrumentationRegistry.getTargetContext();
        ReaperApi reaperApi = ReaperInit.init(context);
        Assert.assertNotNull(reaperApi);

        reaperApi.init(context, "10010", "not_a_real_key", null);
    }

    @Test
    public void testGetAdRequester() {
        Context context = InstrumentationRegistry.getTargetContext();
        ReaperApi reaperApi = ReaperInit.init(context);
        Assert.assertNotNull(reaperApi);

        reaperApi.init(context, "10010", "not_a_real_key", null);

        ReaperApi.AdRequester adRequester =
                reaperApi.getAdRequester("323232", new ReaperApi.AdRequestCallback() {
                    @Override
                    public void onSuccess(List<ReaperApi.AdInfo> ads) {
                        Log.d(TAG, "onSuccess " + ads);
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        Log.d(TAG, "onFailed " + errMsg);
                    }
                }, null);

        adRequester.requestAd();

        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

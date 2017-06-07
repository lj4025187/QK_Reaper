package com.fighter.loader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.utils.LoaderLog;

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

        reaperApi.init(context, "10010", "not_a_real_key", true);
    }

    @Test
    public void testGetAdRequester() {
        Context context = InstrumentationRegistry.getTargetContext();
        ReaperApi reaperApi = ReaperInit.init(context);
        Assert.assertNotNull(reaperApi);

        reaperApi.init(context, "10010", "not_a_real_key", true);

        AdRequester adRequester =
                reaperApi.getAdRequester("323232", new AdRequester.AdRequestCallback() {
                    @Override
                    public void onSuccess(AdInfo adInfo) {
                        LoaderLog.i(TAG, "onSuccess " + adInfo);
                        adInfo.onAdShow(null);
                        adInfo.onAdClicked(null, null, -999, -999, -999, -999);
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        LoaderLog.i(TAG, "onFailed " + errMsg);
                    }
                });

        adRequester.requestAd(1);

        try {
            Thread.sleep(500
                    * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

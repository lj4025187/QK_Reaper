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

/**
 * Created by jia on 8/21/17.
 */
@RunWith(AndroidJUnit4.class)
public class QKHuaYiSDKWrapperTest {

    private static final String TAG = "QKHuaYiSDKWrapperTest";

    @Test
    public void testHuaYiNativeAd() {
        final AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
//                .adLocalAppId("7")//华屹demo大图广告
//                .adLocalPositionId("367")//华屹demo大图广告
//                .adLocalAppId("16")//华屹demo三图广告
//                .adLocalPositionId("370")//华屹demo三图广告
                .adLocalAppId("17")//sample app id
//                .adLocalPositionId("371")//sample大图广告
                .adLocalPositionId("372")//sample三图广告
                .adType(AdType.TYPE_NATIVE)
                .adCount(1)
                .adWidth(600)
                .adHeight(300)
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new QKHuaYiSDKWrapper();
        sdkWrapper.init(context, null);
        sdkWrapper.requestAdAsync(adRequest, new AdResponseListener() {
            @Override
            public void onAdResponse(AdResponse adResponse) {
                ReaperLog.i(TAG, "response " + adResponse);
                if (adResponse != null && adResponse.isSucceed()) {
                    final AdInfo adInfo = adResponse.getAdInfo();
                    new Thread() {
                        @Override
                        public void run() {
                            sdkWrapper.onEvent(AdEvent.EVENT_VIEW_SUCCESS, adInfo);
                        }
                    }.start();
                    new Thread() {
                        @Override
                        public void run() {
                            sdkWrapper.onEvent(AdEvent.EVENT_CLICK, adInfo);
                        }
                    }.start();
                }
            }
        });
        try {
            Thread.sleep(500
                    * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

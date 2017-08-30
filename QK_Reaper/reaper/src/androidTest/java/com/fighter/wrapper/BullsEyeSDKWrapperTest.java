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

import java.util.HashMap;

/**
 * Created by jia on 7/17/17.
 */
@RunWith(AndroidJUnit4.class)
public class BullsEyeSDKWrapperTest {

    private static final String TAG = BullsEyeSDKWrapperTest.class.getSimpleName();

    @Test
    public void testBullsEyeNativeAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .adPosId("1")
                .adExpireTime(30 * 60 * 1000)
                .adLocalAppId("100001")
//                .adLocalPositionId("1001")//电影类广告
//                .adLocalPositionId("1002")//应用下载类广告
                .adLocalPositionId("1005")//美食类广告
                .adType(AdType.TYPE_NATIVE)
                .adCount(1)
                .adWidth(600)
                .adHeight(300)
                .adExtra(BullsEyeSDKWrapper.EXTRA_GPS_SPEED, "0")
                .adExtra(BullsEyeSDKWrapper.EXTRA_GPS_ACCURACY, "0")
                .adExtra(BullsEyeSDKWrapper.EXTRA_GPS_LAT, "39.9811016777")
                .adExtra(BullsEyeSDKWrapper.EXTRA_GPS_LON, "116.4883012203")
                .adExtra(BullsEyeSDKWrapper.EXTRA_CURRENT_MILLIS, String.valueOf(System.currentTimeMillis()))
                .create();
        ReaperLog.i(TAG, "request " + adRequest);

        Context context = InstrumentationRegistry.getTargetContext();
        final ISDKWrapper sdkWrapper = new BullsEyeSDKWrapper();
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_id", "100001");
        params.put("app_key", "qwertyuiop");
        sdkWrapper.init(context, params);
        AdResponse adResponse = sdkWrapper.requestAdSync(adRequest);

        ReaperLog.i(TAG, "response " + adResponse);

        if (adResponse != null &&
                adResponse.isSucceed()) {
            AdInfo adInfo = adResponse.getAdInfo();
            if (adInfo != null && adInfo.canCache()) {
                String cacheJson = AdInfo.convertToString(adInfo);
                ReaperLog.i(TAG, "ad info cache json " + cacheJson);
                AdInfo cacheAdInfo = AdInfo.convertFromString(cacheJson);
                ReaperLog.i(TAG, "ad info cache obj " + cacheAdInfo);
                Assert.assertNotNull(cacheAdInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_VIEW_SUCCESS, cacheAdInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_CLICK, cacheAdInfo);
                sdkWrapper.onEvent(AdEvent.EVENT_AD_DOWN_FAIL, cacheAdInfo);

                if (cacheAdInfo.getActionType() == AdInfo.ActionType.BROWSER) {
                    ReaperLog.i(TAG, "Browser url " + sdkWrapper.requestWebUrl(cacheAdInfo));
                } else {
                    ReaperLog.i(TAG, "App url " + sdkWrapper.requestDownloadUrl(cacheAdInfo));
                }
            }
        }
    }

}

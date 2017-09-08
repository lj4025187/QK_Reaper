package com.fighter.wrapper;

import android.content.Context;

import com.adeaz.AdeazController;
import com.fighter.ad.AdInfo;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.utils.ReaperLog;

import java.util.Map;

/**
 * 互众广告
 * <p/>
 * Created by jia on 9/7/17.
 */
public class AdeazSDKWrapper extends ISDKWrapper {

    private static final String TAG = "AdeazSDKWrapper";
    public static boolean ADEAZ_TEST = false;

    @Override
    public String getSdkVersion() {
        return null;
    }

    @Override
    public String getSdkName() {
        return null;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        if (appContext == null) return;
        ReaperLog.i(TAG, "[init] AdeazSDKWrapper");
        ADEAZ_TEST |= Device.checkSDKMode(SdkName.HU_ZHONG);
        AdeazController.init(appContext, ADEAZ_TEST);
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return false;
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {

    }
}

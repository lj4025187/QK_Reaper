package com.fighter.reaper.sample;


import android.app.Application;

import com.fighter.loader.ReaperApi;
import com.fighter.loader.ReaperInit;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.utils.ResponseGenerator;
import com.fighter.reaper.sample.utils.ToastUtil;

/**
 * Created by LiuJia on 2017/5/19.
 */

//public class SampleApp extends ReaperApplication {
//
//    private final static String TAG = SampleApp.class.getSimpleName();
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        if(mReaperApi == null) {
//            SampleLog.e(TAG, "Sample app onCreate method mReaperApi is null");
//            ToastUtil.getInstance(this).showSingletonToast(getString(R.string.ad_reaper_init_failed));
//            return;
//        }
//        mReaperApi.init(this, SampleConfig.APP_ID, SampleConfig.APP_KEY, true);
//        if(SampleConfig.LOCAL_CONFIG)
//            mReaperApi.setTargetConfig(ResponseGenerator.generate());
//    }
//
//}
public class SampleApp extends Application {

    private final static String TAG = SampleApp.class.getSimpleName();
    protected ReaperApi mReaperApi;

    @Override
    public void onCreate() {
        super.onCreate();
        if(mReaperApi == null) {
            mReaperApi = ReaperInit.init(this);
        }
        if(mReaperApi == null) {
            ToastUtil.getInstance(this).showSingletonToast(getString(R.string.ad_reaper_init_failed));
            return;
        }
        mReaperApi.init(this, SampleConfig.TEST_MODE ? SampleConfig.TEST_APP_ID : SampleConfig.RELEASE_APP_ID,
                SampleConfig.TEST_MODE ? SampleConfig.TEST_APP_KEY : SampleConfig.RELEASE_APP_KEY,
                SampleConfig.TEST_MODE);
        if(SampleConfig.LOCAL_CONFIG)
            mReaperApi.setTargetConfig(ResponseGenerator.generate());
    }

    public ReaperApi getReaperApi() {
        return mReaperApi;
    }

}
package com.fighter.reaper.sample;


import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

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
        if (mReaperApi == null) {
            mReaperApi = ReaperInit.init(this);
        }
        if (mReaperApi == null) {
            ToastUtil.getInstance(this).showSingletonToast(getString(R.string.ad_reaper_init_failed));
            return;
        }

        //模拟负一屏
        Context context = getApplicationContext();
        if (SampleConfig.CARD_MANAGER_MODE) {
            ContextProxy proxy = new ContextProxy(context);
            mReaperApi.init(proxy,
                    SampleConfig.TEST_MODE ? SampleConfig.TEST_CARD_APP_ID : SampleConfig.RELEASE_CARD_APP_ID,
                    SampleConfig.TEST_MODE ? SampleConfig.TEST_CARD_APP_KEY : SampleConfig.RELEASE_CARD_APP_KEY,
                    SampleConfig.TEST_MODE);
        } else {
            mReaperApi.init(context,
                    SampleConfig.TEST_MODE ? SampleConfig.TEST_SAMPLE_APP_ID : SampleConfig.RELEASE_SAMPLE_APP_ID,
                    SampleConfig.TEST_MODE ? SampleConfig.TEST_SAMPLE_APP_KEY : SampleConfig.RELEASE_SAMPLE_APP_KEY,
                    SampleConfig.TEST_MODE);
        }
        if (SampleConfig.LOCAL_CONFIG)
            mReaperApi.setTargetConfig(ResponseGenerator.generate());
    }

    public ReaperApi getReaperApi() {
        return mReaperApi;
    }


    class ContextProxy extends ContextWrapper {

        public ContextProxy(Context base) {
            super(base);
        }

        @Override
        public String getPackageName() {
            return "com.qiku.cardmanager";
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }
    }
}
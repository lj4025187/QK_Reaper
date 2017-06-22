package com.fighter.reaper.sample;


import com.fighter.loader.ReaperApplication;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.utils.ResponseGenerator;
import com.fighter.reaper.sample.utils.SampleLog;
import com.fighter.reaper.sample.utils.ToastUtil;

/**
 * Created by LiuJia on 2017/5/19.
 */

public class SampleApp extends ReaperApplication {

    private final static String TAG = SampleApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if(mReaperApi == null) {
            SampleLog.e(TAG, "Sample app onCreate method mReaperApi is null");
            ToastUtil.getInstance(this).showSingletonToast(getString(R.string.ad_reaper_init_failed));
            return;
        }
        mReaperApi.init(this, SampleConfig.APP_ID, SampleConfig.APP_KEY, true);
        if(SampleConfig.LOCAL_CONFIG)
            mReaperApi.setTargetConfig(ResponseGenerator.generate());
    }

}

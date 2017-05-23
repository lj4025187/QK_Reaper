package com.qiku.sample;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.fighter.loader.ReaperApi;

/**
 * Created by Administrator on 2017/5/19.
 */

public class BaseActivity extends Activity implements Handler.Callback {

    protected ReaperApi mReaperApi;
    protected Context mContext;
    protected Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Application application = getApplication();
        if (application instanceof SampleApp) {
            SampleApp app = (SampleApp) application;
            mReaperApi = app.getReaperApi();
        }
        mContext = application;
        new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }
}

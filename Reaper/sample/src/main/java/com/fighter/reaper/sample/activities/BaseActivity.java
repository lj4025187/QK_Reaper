package com.fighter.reaper.sample.activities;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.fighter.loader.ReaperApi;
import com.fighter.reaper.sample.SampleApp;

/**
 * Created by Administrator on 2017/5/19.
 */

public class BaseActivity extends FragmentActivity implements Handler.Callback {

    public ReaperApi mReaperApi;
    protected Context mContext;
    protected Handler mMainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        if (application instanceof SampleApp) {
            SampleApp app = (SampleApp) application;
            mReaperApi = app.getReaperApi();
        }
        mContext = application;
        mMainHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }
}

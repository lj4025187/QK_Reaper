package com.fighter.loader;

import android.app.Application;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApplication extends Application {

    protected ReaperApi mReaperApi;

    @Override
    public void onCreate() {
        super.onCreate();

        mReaperApi = ReaperInit.init(this);
    }
}

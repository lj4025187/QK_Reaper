package com.fighter.loader;

import android.app.Application;

/**
 * If you create a Application extends ReaperApplication,
 * you can get ReaperApi by {@link ReaperApplication#getReaperApi()}.
 *
 * If you use
 *
 *  ReaperApi api = ReaperInit.init(Context);
 *
 * in your Application's onCreate, you should manage api yourself.
 *
 * Created by wxthon on 5/5/17.
 */

public class ReaperApplication extends Application {

    protected ReaperApi mReaperApi;

    @Override
    public void onCreate() {
        super.onCreate();

        mReaperApi = ReaperInit.init(this);
    }

    /**
     * If you extends ReaperApplication, you can get ReaperApi by this.
     * @return ReaperApi
     */
    public ReaperApi getReaperApi() {
        return mReaperApi;
    }
}

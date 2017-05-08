package com.fighter.loader;

import com.fighter.utils.Slog;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApi {

    private static final java.lang.String TAG = ReaperApi.class.getSimpleName();

    private Object mInstance;

    public ReaperApi(Object instance) {
        mInstance = instance;
        Slog.e(TAG, "mInstance : " + instance);
    }


}

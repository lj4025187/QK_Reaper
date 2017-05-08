package com.fighter.loader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.utils.Slog;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by huayang on 17-5-8.
 */

@RunWith(AndroidJUnit4.class)
public class ReaperInitTest {

    public static final String TAG = ReaperInitTest.class.getSimpleName();

    @Test
    public void testInit() {
        Context context = InstrumentationRegistry.getContext();
        ReaperApi reaperApi = ReaperInit.init(context);
        Slog.e(TAG, "reaperApi : " + reaperApi);
    }

}

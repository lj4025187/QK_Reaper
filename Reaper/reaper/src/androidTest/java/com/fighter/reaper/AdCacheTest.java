package com.fighter.reaper;

import android.content.Context;
import android.os.HandlerThread;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.Collections2;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.api.ReaperApi;
import com.fighter.cache.AdCacheInfo;
import com.fighter.cache.AdCacheManager;
import com.fighter.common.utils.ReaperLog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lichen on 17-5-17.
 */
@RunWith(AndroidJUnit4.class)
public class AdCacheTest {
    private static final String TAG = AdCacheTest.class.getSimpleName();

    @Test
    public void useCacheAdInfo () throws Exception {
        HandlerThread thread = new HandlerThread("adCache test") {
            @Override
            protected void onLooperPrepared() {
                Context context = InstrumentationRegistry.getTargetContext();
                AdCacheManager adCacheManager = AdCacheManager.getInstance();
                adCacheManager.init(context, "100010", "not_a_real_key");

                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Class <?> clz = AdCacheManager.class;
                Field fieldPath = null;
                try {
                    fieldPath = clz.getDeclaredField("mCacheDir");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                fieldPath.setAccessible(true);
                String path = null;
                try {
                    path = ((File)fieldPath.get(adCacheManager)).getAbsolutePath();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ReaperLog.i(TAG, "test CacheDir path = " + path);
                Assert.assertNotNull(path);
                Field cacheField = null;
                try {
                    cacheField = clz.getDeclaredField("mAdCache");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                cacheField.setAccessible(true);
                HashMap<String, List<Object>> cacheMap = null;
                try {
                    cacheMap = (HashMap<String, List<Object>>) cacheField.get(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ReaperLog.i(TAG, "cache map = " + cacheMap);
                Assert.assertTrue(cacheMap.size() > 0);

                adCacheManager.requestAdCache("1", new TestCallBack() {
                    @Override
                    public void onResponse(Map<String, Object> params) {
                        ReaperLog.i(TAG, "onResponse param = " + params);
                        Assert.assertNotNull(params);
                        Assert.assertTrue(params.size() > 0);
                    }
                });
            }
        };
        thread.start();
    }

    @Test
    public void localTest() throws Exception {
    }

    private interface TestCallBack {
        void onResponse(Map<String, Object> params);
    }
}

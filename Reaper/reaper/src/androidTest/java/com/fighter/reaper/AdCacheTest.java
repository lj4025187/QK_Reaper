package com.fighter.reaper;

import android.content.Context;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Created by lichen on 17-5-17.
 */
@RunWith(AndroidJUnit4.class)
public class AdCacheTest {
    private static final String TAG = AdCacheTest.class.getSimpleName();

    private void putParam(Map<String, Object> params, String key, Object value) {
        if (params != null && !TextUtils.isEmpty(key) && value != null) {
            params.put(key, value);
        }
    }
    @Test
    public void useCacheAdInfo () throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Map<String, Object> params = new ArrayMap<>();
        putParam(params, "appContext", context);
        putParam(params, "appId", "100010");
        putParam(params, "appKey", "not_a_real_key");
        AdCacheManager adCacheManager = AdCacheManager.getInstance();
        adCacheManager.init(params);

        Thread.sleep(2*1000);

        Class <?> clz = AdCacheManager.class;
        Field fieldPath = clz.getDeclaredField("mCacheDir");
        fieldPath.setAccessible(true);
        final String path =  ((File)fieldPath.get(adCacheManager)).getAbsolutePath();
        ReaperLog.i(TAG, "test CacheDir path = " + path);
        Assert.assertNotNull(path);
        Field cachePathField = clz.getDeclaredField("mAdCacheFilePath");
        cachePathField.setAccessible(true);
        Object map = cachePathField.get(null);
        HashMap<String, List<String>> cachePathMap = (HashMap<String, List<String>>)map;
        ReaperLog.i(TAG, "cache path map = " + cachePathMap);
        Assert.assertTrue(cachePathMap.size() > 0);
        Field cacheField = clz.getDeclaredField("mAdCache");
        cacheField.setAccessible(true);
        HashMap<String, List<Object>> cacheMap = (HashMap<String, List<Object>>) cacheField.get(null);
        ReaperLog.i(TAG, "cache map = " + cacheMap);
        Assert.assertTrue(cacheMap.size() > 0);

        adCacheManager.requestAdCache("121212", new TestCallBack() {
            @Override
            public void onResponse(Map<String, Object> params) {
                ReaperLog.i(TAG, "onResponse param = " + params);
                Assert.assertNotNull(params);
                Assert.assertTrue(params.size() > 0);
            }
        });
    }

    private interface TestCallBack {
        void onResponse(Map<String, Object> params);
    }
}

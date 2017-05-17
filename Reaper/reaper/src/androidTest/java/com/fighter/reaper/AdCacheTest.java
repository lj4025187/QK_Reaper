package com.fighter.reaper;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.cache.AdCacheInfo;
import com.fighter.cache.AdCacheManager;
import com.fighter.common.utils.ReaperLog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

/**
 * Created by lichen on 17-5-17.
 */
@RunWith(AndroidJUnit4.class)
public class AdCacheTest {
    private static final String TAG = AdCacheTest.class.getSimpleName();

    @Test
    public void useCacheAdInfo () throws Exception {
        AdCacheManager adCacheManager = new AdCacheManager(InstrumentationRegistry.getTargetContext());
        AdCacheInfo info = new AdCacheInfo();
        info.setAppPostId("888888");
        adCacheManager.cacheAdInfo("1", info);
        Object getInfo = adCacheManager.getCacheAdInfo("1");
        Assert.assertNotNull(getInfo);
        Assert.assertTrue(getInfo instanceof AdCacheInfo);
        ReaperLog.i(TAG, "appPostId = " + ((AdCacheInfo) getInfo).getAppPostId());
    }
}

package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.tracker.AdParam;
import com.fighter.tracker.EventActionParam;
import com.fighter.tracker.EventCacheDisplayParam;
import com.fighter.tracker.EventClickParam;
import com.fighter.tracker.EventDisPlayParam;
import com.fighter.tracker.Tracker;
import com.fighter.tracker.TrackerEventType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;


@RunWith(AndroidJUnit4.class)
public class TrackerTest {
    private static final String TAG = TrackerTest.class.getSimpleName();
    private Tracker tTracker;
    private Context mContext;
    @Test
    public void useGetTracker() throws Exception {
        tTracker = Tracker.getTracker();
        Assert.assertNotNull(tTracker);
        Log.d(TAG, "tracker = " + tTracker);
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void useInit() throws Exception {
        tTracker = Tracker.getTracker();
        mContext = InstrumentationRegistry.getTargetContext();
        tTracker.init(mContext);
        Class <?> clz = Class.forName("com.fighter.tracker.DeviceParam");
        Field macField = clz.getDeclaredField("mac");
        macField.setAccessible(true);
        String mac = (String) macField.get(null);
        Log.d(TAG, "device param:mac = " + mac);
        Assert.assertNotNull(mac);

        Field m1Field = clz.getDeclaredField("m1");
        m1Field.setAccessible(true);
        String m1 = (String) m1Field.get(null);
        Log.d(TAG, "device param:m1 = " + m1);
        Assert.assertNotNull(m1);

        Field brandField = clz.getDeclaredField("brand");
        brandField.setAccessible(true);
        String brand = (String) brandField.get(null);
        Log.d(TAG, "device param:brand = " + brand);
        Assert.assertNotNull(brand);

        Field solutionField = clz.getDeclaredField("solution");
        solutionField.setAccessible(true);
        String solution = (String) solutionField.get(null);
        Log.d(TAG, "device param:solution = " + solution);
        Assert.assertNotNull(solution);

        Field modelField = clz.getDeclaredField("d_model");
        modelField.setAccessible(true);
        String d_model = (String) modelField.get(null);
        Log.d(TAG, "device param:d_model = " + d_model);
        Assert.assertNotNull(d_model);

        Field screenField = clz.getDeclaredField("screen");
        screenField.setAccessible(true);
        String screen = (String) screenField.get(null);
        Log.d(TAG, "device param:screen = " + screen);
        Assert.assertNotNull(screen);
    }

    @Test
    public void useOnEvent() throws Exception {
        tTracker = Tracker.getTracker();
        mContext = InstrumentationRegistry.getTargetContext();
        AdParam param = new AdParam();
        param.ad_num = String.valueOf(1);
        param.ad_posid = String.valueOf(10);
        param.ad_source = "gtk";
        param.ad_type = "banner";
        param.app_pkg = "com.fighter.test";
        tTracker.onEvent(mContext, "ad_display", param);

        EventDisPlayParam param1 = new EventDisPlayParam();
        param1.ad_num = String.valueOf(1);
        param1.ad_posid = String.valueOf(10);
        param1.ad_source = "gtk";
        param1.ad_type = "banner";
        param1.app_pkg = "com.fighter.test";
        param1.ad_fail_srcs = "gdt";
        tTracker.onEvent(mContext, TrackerEventType.AD_DISPLAY_EVENT, param1);

        EventClickParam param2 = new EventClickParam();
        param2.ad_num = String.valueOf(1);
        param2.ad_posid = String.valueOf(10);
        param2.ad_source = "gtk";
        param2.ad_type = "banner";
        param2.app_pkg = "com.fighter.test";
        param2.click_pos = "100,100";
        tTracker.onEvent(mContext, TrackerEventType.AD_CLICK_EVENT, param1);

        EventActionParam param3 = new EventActionParam();
        param3.ad_num = String.valueOf(1);
        param3.ad_posid = String.valueOf(10);
        param3.ad_source = "gtk";
        param3.ad_type = "banner";
        param3.app_pkg = "com.fighter.test";
        param3.act_type = TrackerEventType.ACTION_TYPE_BEGIN;
        tTracker.onEvent(mContext, TrackerEventType.AD_ACTION_EVENT, param3);

        EventCacheDisplayParam param4 = new EventCacheDisplayParam();
        param4.ad_num = String.valueOf(1);
        param4.ad_posid = String.valueOf(10);
        param4.ad_source = "gtk";
        param4.ad_type = "banner";
        param4.app_pkg = "com.fighter.test";
        param4.cache_times = "";
        tTracker.onEvent(mContext, TrackerEventType.AD_CACHE_DISPLAY_EVENT, param4);

    }
}

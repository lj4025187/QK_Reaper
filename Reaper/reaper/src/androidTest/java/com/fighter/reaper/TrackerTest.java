package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.common.utils.ReaperLog;
import com.fighter.tracker.EventActionParam;
import com.fighter.tracker.EventClickParam;
import com.fighter.tracker.EventDisPlayParam;
import com.fighter.tracker.EventDownLoadFail;
import com.fighter.tracker.Tracker;
import com.fighter.tracker.TrackerEventType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;


@RunWith(AndroidJUnit4.class)
public class TrackerTest {
    private static final String TAG = TrackerTest.class.getSimpleName();
    @Test
    public void useTracker() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Tracker tTracker = Tracker.getTracker();
        tTracker.init(context);
        Class <?> clz = Class.forName("com.fighter.tracker.DeviceParam");
        Field macField = clz.getDeclaredField("mac");
        macField.setAccessible(true);
        String mac = (String) macField.get(null);
        ReaperLog.i(TAG, "device param:mac = " + mac);

        Field m1Field = clz.getDeclaredField("m1");
        m1Field.setAccessible(true);
        String m1 = (String) m1Field.get(null);
        ReaperLog.i(TAG, "device param:m1 = " + m1);

        Field brandField = clz.getDeclaredField("brand");
        brandField.setAccessible(true);
        String brand = (String) brandField.get(null);
        ReaperLog.i(TAG, "device param:brand = " + brand);
        Assert.assertNotNull(brand);

        Field solutionField = clz.getDeclaredField("solution");
        solutionField.setAccessible(true);
        String solution = (String) solutionField.get(null);
        ReaperLog.i(TAG, "device param:solution = " + solution);
        Assert.assertNotNull(solution);

        Field modelField = clz.getDeclaredField("d_model");
        modelField.setAccessible(true);
        String d_model = (String) modelField.get(null);
        ReaperLog.i(TAG, "device param:d_model = " + d_model);
        Assert.assertNotNull(d_model);

        Field screenField = clz.getDeclaredField("screen");
        screenField.setAccessible(true);
        String screen = (String) screenField.get(null);
        ReaperLog.i(TAG, "device param:screen = " + screen);
        Assert.assertNotNull(screen);

        Field channelField = clz.getDeclaredField("channel");
        channelField.setAccessible(true);
        String channel = (String) channelField.get(null);
        ReaperLog.i(TAG, "device param:channel = " + channel);
        Assert.assertNotNull(channel);

        Field languageField = clz.getDeclaredField("language");
        languageField.setAccessible(true);
        String language = (String) languageField.get(null);
        ReaperLog.i(TAG, "device param:language = " + language);
        Assert.assertNotNull(language);

        EventDisPlayParam param1 = new EventDisPlayParam();
        param1.ad_num = 1;
        param1.ad_posid = 10001;
        param1.ad_source = "gtk";
        param1.ad_type = "banner";
        param1.app_pkg = "com.fighter.test";
        param1.result = "ok";
        param1.reason = "";
        tTracker.trackDisplayEvent(context, param1);

        EventClickParam param2 = new EventClickParam();
        param2.ad_num = 1;
        param2.ad_posid = 10002;
        param2.ad_source = "gtk";
        param2.ad_type = "banner";
        param2.app_pkg = "com.fighter.test";
        param2.click_pos = "(100,100)";
        tTracker.trackClickEvent(context, param2);

        EventActionParam param3 = new EventActionParam();
        param3.ad_num = 1;
        param3.ad_posid = 10003;
        param3.ad_source = "gtk";
        param3.ad_type = "banner";
        param3.app_pkg = "com.fighter.test";
        param3.act_type = TrackerEventType.AD_ACTION_TYPE_BEGIN;
        tTracker.trackActionEvent(context, param3);

        EventDownLoadFail param4 = new EventDownLoadFail();
        param4.ad_num = 1;
        param4.ad_posid = 10004;
        param4.ad_source = "gtk";
        param4.ad_type = "banner";
        param4.app_pkg = "com.fighter.test";
        param4.reason = "network_timeout";
        tTracker.trackDownloadEvent(context, param4);
    }
}

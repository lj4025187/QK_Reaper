package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.common.utils.ReaperLog;
import com.fighter.tracker.EventActionParam;
import com.fighter.tracker.EventClickParam;
import com.fighter.tracker.EventDisPlayParam;
import com.fighter.tracker.EventDownLoadParam;
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
        Class <?> clz = Class.forName("com.fighter.tracker.CommonParam");
        Field macField = clz.getDeclaredField("mac");
        macField.setAccessible(true);
        String mac = (String) macField.get(null);
        ReaperLog.i(TAG, "device param:mac = " + mac);
        Assert.assertNotNull(mac);
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

        Field languageField = clz.getDeclaredField("lang");
        languageField.setAccessible(true);
        String language = (String) languageField.get(null);
        ReaperLog.i(TAG, "device param:language = " + language);
        Assert.assertNotNull(language);

        EventDisPlayParam param1 = new EventDisPlayParam();
        param1.ad_num = 1;
        param1.ad_appid = 12222;
        param1.ad_posid = 10001;
        param1.ad_source = "gtk";
        param1.ad_type = "banner";
        param1.app_pkg = "com.fighter.test";
        param1.result = "ok";
        param1.reason = "";
        ReaperLog.i(TAG, "EventDisPlayParam = " + param1);
        tTracker.trackDisplayEvent(context, param1);

        EventClickParam param2 = new EventClickParam();
        param2.ad_num = 1;
        param2.ad_appid = 12222;
        param2.ad_posid = 10002;
        param2.ad_source = "gtk";
        param2.ad_type = "banner";
        param2.app_pkg = "com.fighter.test";
        param2.click_pos = "(100*100)";
        ReaperLog.i(TAG, "EventClickParam = " + param2);
        tTracker.trackClickEvent(context, param2);

        EventActionParam param3 = new EventActionParam();
        param3.ad_num = 1;
        param3.ad_appid = 12222;
        param3.ad_posid = 10003;
        param3.ad_source = "gtk";
        param3.ad_type = "banner";
        param3.app_pkg = "com.fighter.test";
        param3.act_type = TrackerEventType.AD_ACTION_TYPE_BEGIN;
        ReaperLog.i(TAG, "EventActionParam = " + param3);
        tTracker.trackActionEvent(context, param3);

        EventActionParam param4 = new EventActionParam();
        param4.ad_num = 1;
        param4.ad_appid = 12222;
        param4.ad_posid = 10003;
        param4.ad_source = "gtk";
        param4.ad_type = "banner";
        param4.app_pkg = "com.fighter.test";
        param4.act_type = TrackerEventType.AD_ACTION_TYPE_END;
        ReaperLog.i(TAG, "EventActionParam = " + param4);
        tTracker.trackActionEvent(context, param4);

        EventActionParam param5 = new EventActionParam();
        param5.ad_num = 1;
        param5.ad_appid = 12222;
        param5.ad_posid = 10003;
        param5.ad_source = "gtk";
        param5.ad_type = "banner";
        param5.app_pkg = "com.fighter.test";
        param5.act_type = TrackerEventType.AD_ACTION_TYPE_INSTALL;
        ReaperLog.i(TAG, "EventActionParam = " + param5);
        tTracker.trackActionEvent(context, param5);

        EventDownLoadParam param6 = new EventDownLoadParam();
        param6.ad_num = 1;
        param6.ad_appid = 12222;
        param6.ad_posid = 10004;
        param6.ad_source = "gtk";
        param6.ad_type = "banner";
        param6.app_pkg = "com.fighter.test";
        param6.reason = "network_timeout";
        ReaperLog.i(TAG, "EventDownLoadParam = " + param6);
        tTracker.trackDownloadEvent(context, param6);
    }
}

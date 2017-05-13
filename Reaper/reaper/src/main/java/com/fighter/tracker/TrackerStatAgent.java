package com.fighter.tracker;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;
import com.fighter.reaper.BuildConfig;
import com.fighter.reaper.BumpVersion;
import com.qihoo.sdk.report.QHConfig;
import com.qihoo.sdk.report.QHStatAgent;
import com.qihoo.sdk.report.QHStatAgent.ExtraTagIndex;
import com.qihoo.sdk.report.QHStatAgent.DataUploadLevel;
import com.qihoo.sdk.report.QHStatAgent.SamplingPlan;
import com.qihoo.sdk.report.ReportServerAddress;

import java.util.HashMap;

/**
 * This Util is the connection with QHStatAgent.jar
 * <p>
 * Created by LiuJia on 2017/5/11.
 */

public class TrackerStatAgent {

    private final static String TAG = TrackerStatAgent.class.getSimpleName();
    private final static String REAPER_AGENT_KEY = "dd458505749b2941217ddd59394240e8";
    //switch for QHStatAgent function
    private final static Boolean SWITCH_OPEN = true;
    //switch for error exception upload
    private final static boolean CATCH_ERR = true;

    private static Context sContext;

    /**
     * @param application
     */
    public static void init(Application application) {
        if (!SWITCH_OPEN || application == null) {
            ReaperLog.e(TAG, " application is null QHAgent init failed");
            return;
        }
        init(application.getApplicationContext());
    }

    /**
     * Init QHStatAgent configuration here
     *
     * @param context
     */
    public static void init(Context context) {
        if (!SWITCH_OPEN || context == null) {
            return;
        }
        sContext = context;
        //设置SDK类的产品AppKey，请在init之前设置，以免生成文件名时取不到appkey。
        QHConfig.setAppkey(context, REAPER_AGENT_KEY);
        //设置SDK类的产品版本号。SDK类的产品必须使用（因为如果不设置的话，自动获取到的版本会是app的版本号）
        QHConfig.setVersionName(BumpVersion.value());
        //设置保存的文件名使用AppKey，SDK类的产品必须使用。
        QHConfig.setFileNameUseAppkey(true);
        //设置打点服务器
        //QHConfig.setReportServer(new ReportServerAddress("http://g.s.360.cn", "http://gf.s.360.cn", "http://gc.s.360.cn"));
        //初始化统计SDK
        QHStatAgent.init(context);
        //设置分发渠道，如手机助手；或者可以填写你们分给App的Id之类的，用以区分这些数据是由谁带来的
        QHStatAgent.setChannel(context, "");
        QHStatAgent.onError(context);
        QHStatAgent.setLoggingEnabled(BuildConfig.DEBUG);
    }

    /**
     * @param context
     * @param tag
     * @deprecated Please use {@link #setExtraTag} instead.
     */
    public static void setTags(Context context, String tag) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "setTags");
        QHStatAgent.setTags(context == null ? sContext : context, tag);
    }

    /**
     * @param context
     * @param tagValue
     * @param index
     */
    public static void setExtraTag(Context context, String tagValue, ExtraTagIndex index) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, " ExtraTagIndex " + index + " setTags " + tagValue);
        QHStatAgent.setExtraTag(context == null ? sContext : context, tagValue, index);
    }

    /**
     * @param context
     */
    public static void onResume(Context context) {
        if (!SWITCH_OPEN)
            return;
        QHStatAgent.onResume(context == null ? sContext : context);
    }

    /**
     * @param context
     */
    public static void onPause(Context context) {
        if (!SWITCH_OPEN)
            return;
        QHStatAgent.onPause(context == null ? sContext : context);
    }

    /**
     * @param eventId 事件编号 {@link TrackerEventType}
     */
    public static void onEvent(String eventId) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "onEvent");
        onEvent(sContext, eventId);
    }

    /**
     * @param context
     * @param eventId
     */
    public static void onEvent(Context context, String eventId) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "onEvent");
        onEvent(context == null ? sContext : context, eventId, null);
    }

    /**
     * @param context
     * @param eventId
     * @param times   该事件执行次数
     */
    public static void onEvent(Context context, String eventId, int times) {
        if (!SWITCH_OPEN)
            return;
        onEvent(context == null ? sContext : context, eventId, null, times);
    }

    /**
     * @param context
     * @param eventId
     * @param hashMap 扩展的自定义属性
     */
    public static void onEvent(Context context, String eventId, HashMap hashMap) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "onEvent three params contains HashMap");
        onEvent(context == null ? sContext : context, eventId, hashMap, 1);
    }

    /**
     * @param context
     * @param eventId
     * @param hashMap 扩展的自定义属性
     * @param acc     事件次数
     */
    public static void onEvent(Context context, String eventId, HashMap hashMap, int acc) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "onEvent six params contains HashMap");
        QHStatAgent.onEvent(context == null ? sContext : context, eventId, hashMap, acc, DataUploadLevel.L9, SamplingPlan.B);
    }

    /**
     * 状态、功能开关类型的事件
     *
     * @param context
     * @param eventId 事件编号 {@link TrackerEventType}
     * @param status  当前状态
     */
    public static void onStatusEvent(Context context, String eventId, int status) {
        if (!SWITCH_OPEN)
            return;
        ReaperLog.i(TAG, "onStatusEvent");
        onEvent(context == null ? sContext : context, eventId, status);
    }

    /**
     * 用来对错误信息进行打点
     *
     * @param context
     * @param exception 错误的Exception
     * @param errorType 错误的类型
     */
    public static void onError(Context context, Exception exception, String errorType) {
        if (!SWITCH_OPEN || !CATCH_ERR || exception == null)
            return;
        if (TextUtils.isEmpty(errorType)) errorType = "unKnow error";
        QHStatAgent.onError(context == null ? sContext : context, exception.toString(), errorType);
    }
}

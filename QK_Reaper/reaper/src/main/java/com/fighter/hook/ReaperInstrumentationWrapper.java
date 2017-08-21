package com.fighter.hook;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;

import java.util.List;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperInstrumentationWrapper extends Instrumentation {
    private static final String TAG = "ReaperInstrumentationWrapper";
    private static final String DATA_TYPE_ARCHIVE = "application/vnd.android.package-archive";

    private final ReaperInstrumentationHook hackInstrumentation;

    private final Instrumentation originInstrumentation;

    public ReaperInstrumentationWrapper(Instrumentation instrumentation) {
        this.hackInstrumentation = new ReaperInstrumentationHook(instrumentation);
        this.originInstrumentation = instrumentation;
    }

    // Override newActivity　method to proxy Activity, such as AkAdActivity
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ReaperLog.i(TAG, "className = " + className + ", Intent = " + intent);
        ReaperLog.i(TAG, "\n");
        Intent akadIntent = intent;
        ClassLoader akadClassLoader = cl;
        if (className.equals(ProxyActivityName.PROXY_ACTIVITY)) {
            className = ReaperGlobal.getClassName();
            akadClassLoader = ReaperInstrumentationWrapper.class.getClassLoader();
            ReaperLog.i(TAG, "new wrapper activity");
            ReaperLog.i(TAG, "\n");
        }
        ReaperLog.i(TAG, "fix className = " + className + ", Intent = " + intent + ", akadIntent = " + akadIntent);
        ReaperLog.i(TAG, "\n");
        Bundle extras = intent.getExtras();
        if(extras != null)
            ReaperLog.i(TAG, " intent extras " + extras.toString());
        ReaperLog.i(TAG, "\n");
        return originInstrumentation.newActivity(akadClassLoader, className, akadIntent);
    }

    @Override
    public void onCreate(Bundle arguments) {
        originInstrumentation.onCreate(arguments);
    }

    @Override
    public void start() {
        originInstrumentation.start();
    }

    @Override
    public void onStart() {
        originInstrumentation.onStart();
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        return originInstrumentation.onException(obj, e);
    }

    @Override
    public void sendStatus(int resultCode, Bundle results) {
        originInstrumentation.sendStatus(resultCode, results);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        originInstrumentation.finish(resultCode, results);
    }

    @Override
    public void setAutomaticPerformanceSnapshots() {
        originInstrumentation.setAutomaticPerformanceSnapshots();
    }

    @Override
    public void startPerformanceSnapshot() {
        originInstrumentation.startPerformanceSnapshot();
    }

    @Override
    public void endPerformanceSnapshot() {
        originInstrumentation.endPerformanceSnapshot();
    }

    @Override
    public void onDestroy() {
        originInstrumentation.onDestroy();
    }

    @Override
    public Context getContext() {
        return originInstrumentation.getContext();
    }

    @Override
    public ComponentName getComponentName() {
        return originInstrumentation.getComponentName();
    }

    @Override
    public Context getTargetContext() {
        return originInstrumentation.getTargetContext();
    }

    @Override
    public boolean isProfiling() {
        return originInstrumentation.isProfiling();
    }

    @Override
    public void startProfiling() {
        originInstrumentation.startProfiling();
    }

    @Override
    public void stopProfiling() {
        originInstrumentation.stopProfiling();
    }

    @Override
    public void setInTouchMode(boolean inTouch) {
        originInstrumentation.setInTouchMode(inTouch);
    }

    @Override
    public void waitForIdle(Runnable recipient) {
        originInstrumentation.waitForIdle(recipient);
    }

    @Override
    public void waitForIdleSync() {
        originInstrumentation.waitForIdleSync();
    }

    @Override
    public void runOnMainSync(Runnable runner) {
        originInstrumentation.runOnMainSync(runner);
    }

    @Override
    public Activity startActivitySync(Intent intent) {
        return originInstrumentation.startActivitySync(intent);
    }

    @Override
    public void addMonitor(ActivityMonitor monitor) {
        originInstrumentation.addMonitor(monitor);
    }

    @Override
    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        return originInstrumentation.addMonitor(filter, result, block);
    }

    @Override
    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        return originInstrumentation.addMonitor(cls, result, block);
    }

    @Override
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        return originInstrumentation.checkMonitorHit(monitor, minHits);
    }

    @Override
    public Activity waitForMonitor(ActivityMonitor monitor) {
        return originInstrumentation.waitForMonitor(monitor);
    }

    @Override
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        return originInstrumentation.waitForMonitorWithTimeout(monitor, timeOut);
    }

    @Override
    public void removeMonitor(ActivityMonitor monitor) {
        originInstrumentation.removeMonitor(monitor);
    }

    @Override
    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        return originInstrumentation.invokeMenuActionSync(targetActivity, id, flag);
    }

    @Override
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        return originInstrumentation.invokeContextMenuAction(targetActivity, id, flag);
    }

    @Override
    public void sendStringSync(String text) {
        originInstrumentation.sendStringSync(text);
    }

    @Override
    public void sendKeySync(KeyEvent event) {
        originInstrumentation.sendKeySync(event);
    }

    @Override
    public void sendKeyDownUpSync(int key) {
        originInstrumentation.sendKeyDownUpSync(key);
    }

    @Override
    public void sendCharacterSync(int keyCode) {
        originInstrumentation.sendCharacterSync(keyCode);
    }

    @Override
    public void sendPointerSync(MotionEvent event) {
        originInstrumentation.sendPointerSync(event);
    }

    @Override
    public void sendTrackballEventSync(MotionEvent event) {
        originInstrumentation.sendTrackballEventSync(event);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return originInstrumentation.newApplication(cl, className, context);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        originInstrumentation.callApplicationOnCreate(app);
    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        return originInstrumentation.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        originInstrumentation.callActivityOnCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        originInstrumentation.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        originInstrumentation.callActivityOnDestroy(activity);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        originInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        originInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        originInstrumentation.callActivityOnPostCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        originInstrumentation.callActivityOnPostCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        originInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        originInstrumentation.callActivityOnStart(activity);
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        originInstrumentation.callActivityOnRestart(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        originInstrumentation.callActivityOnResume(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        originInstrumentation.callActivityOnStop(activity);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        originInstrumentation.callActivityOnSaveInstanceState(activity, outState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        originInstrumentation.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        originInstrumentation.callActivityOnPause(activity);
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        originInstrumentation.callActivityOnUserLeaving(activity);
    }

    @Override
    public void startAllocCounting() {
        originInstrumentation.startAllocCounting();
    }

    @Override
    public void stopAllocCounting() {
        originInstrumentation.stopAllocCounting();
    }

    @Override
    public Bundle getAllocCounts() {
        return originInstrumentation.getAllocCounts();
    }

    @Override
    public Bundle getBinderCounts() {
        return originInstrumentation.getBinderCounts();
    }

    @Override
    public UiAutomation getUiAutomation() {
        return originInstrumentation.getUiAutomation();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public UiAutomation getUiAutomation(int flags) {
        return originInstrumentation.getUiAutomation(flags);
    }

    @NoProguard
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options) {
        ReaperLog.i(TAG, "who = " + who + ", IBinder = " + contextThread + ", token = " + token + ", target = " + target
                        + ", Intent = " + intent + ", requestCode = " + requestCode + ", options = " + options);
        ReaperLog.i(TAG, "\n");
        Intent proxyIntent = new Intent();
        proxyIntent = intent;
        ComponentName componentName = intent.getComponent();
        if (componentName != null) {
            String className = componentName.getClassName();
            if (className.equals(ProxyActivityName.AKAD_ACTIVITY)) {
                proxyIntent.setFlags(intent.getFlags());
                ComponentName proxyComponent = new ComponentName(intent.getComponent().getPackageName(), ProxyActivityName.PROXY_ACTIVITY);
                proxyIntent.setComponent(proxyComponent);
                proxyIntent.putExtras(intent.getExtras());
                ReaperGlobal.setClassName(className);
                ReaperLog.i(TAG, "start proxy activity");
                ReaperLog.i(TAG, "\n");
            }
        }
        if(TextUtils.equals(intent.getType(), DATA_TYPE_ARCHIVE)) {
            proxyIntent = intent;
            proxyIntent.setDataAndType(generateUri(who, intent.getData()), DATA_TYPE_ARCHIVE);
            ReaperLog.i(TAG, "who = " + who + ", IBinder = " + contextThread + ", token = " + token + ", target = " + target
                    + ", Intent = " + intent + ", requestCode = " + requestCode + ", options = " + options);
        }

        return hackInstrumentation.execStartActivity(who, contextThread, token, target,
                proxyIntent, requestCode, options);
    }

    /**
     * fake the uri authorities
     *
     * @param context
     * @param origin
     * @return
     */
    private Uri generateUri(Context context, Uri origin) {
        ReaperLog.i(TAG, "uri origin " + origin);
        if(TextUtils.isEmpty(origin.toString()) || Build.VERSION.SDK_INT < 24)
            return origin;
        if(!TextUtils.isEmpty(origin.getAuthority()))
            return origin;
        List<String> pathSegments = origin.getPathSegments();
        StringBuilder path = new StringBuilder("content://" + context.getPackageName()
                + ".reaper.provider.ReaperProxyProvider");
        if(pathSegments != null && !pathSegments.isEmpty()) {
            for(String value : pathSegments) {
                path.append("/").append(value);
            }
        }
        return Uri.parse(path.toString());
    }
}

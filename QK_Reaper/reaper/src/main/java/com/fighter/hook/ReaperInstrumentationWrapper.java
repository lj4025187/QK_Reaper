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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.fighter.common.utils.ReaperLog;
import com.qiku.proguard.annotations.NoProguard;

import java.util.List;

/**
 * instrumentation wrapper
 *
 * Created by lichen on 17-6-12.
 */

public class ReaperInstrumentationWrapper extends Instrumentation {
    private static final String TAG = "ReaperInstrumentationWrapper";
    private static final String DATA_TYPE_ARCHIVE = "application/vnd.android.package-archive";

    private final ReaperInstrumentationHook mHackInstrumentation;

    private final Instrumentation mOriginInstrumentation;

    public ReaperInstrumentationWrapper(Instrumentation instrumentation) {
        this.mHackInstrumentation = new ReaperInstrumentationHook(instrumentation);
        this.mOriginInstrumentation = instrumentation;
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
        return mOriginInstrumentation.newActivity(akadClassLoader, className, akadIntent);
    }

    @Override
    public void onCreate(Bundle arguments) {
        mOriginInstrumentation.onCreate(arguments);
    }

    @Override
    public void start() {
        mOriginInstrumentation.start();
    }

    @Override
    public void onStart() {
        mOriginInstrumentation.onStart();
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        return mOriginInstrumentation.onException(obj, e);
    }

    @Override
    public void sendStatus(int resultCode, Bundle results) {
        mOriginInstrumentation.sendStatus(resultCode, results);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        mOriginInstrumentation.finish(resultCode, results);
    }

    @Override
    public void setAutomaticPerformanceSnapshots() {
        mOriginInstrumentation.setAutomaticPerformanceSnapshots();
    }

    @Override
    public void startPerformanceSnapshot() {
        mOriginInstrumentation.startPerformanceSnapshot();
    }

    @Override
    public void endPerformanceSnapshot() {
        mOriginInstrumentation.endPerformanceSnapshot();
    }

    @Override
    public void onDestroy() {
        mOriginInstrumentation.onDestroy();
    }

    @Override
    public Context getContext() {
        return mOriginInstrumentation.getContext();
    }

    @Override
    public ComponentName getComponentName() {
        return mOriginInstrumentation.getComponentName();
    }

    @Override
    public Context getTargetContext() {
        return mOriginInstrumentation.getTargetContext();
    }

    @Override
    public boolean isProfiling() {
        return mOriginInstrumentation.isProfiling();
    }

    @Override
    public void startProfiling() {
        mOriginInstrumentation.startProfiling();
    }

    @Override
    public void stopProfiling() {
        mOriginInstrumentation.stopProfiling();
    }

    @Override
    public void setInTouchMode(boolean inTouch) {
        mOriginInstrumentation.setInTouchMode(inTouch);
    }

    @Override
    public void waitForIdle(Runnable recipient) {
        mOriginInstrumentation.waitForIdle(recipient);
    }

    @Override
    public void waitForIdleSync() {
        mOriginInstrumentation.waitForIdleSync();
    }

    @Override
    public void runOnMainSync(Runnable runner) {
        mOriginInstrumentation.runOnMainSync(runner);
    }

    @Override
    public Activity startActivitySync(Intent intent) {
        return mOriginInstrumentation.startActivitySync(intent);
    }

    @Override
    public void addMonitor(ActivityMonitor monitor) {
        mOriginInstrumentation.addMonitor(monitor);
    }

    @Override
    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        return mOriginInstrumentation.addMonitor(filter, result, block);
    }

    @Override
    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        return mOriginInstrumentation.addMonitor(cls, result, block);
    }

    @Override
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        return mOriginInstrumentation.checkMonitorHit(monitor, minHits);
    }

    @Override
    public Activity waitForMonitor(ActivityMonitor monitor) {
        return mOriginInstrumentation.waitForMonitor(monitor);
    }

    @Override
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        return mOriginInstrumentation.waitForMonitorWithTimeout(monitor, timeOut);
    }

    @Override
    public void removeMonitor(ActivityMonitor monitor) {
        mOriginInstrumentation.removeMonitor(monitor);
    }

    @Override
    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        return mOriginInstrumentation.invokeMenuActionSync(targetActivity, id, flag);
    }

    @Override
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        return mOriginInstrumentation.invokeContextMenuAction(targetActivity, id, flag);
    }

    @Override
    public void sendStringSync(String text) {
        mOriginInstrumentation.sendStringSync(text);
    }

    @Override
    public void sendKeySync(KeyEvent event) {
        mOriginInstrumentation.sendKeySync(event);
    }

    @Override
    public void sendKeyDownUpSync(int key) {
        mOriginInstrumentation.sendKeyDownUpSync(key);
    }

    @Override
    public void sendCharacterSync(int keyCode) {
        mOriginInstrumentation.sendCharacterSync(keyCode);
    }

    @Override
    public void sendPointerSync(MotionEvent event) {
        mOriginInstrumentation.sendPointerSync(event);
    }

    @Override
    public void sendTrackballEventSync(MotionEvent event) {
        mOriginInstrumentation.sendTrackballEventSync(event);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return mOriginInstrumentation.newApplication(cl, className, context);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        mOriginInstrumentation.callApplicationOnCreate(app);
    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        return mOriginInstrumentation.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        mOriginInstrumentation.callActivityOnCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        mOriginInstrumentation.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        mOriginInstrumentation.callActivityOnDestroy(activity);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        mOriginInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        mOriginInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        mOriginInstrumentation.callActivityOnPostCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        mOriginInstrumentation.callActivityOnPostCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        mOriginInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        mOriginInstrumentation.callActivityOnStart(activity);
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        mOriginInstrumentation.callActivityOnRestart(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        mOriginInstrumentation.callActivityOnResume(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        mOriginInstrumentation.callActivityOnStop(activity);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        mOriginInstrumentation.callActivityOnSaveInstanceState(activity, outState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        mOriginInstrumentation.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        mOriginInstrumentation.callActivityOnPause(activity);
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        mOriginInstrumentation.callActivityOnUserLeaving(activity);
    }

    @Override
    public void startAllocCounting() {
        mOriginInstrumentation.startAllocCounting();
    }

    @Override
    public void stopAllocCounting() {
        mOriginInstrumentation.stopAllocCounting();
    }

    @Override
    public Bundle getAllocCounts() {
        return mOriginInstrumentation.getAllocCounts();
    }

    @Override
    public Bundle getBinderCounts() {
        return mOriginInstrumentation.getBinderCounts();
    }

    @Override
    public UiAutomation getUiAutomation() {
        return mOriginInstrumentation.getUiAutomation();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public UiAutomation getUiAutomation(int flags) {
        return mOriginInstrumentation.getUiAutomation(flags);
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

        return mHackInstrumentation.execStartActivity(who, contextThread, token, target,
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

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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.RefInvoker;
import com.qiku.proguard.annotations.NoProguard;


/**
 * The Instrumentation is used to replace origin instrumentation in ActivityThread
 *
 * Created by lichen on 17-6-12.
 */

public class ReaperInstrumentation extends Instrumentation {
    private static final String TAG = "ReaperInstrumentation";


    private final Instrumentation mOriginInstrumentation;

    public ReaperInstrumentation(Instrumentation instrumentation) {
        this.mOriginInstrumentation = instrumentation;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        String oriClassName = className; // for log

        if (ComponentProxyMap.PROXY_ACTIVITY.equals(className)) {
            className = ComponentProxyMap.getRealActivityByProxy(ComponentProxyMap.PROXY_ACTIVITY);
            cl = ReaperInstrumentation.class.getClassLoader(); // ReaperClassLoader
        } else if (ComponentProxyMap.PROXY_WEB_VIEW_ACTIVITY.equals(className)) {
            className = ComponentProxyMap.getRealActivityByProxy(
                    ComponentProxyMap.PROXY_WEB_VIEW_ACTIVITY);
            cl = ReaperInstrumentation.class.getClassLoader();
        }

        if (!TextUtils.equals(oriClassName, className)) {
            ReaperLog.i(TAG, "replace activity from " + oriClassName +
                    " to " + className + " when newActivity");
        }

        return mOriginInstrumentation.newActivity(cl, className, intent);
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
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return mOriginInstrumentation.newApplication(cl, className, context);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        mOriginInstrumentation.callApplicationOnCreate(app);
    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token,
                                Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance)
            throws InstantiationException, IllegalAccessException {
        return mOriginInstrumentation.newActivity(clazz, context, token, application, intent, info,
                title, parent, id, lastNonConfigurationInstance);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        mOriginInstrumentation.callActivityOnCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle,
                                     PersistableBundle persistentState) {
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
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState,
                                                   PersistableBundle persistentState) {
        mOriginInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState,
                persistentState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        mOriginInstrumentation.callActivityOnPostCreate(activity, icicle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle,
                                         PersistableBundle persistentState) {
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
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState,
                                                PersistableBundle outPersistentState) {
        mOriginInstrumentation.callActivityOnSaveInstanceState(activity, outState,
                outPersistentState);
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
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
            Activity target, Intent intent, int requestCode, Bundle options) {

        ReaperLog.i(TAG, "who = " + who + ", IBinder = " + contextThread + ", token = " + token +
                ", target = " + target + ", Intent = " + intent + ", requestCode = " + requestCode +
                ", options = " + options);

        AKADHook.changeToProxyWhenStartAKActivity(intent);

        AKADHook.changeUriWhenCallPackageInstaller(who, intent);

        // call origin method
        Object result = RefInvoker.invokeMethod(mOriginInstrumentation,
                mOriginInstrumentation.getClass(), "execStartActivity",
                new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class },
                new Object[] { who, contextThread, token, target,
                        intent, requestCode, options });
        return (ActivityResult)result;
    }

}

package com.fighter.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.fighter.common.utils.ReaperLog;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperInstrumentationWrapper extends Instrumentation {
    private static final String TAG = ReaperInstrumentationWrapper.class.getSimpleName();

    private final ReaperInstrumentationHook hackInstrumentation;

    public ReaperInstrumentationWrapper(Instrumentation instrumentation) {
        this.hackInstrumentation = new ReaperInstrumentationHook(instrumentation);
    }

    // Override newActivity　method to proxy Activity, such as AkAdActivity
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ReaperLog.i(TAG, "classLoader = " + cl + ", className = " + className + ", Intent = " + intent);
        Intent akadIntent = intent;
        ClassLoader akadClassLoader = cl;
        if (className.equals(ProxyActivityName.PROXY_ACTIVITY)) {
            className = ReaperGlobal.getClassName();
            akadClassLoader = ReaperInstrumentationWrapper.class.getClassLoader();
        }
        ReaperLog.i(TAG, "fix classLoader = " + cl + ", className = " + className + ", Intent = " + intent + ", akadIntent = " + akadIntent);
        return super.newActivity(akadClassLoader, className, akadIntent);
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options) {
        ReaperLog.i(TAG, "who = " + who + ", IBinder = " + contextThread + ", token = " + token + ", target = " + target
                        + ", Intent = " + intent + ", requestCode = " + requestCode + ", options = " + options);
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
            }
        }
        return hackInstrumentation.execStartActivity(who, contextThread, token, target,
                proxyIntent, requestCode, options);
    }
}

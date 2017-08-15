package com.fighter.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

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

    public ReaperInstrumentationWrapper(Instrumentation instrumentation) {
        this.hackInstrumentation = new ReaperInstrumentationHook(instrumentation);
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
        return super.newActivity(akadClassLoader, className, akadIntent);
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

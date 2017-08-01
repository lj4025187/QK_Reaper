package com.fighter.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.fighter.common.utils.RefInvoker;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperInstrumentationHook {

    private static final String Method_execStartActivity = "execStartActivity";

    private Object instance;

    public ReaperInstrumentationHook(Object instance) {
        this.instance = instance;
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                                            Intent intent, int requestCode, Bundle options) {
        Object result = RefInvoker.invokeMethod(instance, android.app.Instrumentation.class.getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class }, new Object[] { who, contextThread, token, target,
                        intent, requestCode, options });

        return (Instrumentation.ActivityResult) result;
    }
}

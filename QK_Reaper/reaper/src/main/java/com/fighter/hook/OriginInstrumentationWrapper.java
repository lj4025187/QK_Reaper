package com.fighter.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.fighter.common.utils.RefInvoker;

/**
 * hook instrumentation method
 *
 * Created by lichen on 17-6-12.
 */

public class OriginInstrumentationWrapper {

    private static final String Method_execStartActivity = "execStartActivity";

    private Object mOriginInstrumentation;

    public OriginInstrumentationWrapper(Object originInstrumentation) {
        this.mOriginInstrumentation = originInstrumentation;
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                                            Intent intent, int requestCode, Bundle options) {
        Object result = RefInvoker.invokeMethod(mOriginInstrumentation, mOriginInstrumentation.getClass().getName(),
                Method_execStartActivity, new Class[] { Context.class, IBinder.class, IBinder.class, Activity.class,
                        Intent.class, int.class, Bundle.class }, new Object[] { who, contextThread, token, target,
                        intent, requestCode, options });

        return (Instrumentation.ActivityResult) result;
    }
}

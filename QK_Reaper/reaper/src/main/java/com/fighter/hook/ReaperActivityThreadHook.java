package com.fighter.hook;

import android.app.Instrumentation;
import android.content.Context;

import com.fighter.common.utils.RefInvoker;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperActivityThreadHook {

    private static final String TAG = "ReaperActivityThreadHook";

    private static final String Class_ActivityThread = "android.app.ActivityThread";
    private static final String Field_mInstrumentation = "mInstrumentation";
    private static final String Method_currentActivityThread = "currentActivityThread";
    private static final String Class_ContextImpl = "android.app.ContextImpl";
    private static final String Field_mMainThread = "mMainThread";
    private static final String Method_getImpl = "getImpl";

    private static Object sActivityThread;

    public static void hookInstrumentation() {
        if (!initActivityThread()) {
            return;
        }
        Instrumentation oriInstrumentation = getOriginInstrumentation();
        Instrumentation replaceInstrumentation = new ReaperInstrumentationWrapper(oriInstrumentation);
        setInstrumentation(replaceInstrumentation);
    }

    private static boolean initActivityThread () {
        if (sActivityThread == null) {
            sActivityThread = currentActivityThread();
            if (sActivityThread == null) {
                sActivityThread = getActivityThreadByContextImpl();
            }
        }
        return sActivityThread != null;
    }

    private static Object currentActivityThread() {
        return RefInvoker.invokeMethod(null, Class_ActivityThread,
                Method_currentActivityThread, null, null);
    }

    private static Object getActivityThreadByContextImpl() {
        Object contextImpl = RefInvoker.invokeMethod(null, Class_ContextImpl, Method_getImpl,
                new Class[]{Context.class}, new Object[]{ReaperGlobal.getContext()});
        return RefInvoker.getField(contextImpl, Class_ContextImpl, Field_mMainThread);
    }

    private static Instrumentation getOriginInstrumentation() {
        return (Instrumentation) RefInvoker.getField(sActivityThread,
                Class_ActivityThread, Field_mInstrumentation);
    }

    private static void setInstrumentation(Instrumentation instrumentation) {
        RefInvoker.setField(sActivityThread, Class_ActivityThread,
                Field_mInstrumentation, instrumentation);
    }
}

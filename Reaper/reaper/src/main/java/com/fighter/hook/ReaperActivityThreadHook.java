package com.fighter.hook;

import android.app.Instrumentation;

import com.fighter.common.utils.RefInvoker;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperActivityThreadHook {
    private static final String TAG = ReaperActivityThreadHook.class.getSimpleName();

    private static final String ClassName = "android.app.ActivityThread";

    private static final String Field_mInstrumentation = "mInstrumentation";

    private static final String Method_currentActivityThread = "currentActivityThread";

    private static ReaperActivityThreadHook reaperActivityThreadHook;

    private Object instance;

    private ReaperActivityThreadHook(Object instance) {
        this.instance = instance;
    }

    public static synchronized ReaperActivityThreadHook get() {
        if (reaperActivityThreadHook == null) {
            Object instance = currentActivityThread();
            if (instance != null) {
                reaperActivityThreadHook = new ReaperActivityThreadHook(instance);
            }
        }
        return reaperActivityThreadHook;
    }

    private static Object currentActivityThread() {
        // 从ThreadLocal中取出来的
        Object sCurrentActivityThread = RefInvoker.invokeMethod(null, ClassName,
                Method_currentActivityThread,
                (Class[]) null, (Object[]) null);

        //有些情况下上面的方法拿不到，下面再换个方法尝试一次
        if (sCurrentActivityThread == null) {
//            Object impl = ReaperContextImplHook.getImpl(ReaperGlobal.getApplication());
            Object impl = ReaperContextImplHook.getImpl(ReaperGlobal.getContext());
            if (impl != null) {
                sCurrentActivityThread = new ReaperContextImplHook(impl).getMainThread();
            }
        }
        return sCurrentActivityThread;
    }

    public static void wrapInstrumentation() {
        ReaperActivityThreadHook hackActivityThread = get();
        if (hackActivityThread != null) {
            Instrumentation originalInstrumentation = hackActivityThread.getInstrumentation();
            if (!(originalInstrumentation instanceof ReaperInstrumentationWrapper)) {
                hackActivityThread.setInstrumentation(new ReaperInstrumentationWrapper(originalInstrumentation));
            }
        }
    }

    public Instrumentation getInstrumentation() {
        return (Instrumentation) RefInvoker.getField(instance,
                ClassName, Field_mInstrumentation);
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        RefInvoker.setField(instance, ClassName,
                Field_mInstrumentation,
                instrumentation);
    }
}

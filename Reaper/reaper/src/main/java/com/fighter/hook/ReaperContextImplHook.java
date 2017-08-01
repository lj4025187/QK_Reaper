package com.fighter.hook;

import android.content.Context;

import com.fighter.common.utils.RefInvoker;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperContextImplHook {
    private static final String ClassName = "android.app.ContextImpl";

    private static final String Field_mMainThread = "mMainThread";

    private static final String Method_getImpl = "getImpl";

    private Object instance;

    public ReaperContextImplHook(Object instance) {
        this.instance = instance;
    }

    public Object getMainThread() {
        return RefInvoker.getField(instance, ClassName, Field_mMainThread);
    }

    public static Object getImpl(Object paramValues) {
        return RefInvoker.invokeMethod(null, ClassName, Method_getImpl, new Class[]{Context.class}, new Object[]{paramValues});
    }
}

package com.fighter.hook;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;

import java.lang.reflect.Field;

/**
 * Created by lichen on 17-8-15.
 */

public class ReaperHookProvider {
    private static final String TAG = "ReaperHookProvider";

    public static void hookReaperProxyProvider(Context context) {
        ReaperLog.i(TAG, "hookReaperProxyProvider");
        if (context == null)
            return;
        ClassLoader contextClassLoader = context.getClassLoader();

        try {
            Class<?> reaperProxyProvider = Class.forName("com.fighter.loader.ReaperProxyProvider", true, contextClassLoader);
            Field proxyProvider  = reaperProxyProvider.getDeclaredField("providerProxy");
            proxyProvider.setAccessible(true);
            proxyProvider.set(reaperProxyProvider.newInstance(), new ProxyProvider());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

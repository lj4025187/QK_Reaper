package com.fighter.hook;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;

import com.fighter.common.utils.ReaperLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        ReaperLog.i(TAG, "context class loader = " + contextClassLoader);

        try {
            Class<?> reaperProxyProvider = Class.forName("com.fighter.loader.ReaperProxyProvider", true, contextClassLoader);
            for (Field field : reaperProxyProvider.getDeclaredFields()) {
                ReaperLog.i(TAG, "name ===>" + field.getName());
            }
            Field proxyProvider  = reaperProxyProvider.getDeclaredField("providerProxy");
            proxyProvider.setAccessible(true);
            proxyProvider.set(getContentProvider(context), new ProxyProvider());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static ContentProvider getContentProvider(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String auth = context.getPackageName() + ".reaper.provider.ReaperProxyProvider";

        ContentProviderClient client = resolver.acquireContentProviderClient(auth);
        Class<?> clientClz = ContentProviderClient.class;
        Class<?> contentProviderClz = ContentProvider.class;
        ContentProvider ret;
        try {
            Class IContentProviderClass = Class.forName("android.content.IContentProvider");
            Field IContentProviderField = clientClz.getDeclaredField("mContentProvider");
            IContentProviderField.setAccessible(true);
            Object IContentProvider = IContentProviderField.get(client);
            Method getProviderMethod = contentProviderClz.getDeclaredMethod("coerceToLocalContentProvider", IContentProviderClass);
            getProviderMethod.setAccessible(true);
            ret = (ContentProvider) getProviderMethod.invoke(null, IContentProvider);
            return ret;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}

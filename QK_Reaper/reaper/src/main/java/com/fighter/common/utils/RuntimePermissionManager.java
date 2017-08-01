package com.fighter.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import java.util.Map;

/**
 * Created by jia on 6/19/17.
 */
public class RuntimePermissionManager {

    private final static String TAG = RuntimePermissionManager.class.getSimpleName();

    private static RuntimePermissionManager mInstance;
    private static Context sContext;

    private final static int REQUEST_CODE = 8888;

    private final static String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * private constructor
     *
     * @param context AppContext from {@link com.fighter.api.ReaperApi#init(Map)}
     */
    private RuntimePermissionManager(Context context) {
        sContext = context;
    }

    /**
     * Single instance method context can not be null
     *
     * @param context AppContext from {@link com.fighter.api.ReaperApi#init(Map)}
     * @return mInstance or null
     */
    public static RuntimePermissionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RuntimePermissionManager(context);
        }
        return mInstance;
    }

    /**
     * provide for ReaperApi to check permissions
     */
    public void requestPermissions() {
        startCheckActivity(REQUESTED_PERMISSIONS);
    }

    /**
     * should start ReaperActivity for check permissions
     * @param needPermissions no granted permissions
     */
    private static void startCheckActivity(String[] needPermissions) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("request permission only can run in MainThread!");
        }
        try {
            Class<?> reaperClass = Class.forName("com.fighter.loader.ReaperActivity");
            Intent intent = new Intent(sContext, reaperClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("needPermissions", needPermissions);
            intent.putExtra("requestCode", REQUEST_CODE);
            sContext.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

package com.fighter.loader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Proxy activity
 *
 * Created by lichen on 17-6-10.
 */

public class ReaperProxyActivity extends Activity {
    private static final String TAG = ReaperProxyActivity.class.getSimpleName();

    private static Activity mRemoteActivity;

    private Activity getRemoteActivity(String activityName) {
        if (activityName == null)
            return null;
        Activity activity = null;
        try {
            Class <?> clz = Class.forName(activityName, false, getClassLoader());
            if (clz != null) {
                activity = (Activity) clz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return activity;
    }

    private String getActivityName() {
        String activityName = null;
        Intent intent = getIntent();
        if (intent != null) {
            activityName = intent.getStringExtra("activity_name");
        }
        return activityName;
    }

    private void callLifeCycleMethod(Activity remoteActivity, String methodName, Class[] paramsType, Object[] params) {
        if (remoteActivity == null)
            return;
        Class<?> clz = remoteActivity.getClass();
        try {
            Method lifeCycleMethod = clz.getDeclaredMethod(methodName, paramsType);
            if (lifeCycleMethod == null)
                return;
            lifeCycleMethod.setAccessible(true);
            lifeCycleMethod.invoke(remoteActivity, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRemoteActivity = getRemoteActivity(getActivityName());
        callLifeCycleMethod(mRemoteActivity, "onCreate", new Class[] {Bundle.class}, new Object[] {savedInstanceState});
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        callLifeCycleMethod(mRemoteActivity, "onStart", null, null);
        super.onStart();
    }

    @Override
    protected void onRestart() {
        callLifeCycleMethod(mRemoteActivity, "onRestart", null, null);
        super.onRestart();
    }

    @Override
    protected void onResume() {
        callLifeCycleMethod(mRemoteActivity, "onResume", null, null);
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        callLifeCycleMethod(mRemoteActivity, "onNewIntent", new Class[]{Intent.class}, new Object[]{intent});
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        callLifeCycleMethod(mRemoteActivity, "onPause", null, null);
        super.onPause();
    }

    @Override
    protected void onStop() {
        callLifeCycleMethod(mRemoteActivity, "onStop", null, null);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        callLifeCycleMethod(mRemoteActivity, "onDestroy", null, null);
        super.onDestroy();
    }
}

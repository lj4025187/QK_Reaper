package com.fighter.hook;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.qiku.proguard.annotations.NoProguard;

/**
 * Created by lichen on 17-6-12.
 */
@NoProguard
public class ReaperGlobal {
    private static Application sApplication;
    private static Context sContext;
    private static Intent sIntent;
    private static String sClassName;

    public static Application getApplication() {
        return sApplication;
    }

    public static void setApplication(Application application) {
        sApplication = application;
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static Intent getIntent() {
        return sIntent;
    }

    public static void setIntent(Intent sIntent) {
        ReaperGlobal.sIntent = sIntent;
    }

    public static String getClassName() {
        return sClassName;
    }

    public static void setClassName(String sClassName) {
        ReaperGlobal.sClassName = sClassName;
    }
}

package com.fighter.hook;

import android.app.Application;
import android.content.Intent;

/**
 * Created by lichen on 17-6-12.
 */

public class ReaperGlobal {
    private static Application sApplication;
    private static Intent sIntent;
    private static String sClassName;

    public static Application getApplication() {
        return sApplication;
    }

    public static void setApplication(Application application) {
        sApplication = application;
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

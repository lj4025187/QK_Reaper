package com.fighter.hook;

import android.content.Context;

import com.qiku.proguard.annotations.NoProguard;

/**
 * Created by lichen on 17-6-12.
 */
@NoProguard
public class ReaperGlobal {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }
}

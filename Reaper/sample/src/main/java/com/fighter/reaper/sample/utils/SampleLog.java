package com.fighter.reaper.sample.utils;

import android.util.Log;

/**
 * Created by Administrator on 2017/5/23.
 */

public class SampleLog {

    public static final String TAG = "ReaperSample";
    public static boolean DEBUG_LOG = true;

    public static void i(String msg) {
        if (!DEBUG_LOG)
            return;
        Log.i(TAG, msg);
    }

    public static void i(String subTag, String msg) {
        if (!DEBUG_LOG)
            return;
        Log.i(TAG, "[" + subTag + "] ==> " + msg);
    }

    public static void e(String msg) {
        if (!DEBUG_LOG)
            return;
        Log.e(TAG, msg);
    }

    public static void e(String subTag, String msg) {
        if (!DEBUG_LOG)
            return;
        Log.e(TAG, "[" + subTag + "] ==> " + msg);
    }
}

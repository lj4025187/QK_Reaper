package com.fighter.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoaderLog {

    public static final String TAG = "Reaper";
    private static final String LOCAL_DIR = Environment.getExternalStorageDirectory() + "/Reaper";
    private static final String LOCAL_LOG_DIR = LOCAL_DIR + "/logs";
    private static final boolean DEBUG_LOG = true;
    private static final boolean RECORD_LOG = true;
    private static long sStartTime = 0;

    public static void i(String msg) {
        if (!DEBUG_LOG)
            return;
        Log.i(TAG, msg);
        if (RECORD_LOG)
            writeLocalLog("I ", msg);
    }

    public static void i(String subTag, String msg) {
        if (!DEBUG_LOG)
            return;
        Log.i(TAG, "[" + subTag + "] ==> " + msg);
        if (RECORD_LOG)
            writeLocalLog("I ", msg);
    }

    public static void e(String msg) {
        if (!DEBUG_LOG)
            return;
        Log.e(TAG, msg);
        if (RECORD_LOG)
            writeLocalLog("E ", msg);
    }

    public static void e(String subTag, String msg) {
        if (!DEBUG_LOG)
            return;
        Log.e(TAG, "[" + subTag + "] ==> " + msg);
        if (RECORD_LOG)
            writeLocalLog("E ", msg);
    }

    //for print list
    public static <T> void printList(List<T> list) {
        if (!DEBUG_LOG)
            return;
        if (list == null) return;
        for (T t : list) {
            e(TAG, "from list : " + t);
        }
    }

    public static void startTime(String msg) {
        if (!DEBUG_LOG)
            return;
        sStartTime = System.currentTimeMillis();
        String message = "start count time : " + msg;
        e(message);
        if (RECORD_LOG)
            writeLocalLog("TIME START : ", message);
    }

    public static void endTime(String msg) {
        if (!DEBUG_LOG)
            return;
        String message = msg + " used time : " + (System.currentTimeMillis() - sStartTime);
        e(message);
        if (RECORD_LOG)
            writeLocalLog("TIME END : ", message);
    }

    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    private static void writeLocalLog(String type, String msg) {
        String currentDate = getCurrentDate();

        File file = new File(LOCAL_LOG_DIR + File.separator + "LoaderLog-" + currentDate + ".txt");
        if(!file.exists()) {
            if(createLocalLogFile(file.toString())) {
                writeLocalLog(file, type, msg);
            }
        } else {
            writeLocalLog(file, type, msg);
        }

    }

    private static void writeLocalLog(File file, String type, String msg) {
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true), type.length()+msg.length()+1);
            bufferedWriter.write(type);
            bufferedWriter.write(msg);
            bufferedWriter.write('\n');
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean createLocalLogFile(String path) {
        boolean ret = true;
        File local = new File(LOCAL_DIR);
        if(!local.exists()) {
            ret = local.mkdir();
        }
        if(!ret)
            return false;
        File dir = new File(LOCAL_LOG_DIR);
        if(!dir.exists())
            ret = dir.mkdir();
        if(!ret) {
            return false;
        }
        File file = new File(path);
        if(!file.exists()) {
            try {
                ret = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}

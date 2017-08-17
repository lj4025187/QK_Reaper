package com.fighter.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoaderLog {

    public static final String TAG = "Reaper";
    private static final String LOCAL_DIR = Environment.getExternalStorageDirectory() + "/Reaper";
    private static final String LOCAL_LOG_DIR = LOCAL_DIR + "/logs";
    private static final boolean DEBUG_LOG = true;
    private static final boolean RECORD_LOG = true;
    private static final int FILES_LENGTH = 5;
    private static ExecutorService sExecutor;
    private static SimpleDateFormat sMillionsFormat, sCurrentFormat;

    public static void i(String msg) {
        if (DEBUG_LOG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String subTag, String msg) {
        if (DEBUG_LOG) {
            Log.i(TAG, "[" + subTag + "] ==> " + msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG_LOG) {
            Log.e(TAG, msg);
        }
        if (RECORD_LOG) {
            writeLocalLog(getCurrentMillis()+" : E ", msg);
        }
    }

    public static void e(String subTag, String msg) {
        if (DEBUG_LOG) {
            Log.e(TAG, "[" + subTag + "] ==> " + msg);
        }
        if (RECORD_LOG) {
            writeLocalLog(getCurrentMillis() + " : E ", "[" + subTag + "] ==> " + msg);
        }
    }

    private static String getCurrentMillis() {
        if(sMillionsFormat == null)
            sMillionsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:", Locale.getDefault());
        Date curMillion = new Date(System.currentTimeMillis());//获取当前时间
        return sMillionsFormat.format(curMillion);
    }

    private static String getCurrentDate() {
        if(sCurrentFormat == null)
            sCurrentFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return sCurrentFormat.format(curDate);
    }

    private static void writeLocalLog(final String type, final String msg) {

        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String currentDate = getCurrentDate();
                File file = new File(LOCAL_LOG_DIR + File.separator
                        + "ReaperLog-" + currentDate + ".txt");
                if (!file.exists()) {
                    createLocalLogFile(file.toString());
                }
                if (file.exists()) {
                    writeLocalLog(file, type, msg);
                }
            }
        });
    }

    private static boolean createLocalLogFile(String path) {
        boolean ret = createLocalLogDir();
        if (!ret) return false;
        deleteOldestFile(new File(LOCAL_LOG_DIR));
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createLocalLogDir() {
        boolean ret;
        File local = new File(LOCAL_DIR);
        ret = local.exists() || local.mkdir();
        if (!ret)
            return false;
        File dir = new File(LOCAL_LOG_DIR);
        ret = dir.exists() || dir.mkdir();
        return ret;
    }

    private static void deleteOldestFile(File directory) {
        if (!directory.exists() || !directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        int length = files.length;
        if (length < FILES_LENGTH) return;
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        for (int i = 0; i < length - FILES_LENGTH + 1; i++) {
            files[i].delete();
        }
    }

    private static void writeLocalLog(File file, String type, String msg) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true),
                    type.length() + msg.length() + 1);
            bufferedWriter.write(type);
            bufferedWriter.write(msg);
            bufferedWriter.write('\n');
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

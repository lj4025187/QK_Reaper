package com.fighter.common.utils;

import android.os.Environment;
import android.util.Log;

import com.fighter.config.ReaperConfig;

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

public class ReaperLog {

    public static final String TAG = "Reaper";
    private static final String LOCAL_DIR = Environment.getExternalStorageDirectory() + "/Reaper";
    private static final String LOCAL_LOG_DIR = LOCAL_DIR + "/logs";
    private static final boolean RECORD_LOG = true;
    private static final int FILES_LENGTH = 5;
    private static long sStartTime = 0;
    private static ExecutorService sExecutor;

    public static void i(String msg) {
        if (!ReaperConfig.TEST_MODE)
            return;
        Log.i(TAG, msg);
    }

    public static void i(String subTag, String msg) {
        if (!ReaperConfig.TEST_MODE)
            return;
        Log.i(TAG, "[" + subTag + "] ==> " + msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
        if (RECORD_LOG)
            writeLocalLog("E ", msg);
    }

    public static void e(String subTag, String msg) {
        Log.e(TAG, "[" + subTag + "] ==> " + msg);
        if (RECORD_LOG)
            writeLocalLog("E ", msg);
    }

    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    private static void writeLocalLog(final String type, final String msg) {

        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String currentDate = getCurrentDate();
                File file = new File(LOCAL_LOG_DIR + File.separator + "ReaperLog-" + currentDate + ".txt");
                if (!file.exists()) {
                    if (createLocalLogFile(file.toString())) {
                        writeLocalLog(file, type, msg);
                    }
                } else {
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
        if (!file.exists()) {
            try {
                ret = file.createNewFile();
            } catch (IOException e) {
                ret = false;
                e.printStackTrace();
            }
        } else {
            ret = true;
        }
        return ret;
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

    private static synchronized void deleteOldestFile(File directory) {
        if (!directory.exists() || !directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        int length = files.length;
        if (length <= FILES_LENGTH) return;
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        for (int i = 0; i < length - FILES_LENGTH + 1; i++) {
            boolean delete = files[i].delete();
        }
    }

    private static void writeLocalLog(File file, String type, String msg) {
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file, true), type.length() + msg.length() + 1);
            bufferedWriter.write(type);
            bufferedWriter.write(msg);
            bufferedWriter.write('\n');
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printStackTrace() {
        if (ReaperConfig.TEST_MODE) {
            try {
                StackTraceElement[] sts = Thread.currentThread().getStackTrace();
                for (StackTraceElement stackTraceElement : sts) {
                    DEFAULT_LOGHANDLER.publish("Log_StackTrace", Log.ERROR, stackTraceElement.toString());
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void printException(String msg, Throwable e) {
        if (ReaperConfig.TEST_MODE) {
            DEFAULT_LOGHANDLER.publish("Log_StackTrace", Log.ERROR, msg + '\n' + Log.getStackTraceString(e));
        }
    }

    public static LogHandler DEFAULT_LOGHANDLER = new LogHandler() {
        @Override
        public void publish(String tag, int level, String message) {
            Log.println(level, tag, message);
        }
    };

    public static interface LogHandler {

        void publish(String tag, int level, String message);

    }
}

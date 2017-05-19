package com.fighter.cache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * File cache manager handle
 * <p>
 * Created by LiuJia on 2017/5/19.
 */

public class AdFileCacheManager {

    private final static String TAG = AdFileCacheManager.class.getSimpleName();
    private File mCacheDir;
    private long mMaxSize;

    /**
     *
     * @param context
     * @param maxSize cache file's max bytes size
     * @return
     */
    public static AdFileCacheManager getInstance(Context context, long maxSize) {
        AdFileCacheManager sInstance = new AdFileCacheManager(context, maxSize);
        return sInstance;
    }

    private AdFileCacheManager(Context context, long maxSize) {
        mMaxSize = maxSize;
        init(context);
    }

    private void init(Context context) {
        mCacheDir = getDiskCacheDir(context);
    }

    /**
     * clear the given directory
     *
     * @param directory
     * @return
     */
    public synchronized boolean clearCacheFile(File directory) {
        if (!directory.exists()) {
            ReaperLog.i(TAG, " directory is not exists");
            return false;
        }
        long dirSize = getDirSize(directory);
        ReaperLog.i(TAG, " directory size is before " + dirSize);
        while (dirSize > mMaxSize) {
            deleteOldestFile(directory);
            dirSize = getDirSize(directory);
        }
        ReaperLog.i(TAG, " directory size is after " + dirSize);
        return true;
    }

    /**
     * clear the app cache dir
     *
     * for example "/data/data/com.test.example/cache/*"
     * @return whether clear success
     */
    public synchronized boolean clearCacheFile() {
        if (!mCacheDir.exists()) {
            ReaperLog.i(TAG, " mCacheDir is not exists");
            return false;
        }
        long dirSize = getDirSize(mCacheDir);
        ReaperLog.i(TAG, " mCacheDir size is before " + dirSize);
        while (dirSize > mMaxSize) {
            deleteOldestFile(mCacheDir);
            dirSize = getDirSize(mCacheDir);
        }
        ReaperLog.i(TAG, " mCacheDir size is after " + dirSize);
        return true;
    }

    private long getDirSize(File dir) {
        long result = 0;
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteFile(file);
            } else {
                result += file.length();
            }
        }
        return result;
    }

    private synchronized void deleteOldestFile(File directory) {
        File[] files = directory.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        deleteFile(files[0]);
    }

    private synchronized boolean deleteFile(File file) {
        String fileName = file.getName();
        boolean delete = file.delete();
        ReaperLog.i(TAG, file.lastModified() + " " + (!TextUtils.isEmpty(fileName) ? fileName : " file name is null ")
                + " delete " + (delete ? "success" : "failed"));
        return delete;
    }

    private File getDiskCacheDir(Context context) {
        String cachePath = context.getCacheDir().getPath();
        return new File(cachePath);
    }
}

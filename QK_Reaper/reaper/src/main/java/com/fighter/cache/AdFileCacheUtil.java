package com.fighter.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;

/**
 * File cache manager handle
 * <p>
 * Created by LiuJia on 2017/5/19.
 */

public class AdFileCacheUtil {

    private final static String TAG = AdFileCacheUtil.class.getSimpleName();
    private final static String CACHE_DIR = "ac_file";
    private static File sCacheDir;
    private static long sMaxSize;

    /**
     * @param context
     * @param maxSize cache file's max bytes size
     * @return
     */
    public static AdFileCacheUtil getInstance(Context context, long maxSize) {
        AdFileCacheUtil sInstance = new AdFileCacheUtil(context, maxSize);
        return sInstance;
    }

    private AdFileCacheUtil(Context context, long maxSize) {
        sMaxSize = maxSize;
        init(context);
    }

    private void init(Context context) {
        sCacheDir = getDiskCacheDir(context);
        if (!sCacheDir.exists()) {
            boolean success = sCacheDir.mkdir();
            ReaperLog.i(TAG, "cache dir " + sCacheDir.getAbsolutePath() + " make dir " + success);
        }
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
        while (dirSize > sMaxSize) {
            deleteOldestFile(directory);
            dirSize = getDirSize(directory);
        }
        ReaperLog.i(TAG, " directory size is after " + dirSize);
        return true;
    }

    /**
     * clear the app cache dir
     * <p>
     * for example "/data/data/com.test.example/cache/ac_file/*"
     *
     * @return whether clear success
     */
    public synchronized boolean clearCacheFile() {
        if (!sCacheDir.exists()) {
            ReaperLog.i(TAG, " mCacheDir is not exists");
            return false;
        }
        long dirSize = getDirSize(sCacheDir);
        ReaperLog.i(TAG, " mCacheDir size is before " + dirSize);
        while (dirSize > sMaxSize) {
            deleteOldestFile(sCacheDir);
            dirSize = getDirSize(sCacheDir);
        }
        ReaperLog.i(TAG, " mCacheDir size is after " + dirSize);
        return true;
    }

    /**
     * get the directory's size
     *
     * @param dir
     * @return
     */
    private long getDirSize(File dir) {
        long result = 0;
        for (File file : dir.listFiles()) {
            result += file.length();
        }
        return result;
    }

    /**
     * this method delete the oldest file
     *
     * @param directory
     */
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
        return new File(cachePath, CACHE_DIR);
    }

    /**
     * save the bitmap as a file according key
     *
     * @param key
     * @param bmp
     */
    public synchronized void saveBitmapCache(String key, Bitmap bmp) {
        boolean clearCacheFile = clearCacheFile();
        if (!clearCacheFile)
            return;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(sCacheDir, key));
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            ReaperLog.i(TAG, " save bitmap cache file failed");
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //clear cache file to ensure cache file is bellow sMaxSize;
            clearCacheFile(sCacheDir);
        }
    }

    /**
     * get Bitmap from sdCard according key
     *
     * @param key
     * @return
     */
    public Bitmap readBitmapCache(String key) {
        return BitmapFactory.decodeFile(new File(sCacheDir, key).getAbsolutePath());
    }

    /**
     * get the video path method
     *
     * @param key the video url
     * @return
     */
    public String readVideoPathCache(String key, String url) {
        String suffix = url.substring(url.lastIndexOf("."));
        File file = new File(sCacheDir, key + suffix);
        if (!file.exists())
            return "";
        return file.getAbsolutePath();
    }

    /**
     * save the video into cache file
     *
     * @param key the file url
     * @return
     */
    public synchronized String saveCustomFile(String key, String url) {
        boolean clearCacheFile = clearCacheFile();
        if (!clearCacheFile)
            return "";
        String suffix = url.substring(url.lastIndexOf("."));
        File customFile = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        Boolean renameTo = false;
        try {
            URL fileUrl = new URL(url);
            URLConnection conn = fileUrl.openConnection();
            conn.connect();
            int contentLength = conn.getContentLength();
            inputStream = conn.getInputStream();
            if (contentLength <= 0) {
                ReaperLog.e(TAG, "url connection error length unknown");
                return "";
            }
            customFile = new File(sCacheDir, key + ".tmp");
            if (customFile.exists()) {
                boolean delete = customFile.delete();
            }
            String finalName = customFile.getName() + suffix;
            byte[] bytes = new byte[1024];
            boolean success = customFile.createNewFile();
            if (success) {
                fileOutputStream = new FileOutputStream(customFile);
                while (inputStream.read(bytes) != -1) {
                    fileOutputStream.write(bytes);
                }
                renameTo = customFile.renameTo(new File(finalName));
            }
        } catch (IOException e) {
            if (customFile != null) customFile.delete();
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clearCacheFile(sCacheDir);
        }
        if (renameTo) {
            return customFile.getAbsolutePath();
        } else {
            return "";
        }
    }
}

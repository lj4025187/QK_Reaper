package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.cache.AdFileCacheUtil;
import com.fighter.common.utils.ReaperLog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by LiuJia on 2017/5/19.
 */
@RunWith(AndroidJUnit4.class)
public class AdFileCacheTest {

    private static final String TAG = AdFileCacheTest.class.getSimpleName();

    @Test
    public void testFileCache() {
        Context context = InstrumentationRegistry.getTargetContext();
        AdFileCacheUtil manager = AdFileCacheUtil.getInstance(context, 200 * 1024);
        writeFileToCache(context, manager);
    }

    private void writeFileToCache(final Context context, final AdFileCacheUtil manager) {
        new Thread() {
            @Override
            public void run() {
                File cacheDir = context.getCacheDir();
                ReaperLog.i(TAG, " getExternalCacheDir " + cacheDir.getAbsolutePath());
                boolean exists = cacheDir.exists();
                if (!exists)
                    cacheDir.mkdirs();
                boolean success = writeTestFileSuccess(cacheDir);
                boolean clearSuccess = manager.clearCacheFile();
            }
        }.start();
    }

    private synchronized boolean writeTestFileSuccess(File file) {
        int count = 1000;
        for (int i = 0; i < 10; i++) {
            FileOutputStream fos = null;
            try {
                long begin = System.currentTimeMillis();
                File testFile = new File(file, "test" + i + ".txt");
                if (testFile.exists()) {
                    testFile = new File(file, "test" + i + "_" + i + ".txt");
                }
                testFile.createNewFile();
                fos = new FileOutputStream(testFile);
                for (int j = 0; j < count; j++) {
                    fos.write("what the fuck test file \n".getBytes());
                    if (j % 100 == 0) {
                        ReaperLog.i(TAG, " WRITE DATA " + j);
                    }
                }
                long end = System.currentTimeMillis();
                ReaperLog.i(TAG, i + " file length is " + testFile.length() + " last time " + (end - begin));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (file.listFiles().length == 20) {
            return true;
        } else {
            return false;
        }
    }
}

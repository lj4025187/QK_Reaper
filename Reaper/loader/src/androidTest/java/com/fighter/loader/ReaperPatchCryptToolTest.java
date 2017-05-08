package com.fighter.loader;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.patch.ReaperFile;
import com.fighter.patch.ReaperPatchCryptTool;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by wxthon on 5/8/17.
 */

@RunWith(AndroidJUnit4.class)
public class ReaperPatchCryptToolTest {

    private static final String TAG = ReaperPatchCryptToolTest.class.getSimpleName();

    @Test
    public void useEncryptAndDecrypt() {

        Context context = InstrumentationRegistry.getTargetContext();
        String cacheDir = context.getCacheDir().getAbsolutePath();
        String apkPath = cacheDir + "/a.apk";
        String newapkPath = cacheDir + "/a.apk_new";
        String rrPath = cacheDir + "/a.apk_decrypted";

        BufferedOutputStream bos = null;
        try {
            Random random = new Random(SystemClock.currentThreadTimeMillis());
            bos = new BufferedOutputStream(new FileOutputStream(new File(apkPath)));
            for (int i = 0; i < 1090; ++i) {
                bos.write(random.nextInt());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ReaperFile file = new ReaperFile(apkPath);
        Log.d(TAG, "apk size: " + file.getSize());
        ByteBuffer inputBuffer = file.readFully();
        assertTrue(ReaperPatchCryptTool.encryptTo(file, rrPath));

        ReaperFile rrFile = new ReaperFile(rrPath);
        Log.d(TAG, "rr size: " + rrFile.getSize());
        assertTrue(ReaperPatchCryptTool.decryptTo(rrFile, newapkPath));

        ReaperFile newapkFile = new ReaperFile(newapkPath);
        Log.d(TAG, "new apk size: " + newapkFile.getSize());
        ByteBuffer outputBuffer = newapkFile.readFully();
        assertEquals(inputBuffer.capacity(), outputBuffer.capacity());
    }
}

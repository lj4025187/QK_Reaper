package com.fighter.patch;

import android.os.SystemClock;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 * Created by wxthon on 5/5/17.
 */


/**
 * This is a private tool that just supports for `ReaperPatch`
 */
public class ReaperPatchCryptTool {

    private static final String TAG = ReaperPatchCryptTool.class.getSimpleName();
    private static IReaperBlockCipher sCipher = new AESBlockCipher();

    public static ClassLoader createReaperClassLoader(ReaperFile file, String optimizedDirectory,
                                                      String librarySearchPath, ClassLoader parent) {
        String dexPath = optimizedDirectory + "/" + SystemClock.currentThreadTimeMillis() + ".dex";
        ReaperPatchCryptTool.decryptTo(file, dexPath);
        return new ReaperClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    public static boolean decryptTo(ReaperFile file, String dexPath) {
        ByteBuffer inputBuffer = sCipher.allocateBlockBuffer();
        ByteBuffer outputBuffer = sCipher.allocateBlockBuffer();
        IReaperBlockCipher.Key key = sCipher.createKey();
        fillKey(key);
        sCipher.initKey(key);

        FileInputStream fis = file.openFileInputStream();
        if (fis == null)
            return false;

        Header header = new Header();
        header.read(fis);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dexPath);
            int size;
            long total = 0;
            while ((size = fis.read(inputBuffer.array(), inputBuffer.arrayOffset(), inputBuffer.capacity())) > 0) {
                inputBuffer.limit(size);
                inputBuffer.position(0);
                outputBuffer.clear();
                size = sCipher.decrypt(inputBuffer, outputBuffer);
                if (total + size > header.realSize)
                    size = (int) (header.realSize - total);
                fos.write(outputBuffer.array(), outputBuffer.arrayOffset(), size);
                total += size;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                inputBuffer.clear();
                outputBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public static boolean encryptTo(ReaperFile file, String rrPath) {
        ByteBuffer inputBuffer = sCipher.allocateBlockBuffer();
        ByteBuffer outputBuffer = sCipher.allocateBlockBuffer();
        IReaperBlockCipher.Key key = sCipher.createKey();
        fillKey(key);
        sCipher.initKey(key);

        FileInputStream fis = file.openFileInputStream();
        if (fis == null)
            return false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(rrPath);
            int size;
            new Header(file.getSize()).write(fos);
            while ((size = fis.read(inputBuffer.array(), inputBuffer.arrayOffset(), inputBuffer.capacity())) > 0) {
                Log.d(TAG, "read size: " + size);
                inputBuffer.limit(size);
                inputBuffer.position(0);
                outputBuffer.clear();
                size = sCipher.encrypt(inputBuffer, outputBuffer);
                Log.d(TAG, "encrypted size: " + size);
                fos.write(outputBuffer.array(), outputBuffer.arrayOffset(), size);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                inputBuffer.clear();
                outputBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private static void fillKey(IReaperBlockCipher.Key key) {
        for (int i = 0; i < key.data.length; ++i)
            key.data[0] = (byte) (i << 1 * 5);
    }

    private static class Header {
        long realSize;


        Header() {
            realSize = 0;
        }

        Header(long realSize) {
            this.realSize = realSize;
        }

        public boolean write(FileOutputStream fos) {
            ByteBuffer bb = ByteBuffer.allocate(Long.SIZE/8);
            bb.asLongBuffer().put(realSize);
            try {
                fos.write(bb.array(), 0, bb.capacity());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public boolean read(FileInputStream fis) {
            ByteBuffer bb = ByteBuffer.allocate(Long.SIZE/8);
            try {
                fis.read(bb.array(), 0, bb.capacity());
                this.realSize = bb.asLongBuffer().get();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}

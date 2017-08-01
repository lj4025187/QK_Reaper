package com.fighter.patch;

import com.fighter.utils.LoaderLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by wxthon on 5/5/17.
 */


/**
 * This is a private tool that just supports for `ReaperPatch`
 */
public class ReaperPatchCryptTool {

    public static boolean decryptTo(FileInputStream fis, String dexPath) throws Exception {
        IReaperBlockCipher sCipher = new AESBlockCipher();
        ByteBuffer inputBuffer = sCipher.allocateBlockBuffer();
        ByteBuffer outputBuffer = sCipher.allocateBlockBuffer();
        IReaperBlockCipher.Key key = sCipher.createKey();
        fillKey(key);
        sCipher.initKey(key);

        if (fis == null) {
            throw new Exception("file input stream is null");
        }

        File dexFile = new File(dexPath);
        if (!dexFile.exists()) {
            if(!dexFile.getParentFile().mkdirs()) {
                LoaderLog.e("create dex file fail because no permission");
            }
            dexFile.createNewFile();
        }

        Header header = new Header();
        header.read(fis);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dexFile, false);
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
            throw new Exception("decryptTo: ", e);
        } catch (IOException e) {
            throw new Exception("decryptTo: ", e);
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
                throw new Exception("decryptTo: ", e);
            }
        }

        return true;
    }

    public static boolean encryptTo(File file, String rrPath) throws Exception {
        IReaperBlockCipher sCipher = new AESBlockCipher();
        ByteBuffer inputBuffer = sCipher.allocateBlockBuffer();
        ByteBuffer outputBuffer = sCipher.allocateBlockBuffer();
        IReaperBlockCipher.Key key = sCipher.createKey();
        fillKey(key);
        sCipher.initKey(key);

        FileInputStream fis = new FileInputStream(file);
        if (fis == null) {
            throw new Exception("file input stream is null");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(rrPath);
            int size;
            new Header(file.length()).write(fos);
            while ((size = fis.read(inputBuffer.array(), inputBuffer.arrayOffset(), inputBuffer.capacity())) > 0) {
                inputBuffer.limit(size);
                inputBuffer.position(0);
                outputBuffer.clear();
                size = sCipher.encrypt(inputBuffer, outputBuffer);
                fos.write(outputBuffer.array(), outputBuffer.arrayOffset(), size);
            }
        } catch (FileNotFoundException e) {
            throw new Exception("decryptTo: ", e);
        } catch (IOException e) {
            throw new Exception("decryptTo: ", e);
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
                throw new Exception("decryptTo: ", e);
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

package com.fighter.helper;

import com.fighter.patch.ReaperFile;
import com.fighter.utils.CloseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipFile;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperPatchHelper {

    /**
     * Check whether file is a dex
     *
     * @param file dexFile
     * @return whether is dex
     */
    public static boolean isDexFile(File file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".dex"))
            return true;
        return false;
    }

    /**
     * Check whether file is a apk
     *
     * @param file ReaperFile
     * @return whether is apk
     */
    public static boolean isApkFile(ReaperFile file) {
        if (file == null)
            return false;
        if (file.getRawFile() != null && file.getName().endsWith(".apk")) {
            if (file.hasFD())
                return false;
            else {
                try {
                    new ZipFile(file.getRawFile());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Must ends with .rr, it's a reaper format
     *
     * @param file ReaperFile
     * @return whether is rr file
     */
    public static boolean isReaperFile(ReaperFile file) {
        if (file == null)
            return false;
        if ((file.getRawFile() != null && file.getName().endsWith(".rr")) || file.hasFD()) {
            return true;
        }
        return false;
    }

    /**
     * Just copy a file input dest path, must be a file
     *
     * @param file file
     * @param dstPath dstPath
     * @return whether copy success
     */
    public static boolean copyFileTo(File file, String dstPath) {
        if (!file.isFile())
            return false;
        File dstFile = new File(dstPath);
        if (dstFile.exists())
            return false;
        try {
            if (!dstFile.createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean ret = false;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            bos = new BufferedOutputStream(new FileOutputStream(dstFile));
            int readSize;
            byte []buf = new byte[1024];
            while ((readSize = bis.read(buf)) > 0) {
                bos.write(buf, 0, readSize);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(bis, bos);
        }
        return ret;
    }

    /**
     * Just copy a file input dest path, must be a file
     *
     * @param srcPath srcPath
     * @param dstPath dstPath
     * @return whether copy success
     */
    public static boolean copyFileTo(String srcPath, String dstPath) {
        return copyFileTo(new File(srcPath), dstPath);
    }

    /**
     * Write byte buffer(offset - position) into dest file
     *
     * @param buffer buffer
     * @param dstPath dstPath
     * @return whether write success
     */
    public static boolean writeBufferTo(ByteBuffer buffer, String dstPath) {
        return writeBufferTo(buffer, new File(dstPath));
    }

    /**
     * Write byte buffer(offset - position) into dest file
     *
     * @param buffer buffer
     * @param dstFile dstFile
     * @return whether success
     */
    public static boolean writeBufferTo(ByteBuffer buffer, File dstFile) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(dstFile));
            if (bos != null)
                bos.write(buffer.array(), buffer.arrayOffset(), buffer.position() - buffer.arrayOffset());
            bos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}

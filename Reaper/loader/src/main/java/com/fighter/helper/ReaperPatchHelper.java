package com.fighter.helper;

import com.fighter.patch.ReaperFile;

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
     * @param file
     * @return
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
     * @param file
     * @return
     */
    public static boolean isApkFile(ReaperFile file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".apk")) {
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
     * @param file
     * @return
     */
    public static boolean isReaperFile(ReaperFile file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".rr")) {
            return true;
        }
        return false;
    }

    /**
     * Just copy a file input dest path, must be a file
     *
     * @param file
     * @param dstPath
     * @return
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
            try {
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Just copy a file input dest path, must be a file
     *
     * @param srcPath
     * @param dstPath
     * @return
     */
    public static boolean copyFileTo(String srcPath, String dstPath) {
        return copyFileTo(new File(srcPath), dstPath);
    }

    /**
     * Write byte buffer(offset - position) into dest file
     *
     * @param buffer
     * @param dstPath
     * @return
     */
    public static boolean writeBufferTo(ByteBuffer buffer, String dstPath) {
        return writeBufferTo(buffer, new File(dstPath));
    }

    /**
     * Write byte buffer(offset - position) into dest file
     *
     * @param buffer
     * @param dstFile
     * @return
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

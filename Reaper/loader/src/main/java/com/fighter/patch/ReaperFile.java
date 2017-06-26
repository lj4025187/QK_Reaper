package com.fighter.patch;

import android.content.res.AssetFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by wxthon on 5/8/17.
 */

/**
 * Reaper file support .apk and .rr only
 *
 */
public class ReaperFile {

    private File mFile = null;
    private AssetFileDescriptor mAfd = null;

    public ReaperFile(String pathname) {
        mFile = new File(pathname);
    }

    public ReaperFile(AssetFileDescriptor afd) {
        mAfd = afd;
    }

    /**
     * Must close stream after use finished
     *
     * @return FileInputStream
     */
    public FileInputStream openFileInputStream() {
        FileInputStream fis = null;
        if (mFile != null) {
            try {
                fis = new FileInputStream(mFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if(mAfd != null) {
            try {
                fis = mAfd.createInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fis = null;
        }
        return fis;
    }

    /**
     * Must close stream after use finished
     *
     * @return InputStream
     */
    public InputStream openInputStream() {
        return openFileInputStream();
    }

    /**
     * Get the absolute path of this file
     *
     * @return AbsolutePath
     */
    public String getAbsolutePath() {
        return mFile != null ? mFile.getAbsolutePath() : null;
    }

    /**
     * Get the name of this file
     *
     * @return file name
     */
    public String getName() {
        return mFile != null ? mFile.getName() : null;
    }

    /**
     * Check whether this reaper file represents a FileDescriptor
     *
     * @return FileDescriptor
     */
    public boolean hasFD() {
        return mAfd != null && mFile == null;
    }

    /**
     * Get the raw file of this reaper file
     *
     * @return mFile
     */
    public File getRawFile() {
        return mFile;
    }

    public StructStat getStat() {
        StructStat stat = null;
        try {
            if (hasFD()) {
                stat = Os.fstat(mAfd.getFileDescriptor());
            } else if(mFile != null) {
                stat = Os.stat(mFile.getAbsolutePath());
            }
        } catch (ErrnoException e) {
            e.printStackTrace();
        } finally {
            return stat;
        }
    }

    public long getSize() {
        StructStat stat = getStat();
        return stat != null ? stat.st_size : -1;
    }

    public ByteBuffer readFully() {
        FileInputStream fis = openFileInputStream();
        if (fis == null)
            return null;
        ByteBuffer buffer = null;
        try {
            StructStat stat = Os.fstat(fis.getFD());
            buffer = ByteBuffer.allocate((int) stat.st_size);
            fis.read(buffer.array(), 0, buffer.capacity());
        } catch (ErrnoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return buffer;
        }
    }
}

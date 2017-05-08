package com.fighter.patch;

import android.content.res.AssetFileDescriptor;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wxthon on 5/8/17.
 */

public class ReaperFile {

    private File mFile = null;
    private AssetFileDescriptor mAfd = null;

    public ReaperFile(@NonNull String pathname) {
        mFile = new File(pathname);
    }

    public ReaperFile(AssetFileDescriptor afd) {
        mAfd = afd;
    }

    /**
     * Must close stream after use finished
     *
     * @return
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
     * @return
     */
    public InputStream openInputStream() {
        return openFileInputStream();
    }

    /**
     * Get the absolute path of this file
     *
     * @return
     */
    public String getAbsolutePath() {
        return mFile != null ? mFile.getAbsolutePath() : null;
    }

    /**
     * Get the name of this file
     *
     * @return
     */
    public String getName() {
        return mFile != null ? mFile.getName() : null;
    }

    /**
     * Check whether this reaper file represents a FileDescriptor
     *
     * @return
     */
    public boolean hasFD() {
        return mAfd != null && mFile == null;
    }

    /**
     * Get the raw file of this reaper file
     *
     * @return
     */
    public File getRawFile() {
        return mFile;
    }
}

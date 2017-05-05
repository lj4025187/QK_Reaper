package com.fighter.patch;

import android.os.Environment;

import com.fighter.helper.ReaperPatchHelper;

import java.io.File;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperPatch {

    // Patch Types
    public static final int TYPE_UNKNOWN = 0x01;
    public static final int TYPE_DEX = 0x01;
    public static final int TYPE_APK = 0x02;
    public static final int TYPE_REAPER = 0x03;

    private final String PATCH_DIR = "patch";
    private final String PATCH_OPT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + PATCH_DIR + "/opt";
    private final String PATCH_LIB_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + PATCH_DIR + "/libs";

    private File mFile;
    private ClassLoader mLoader;
    private int mType;

    public ReaperPatch(File file) {
        if (ReaperPatchHelper.isDexFile(file)) {
            mFile = file;
            mType = TYPE_DEX;
            mLoader = new DexClassLoader(mFile.getAbsolutePath(), PATCH_OPT_DIR, PATCH_LIB_DIR,
                    ClassLoader.getSystemClassLoader());
        } else if (ReaperPatchHelper.isApkFile(file)) {
            mFile = file;
            mType = TYPE_APK;
            mLoader = new PathClassLoader(mFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        } else if (ReaperPatchHelper.isReaperFile(file)) {
            mFile = file;
            mType = TYPE_REAPER;
            mLoader = ReaperPatchCryptTool.createReaperClassLoader(mFile.getAbsolutePath(),
                    PATCH_OPT_DIR, PATCH_LIB_DIR, ClassLoader.getSystemClassLoader());
        } else {
            mFile = null;
            mType = TYPE_UNKNOWN;
            mLoader = null;
        }
    }

    public boolean isValid() {
        return mFile != null && mLoader != null;
    }

    public String getName() {
        return isValid() ? mFile.getName() : "";
    }

    public String getAbsolutePath() {
        return isValid() ? mFile.getAbsolutePath() : "";
    }

    public ClassLoader getPatchLoader() {
        if (!isValid())
            return null;
        return mLoader;
    }
}

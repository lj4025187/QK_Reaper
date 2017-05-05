package com.fighter.patch;

import android.os.Environment;

import com.fighter.helper.ReaperPatchHelper;

import java.io.File;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by wxthon on 5/5/17.
 */

/**
 * Reaper patch object
 * You can find reaper core class by this patch through ReaperClassLoader
 * This patch can decrypt .rr file
 */
public class ReaperPatch {

    /**
     * Patch Types
     */
    public static final int TYPE_UNKNOWN    = 0x00;
    public static final int TYPE_DEX        = 0x01;
    public static final int TYPE_APK        = 0x02;
    public static final int TYPE_REAPER     = 0x03;

    /**
     * Private final members
     */
    private final String PATCH_DIR = "patch";
    private final String PATCH_OPT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + PATCH_DIR + "/opt";
    private final String PATCH_LIB_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + PATCH_DIR + "/libs";

    /**
     * Private members
     */
    private File mFile;
    private ClassLoader mLoader;
    private int mType;
    private ReaperPatchVersion mVersion;

    /**
     * Constructor
     * A lot of initializes must been done in here
     *
     * @param file
     */
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
        if (mLoader != null)
            mVersion = new ReaperPatchVersion(mLoader);
    }

    /**
     * Check this patch is validate, don't use if not validate
     *
     * @return
     */
    public boolean isValid() {
        return mFile != null && mLoader != null && mVersion.isValid();
    }

    /**
     * Get the patch name
     *
     * @return
     */
    public String getName() {
        return isValid() ? mFile.getName() : null;
    }

    /**
     * Get the absolute path of the patch
     *
     * @return
     */
    public String getAbsolutePath() {
        return isValid() ? mFile.getAbsolutePath() : null;
    }

    /**
     * Get the class loader from this patch, it's patch entrance
     *
     * @return
     */
    public ClassLoader getPatchLoader() {
        return mLoader;
    }

    /**
     * Get the version of this patch, must check version before use it
     *
     * @return
     */
    public ReaperPatchVersion getVersion() {
        return mVersion;
    }
}

package com.fighter.patch;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by haitengwang on 17/05/2017.
 */

public class ReaperPatchCryptAndroidTool {

    private static final String TAG = ReaperPatchCryptTool.class.getSimpleName();


    /**
     * Private final members
     */
    private static final String PATCH_DIR = ".reaper_patch";
    private static final String PATCH_OPT_DIR = PATCH_DIR + "/opt";
    private static final String PATCH_LIB_DIR = PATCH_DIR + "/libs";


    public static ClassLoader createReaperClassLoader(Context context, ReaperFile file, ClassLoader parent) {
        String optPath = Environment.getDataDirectory().getPath() + "/" + context.getPackageName()
                + "/" + PATCH_OPT_DIR;
        File optFile = new File(optPath);
        if (!optFile.exists())
            optFile.mkdirs();

        String libPath = Environment.getDataDirectory().getPath() + "/" + context.getPackageName()
                + "/" + PATCH_LIB_DIR;
        File libFile = new File(libPath);
        if (!libFile.exists())
            libFile.mkdirs();

        String dexPath = optPath + "/" + System.currentTimeMillis() + ".dex";
        try {
            decryptTo(file, dexPath);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        return new ReaperClassLoader(dexPath, optPath, libPath, parent);
    }

    public static boolean decryptTo(ReaperFile file, String dexPath) throws Exception {
        return ReaperPatchCryptTool.decryptTo(file.openFileInputStream(), dexPath);
    }

    public static boolean encryptTo(ReaperFile file, String rrPath) throws Exception {
        return ReaperPatchCryptTool.encryptTo(file.getRawFile(), rrPath);
    }
}

package com.fighter.patch;

import android.content.Context;
import android.util.Log;

import com.fighter.utils.LoaderLog;
import com.fighter.utils.SignUtil;

import java.io.File;

/**
 * Created by haitengwang on 17/05/2017.
 */

public class ReaperPatchCryptAndroidTool {

    private static final String TAG = "ReaperPatchCryptTool";


    /**
     * Private final members
     */
    private static final String PATCH_DIR = ".reaper_patch";
    private static final String PATCH_OPT_DIR = PATCH_DIR + "/opt";
    private static final String PATCH_LIB_DIR = PATCH_DIR + "/libs";

    public static void deleteAllFiles(Context context) {

        File dexDir = new File(context.getFilesDir(), PATCH_DIR);
        deleteDirectory(dexDir);
    }

    private static void deleteDirectory(File directory) {
        if (!directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if(files == null || files.length == 0) return;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
    }

    public static ClassLoader createReaperClassLoader(Context context, ReaperFile file, ClassLoader parent) {
        File optFile = new File(context.getFilesDir(), PATCH_OPT_DIR);
        if (!optFile.exists()) {
            if (!optFile.mkdirs()) {
                LoaderLog.e(TAG, "create optPath fail because no permission");
                return null;
            }
        }

        File libFile = new File(context.getFilesDir(), PATCH_LIB_DIR);
        if (!libFile.exists()) {
            if (!libFile.mkdirs()) {
                LoaderLog.e(TAG, "create libFile fail because no permission");
                return null;
            }
        }

        File dexFile =
                new File(context.getFilesDir(), PATCH_DIR + "/" + System.currentTimeMillis() + ".dex");
        try {
            decryptTo(file, dexFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        if (!SignUtil.checkSign(context, dexFile.getAbsolutePath())) {
            Log.e(TAG, "reaper rr is not signature illegal");
            return null;
        }
        return new ReaperClassLoader(dexFile.getAbsolutePath(), optFile.getAbsolutePath(),
                libFile.getAbsolutePath(), parent);
    }

    public static boolean decryptTo(ReaperFile file, String dexPath) throws Exception {
        return ReaperPatchCryptTool.decryptTo(file.openFileInputStream(), dexPath);
    }

    public static boolean encryptTo(ReaperFile file, String rrPath) throws Exception {
        return ReaperPatchCryptTool.encryptTo(file.getRawFile(), rrPath);
    }
}

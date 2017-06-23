package com.fighter.patch;

import android.content.Context;
import android.util.Log;

import com.fighter.utils.LoaderLog;
import com.fighter.utils.SignUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by haitengwang on 17/05/2017.
 */

public class ReaperPatchCryptAndroidTool {

    private static final String TAG = ReaperPatchCryptAndroidTool.class.getSimpleName();


    /**
     * Private final members
     */
    private static final String PATCH_DIR = ".reaper_patch";
    private static final String PATCH_OPT_DIR = PATCH_DIR + "/opt";
    private static final String PATCH_LIB_DIR = PATCH_DIR + "/libs";


    public static ClassLoader createReaperClassLoader(Context context, ReaperFile file, ClassLoader parent) {
        String optPath = "/data/data/" + context.getPackageName()
                + "/" + PATCH_OPT_DIR;
        File optFile = new File(optPath);
        if (!optFile.exists()) {
            if(optFile.canWrite()) {
                optFile.mkdirs();
            } else {
                LoaderLog.e(TAG, "create optPath fail because no permission");
            }
        }

        String libPath = "/data/data/" + context.getPackageName()
                + "/" + PATCH_LIB_DIR;
        File libFile = new File(libPath);
        if (!libFile.exists()) {
            if(optFile.canWrite()) {
                libFile.mkdirs();
            } else {
                LoaderLog.e(TAG, "create libFile fail because no permission");
            }
        }

        String dexPath = "/data/data/" + context.getPackageName() + "/" + PATCH_DIR + "/" + System.currentTimeMillis() + ".dex";
        try {
            decryptTo(file, dexPath);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        if (!SignUtil.checkSign(context, dexPath)) {
            Log.e(TAG, "reaper rr is not signature illegal");
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

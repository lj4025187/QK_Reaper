package com.fighter.patch;

import android.util.Log;

/**
 * Created by haitengwang on 17/05/2017.
 */

public class ReaperPatchCryptAndroidTool {

    private static final String TAG = ReaperPatchCryptTool.class.getSimpleName();

    public static ClassLoader createReaperClassLoader(ReaperFile file, String optimizedDirectory,
                                                      String librarySearchPath, ClassLoader parent) {
        String dexPath = optimizedDirectory + "/" + System.currentTimeMillis() + ".dex";
        try {
            decryptTo(file, dexPath);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        return new ReaperClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    public static boolean decryptTo(ReaperFile file, String dexPath) throws Exception {
        return ReaperPatchCryptTool.decryptTo(file.openFileInputStream(), dexPath);
    }

    public static boolean encryptTo(ReaperFile file, String rrPath) throws Exception {
        return ReaperPatchCryptTool.encryptTo(file.getRawFile(), rrPath);
    }
}

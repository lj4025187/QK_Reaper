package com.fighter.patch;

import android.os.SystemClock;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperPatchCryptTool {

    public static ClassLoader createReaperClassLoader(String rrPath, String optimizedDirectory,
                                                      String librarySearchPath, ClassLoader parent) {
        String dexPath = optimizedDirectory + "/" + SystemClock.currentThreadTimeMillis() + ".dex";
        ReaperPatchCryptTool.decryptTo(rrPath, dexPath);
        return new ReaperClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    public static boolean decryptTo(String rrPath, String dexPath) {
        return true;
    }

    public static boolean encryptTo(String dexPath, String rrPath) {
        return true;
    }
}

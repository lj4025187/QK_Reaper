package com.fighter.helper;

import java.io.File;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperPatchHelper {

    public static boolean isDexFile(File file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".dex"))
            return true;
        return false;
    }

    public static boolean isApkFile(File file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".apk")) {
            return true;
        }
        return false;
    }

    public static boolean isReaperFile(File file) {
        if (file == null)
            return false;
        if (file.getName().endsWith(".rr")) {
            return true;
        }
        return false;
    }

}

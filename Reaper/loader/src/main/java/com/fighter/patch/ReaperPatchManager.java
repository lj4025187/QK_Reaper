package com.fighter.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperPatchManager {

    //
    // Unpack patch files, eg: .dex, .apk, .rr
    // Use patch after unpackPatches() with ClassLoader of patch
    // Good luck !
    public static List<ReaperPatch> unpackPatches(List<File> files) {
        List<ReaperPatch> patches = null;
        if (!files.isEmpty()) {
            patches = new ArrayList<>();
            for (File file : files) {
                patches.add(new ReaperPatch(file));
            }
        }
        return patches;
    }

}

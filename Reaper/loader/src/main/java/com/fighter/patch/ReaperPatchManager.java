package com.fighter.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wxthon on 5/5/17.
 */

/**
 *
 * example:
 *
 * String[] paths = new String[] {"base.dex", "base.apk", "base.rr" };
 * List<ReaperFile> files = new ArrayList<>();
 * for (String path: paths) {
 *     files.add(new ReaperFile(path));
 * }
 * List<ReaperPath> patches = ReaperPatchManager.getInstance().unpackPatches(files);
 * ...
 * ClassLoader loader = patches.get(0).getPatchLoader();
 * ...
 *
 */

/**
 * A manager to unpack patches which reaper supports
 */
public class ReaperPatchManager {

    private static ReaperPatchManager sManager = null;

    /**
     * Get ReaperPatchManager instance, it's singleton
     *
     * @return
     */
    public static ReaperPatchManager getInstance() {
        ReaperPatchManager mgr;
        if (sManager == null) {
            synchronized (ReaperPatchManager.class) {
                sManager = new ReaperPatchManager();
            }
            return sManager;
        }
        synchronized (ReaperPatchManager.class) {
            mgr = sManager;
        }
        return mgr;
    }

    /**
     * Get ReaperPatchManager by getInstance() only
     */
    private ReaperPatchManager() {

    }

    /**
     * Unpack patch files, eg: .dex, .apk, .rr
     * Use patch after unpackPatches() with ClassLoader of patch
     * Good luck !
     *
     * @param files
     * @return
     */
    public List<ReaperPatch> unpackPatches(List<ReaperFile> files) {
        List<ReaperPatch> patches = null;
        if (!files.isEmpty()) {
            patches = new ArrayList<>();
            for (ReaperFile file : files) {
                patches.add(new ReaperPatch(file));
            }
        }
        return patches;
    }

}

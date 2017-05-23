package com.fighter.patch;

import dalvik.system.DexClassLoader;

/**
 * Created by wxthon on 5/5/17.
 */

/**
 * Only support .rr format file
 * It's can load classes from .rr file
 */
public class ReaperClassLoader extends DexClassLoader {

    private String mRawDexPath = null;

    public ReaperClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath,
                             ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        mRawDexPath = dexPath;
    }

    public String getRawDexPath() {
        return mRawDexPath;
    }
}

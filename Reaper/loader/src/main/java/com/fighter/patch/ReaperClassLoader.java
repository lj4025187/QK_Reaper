package com.fighter.patch;

import dalvik.system.DexClassLoader;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperClassLoader extends DexClassLoader {

    public ReaperClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}

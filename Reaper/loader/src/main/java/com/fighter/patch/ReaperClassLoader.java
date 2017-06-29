package com.fighter.patch;

import android.text.TextUtils;

import com.fighter.utils.LoaderLog;

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

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (name.startsWith("okio") || name.startsWith("okhttp3")) {
            LoaderLog.i("load " + name + " by ReaperClassloader, do not delegate to parent");
            return findClass(name);
        } else {
            return super.loadClass(name, resolve);
        }
    }

    public String getRawDexPath() {
        return mRawDexPath;
    }
}

package com.fighter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.fighter.download.ReaperEnv;

/**
 * Created by Matti on 2017/5/22.
 */

public class ContextProxy extends ContextWrapper {

    public ContextProxy(Context base) {
        super(base);
    }

    @Override
    public AssetManager getAssets() {
        return ReaperEnv.sAssetManager;
    }

    @Override
    public ClassLoader getClassLoader() {
        return ReaperEnv.sClassLoader;
    }

    @Override
    public Resources getResources() {
        return ReaperEnv.sResources;
    }
}

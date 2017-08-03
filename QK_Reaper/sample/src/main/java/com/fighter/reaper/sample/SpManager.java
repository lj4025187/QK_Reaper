package com.fighter.reaper.sample;

import android.content.Context;
import android.content.SharedPreferences;


public class SpManager {

    private static SpManager sInstance;
    private Context mContext;
    private SharedPreferences mSp;

    public static SpManager getInstance(Context ctx) {
        if(sInstance == null) {
            sInstance = new SpManager(ctx);
            return sInstance;
        }
        return sInstance;
    }

    public SpManager(Context ctx) {
        mContext = ctx.getApplicationContext();
        mSp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
    }

    public void saveString(String key, String value) {
        mSp.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return mSp.getString(key, "");
    }

    public void saveBoolean(String key, boolean value) {
        mSp.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key) {
        return mSp.getBoolean(key, false);
    }

    public boolean getBooleanTrue(String key) {
        return mSp.getBoolean(key, true);
    }

    public void saveLong(String key, long value) {
        mSp.edit().putLong(key, value).apply();
    }

    public long getLong(String key) {
        return mSp.getLong(key, 0);
    }

    public void saveInt(String key, int val) {
        mSp.edit().putInt(key, val).apply();
    }

    public int getInt(String key) {
        return mSp.getInt(key, 0);
    }
}

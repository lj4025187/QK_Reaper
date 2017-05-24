package com.fighter.cache;

import com.fighter.wrapper.ICacheConvert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * the ad information to cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheInfo implements Serializable {
    private static final long serialVersionUID = -4242968385056676005L;

    private long mCacheTime;
    private String mExpireTime;
    private String mAdCacheId;
    private String mCache;
    private boolean mCacheAvailable;
    private String mAdSource;

    public String getAdSource() {
        return mAdSource;
    }

    public void setAdSource(String mAdSource) {
        this.mAdSource = mAdSource;
    }

    public boolean ismCacheAvailable() {
        return mCacheAvailable;
    }

    public void setCacheAvailable(boolean isAvailable) {
        mCacheAvailable = isAvailable;
    }

    public AdCacheInfo() {
        mCacheTime = System.currentTimeMillis();
    }

    public String getExpireTime() {
        return mExpireTime;
    }

    public void setExpireTime(String mExpireTime) {
        this.mExpireTime = mExpireTime;
    }

    public long getCacheTime() {
        return mCacheTime;
    }

    public String getCache() {
        return mCache;
    }

    public void setCache(String cache) {
        this.mCache = cache;
    }

    /**
     * get the ad cache unique id.
     *
     * @return the cache id string
     */
    public String getAdCacheId() {
        return mAdCacheId;
    }

    /**
     * set the ad cache unique id
     *
     * @param adCacheId
     */
    public void setAdCacheId(String adCacheId) {
        this.mAdCacheId = adCacheId;
    }

    @Override
    public String toString() {
        return "AdCacheInfo{" +
                "mCacheTime=" + mCacheTime +
                ", mExpireTime='" + mExpireTime + '\'' +
                ", mAdCacheId='" + mAdCacheId + '\'' +
                ", mCache='" + mCache + '\'' +
                ", mCacheAvailable=" + mCacheAvailable +
                ", mAdSource='" + mAdSource + '\'' +
                '}';
    }
}

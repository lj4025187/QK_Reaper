package com.fighter.cache;

import java.io.Serializable;

/**
 * the ad information to cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheInfo implements Serializable {
    private static final long serialVersionUID = -4242968385056676005L;

    public static final int CACHE_IS_GOOD = 1;
    public static final int CACHE_BACK_TO_USER = 1 << 1;
    public static final int CACHE_DISPLAY_BY_USER = 1 << 2;

    private long mCacheTime;
    private String mExpireTime;
    private String mAdCacheId;
    private String mCache;
    private int mCacheState = CACHE_IS_GOOD;
    private String mAdSource;
    private String mCachePath;
    private String mUuid;

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    public void setCacheState(int mCacheState) {
        this.mCacheState = mCacheState;
    }

    public boolean isCacheBackToUser() {
        return (mCacheState & ~CACHE_BACK_TO_USER) == 0;
    }

    public String getCachePath() {
        return mCachePath;
    }

    public void setCachePath(String mCachePath) {
        this.mCachePath = mCachePath;
    }

    public String getAdSource() {
        return mAdSource;
    }

    public void setAdSource(String mAdSource) {
        this.mAdSource = mAdSource;
    }

    public boolean isCacheDisPlayed() {
        return (mCacheState & CACHE_DISPLAY_BY_USER) == 0;
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
                ", mCacheState=" + mCacheState +
                ", mAdSource='" + mAdSource + '\'' +
                ", mCachePath='" + mCachePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdCacheInfo that = (AdCacheInfo) o;

        if (mCacheTime != that.mCacheTime) return false;
        if (mCacheState != that.mCacheState) return false;
        if (!mExpireTime.equals(that.mExpireTime)) return false;
        if (!mAdCacheId.equals(that.mAdCacheId)) return false;
        if (!mCache.equals(that.mCache)) return false;
        if (!mAdSource.equals(that.mAdSource)) return false;
        return mCachePath.equals(that.mCachePath);

    }

    @Override
    public int hashCode() {
        int result = (int) (mCacheTime ^ (mCacheTime >>> 32));
        result = 31 * result + mExpireTime.hashCode();
        result = 31 * result + mAdCacheId.hashCode();
        result = 31 * result + mCache.hashCode();
        result = 31 * result + mCacheState;
        result = 31 * result + mAdSource.hashCode();
        result = 31 * result + mCachePath.hashCode();
        return result;
    }
}

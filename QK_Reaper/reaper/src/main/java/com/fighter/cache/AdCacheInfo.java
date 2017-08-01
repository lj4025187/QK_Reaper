package com.fighter.cache;

import java.io.Serializable;

/**
 * the ad information to cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheInfo implements Serializable {
    private static final String TAG = AdCacheInfo.class.getSimpleName();
    private static final long serialVersionUID = -4242968385056676005L;

    public static final int CACHE_IS_GOOD = 0;
    public static final int CACHE_IS_RETURN = 1 << 1;
    public static final int CACHE_IS_DISPLAY = 1 << 2;
    public static final int CACHE_IS_HOLD_AD = 1 << 3;
    public static final int CACHE_IS_TIMEOUT = 1 << 4;

    private long mCacheTime;
    private String mExpireTime;
    private String mAdCacheId;
    private Object mCache;
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

    public int getCacheState() {
        return mCacheState;
    }

    public boolean isCacheBackToUser() {
        return (mCacheState & CACHE_IS_RETURN) == CACHE_IS_RETURN;
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
        return (mCacheState & CACHE_IS_DISPLAY) == CACHE_IS_DISPLAY;
    }

    public AdCacheInfo() {
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

    public void setCacheTime(long cacheTime) { mCacheTime = cacheTime; }

    public Object getCache() {
        return mCache;
    }

    public void setCache(Object cache) {
        this.mCache = cache;
    }

    public boolean isCacheTimeOut() {
        return (mCacheState & CACHE_IS_TIMEOUT) == CACHE_IS_TIMEOUT;
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

    public boolean isHoldAd() {
        return (mCacheState & CACHE_IS_HOLD_AD) == CACHE_IS_HOLD_AD;
    }

    @Override
    public String toString() {
        return "AdCacheInfo{" +
                "mCacheTime=" + mCacheTime +
                ", mExpireTime='" + mExpireTime + '\'' +
                ", mAdCacheId='" + mAdCacheId + '\'' +
                ", mCache=" + mCache +
                ", mCacheState=" + mCacheState +
                ", mAdSource='" + mAdSource + '\'' +
                ", mCachePath='" + mCachePath + '\'' +
                ", mUuid='" + mUuid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdCacheInfo that = (AdCacheInfo) o;

        if (mCacheTime != that.mCacheTime) return false;
        if (mCacheState != that.mCacheState) return false;
        if (mExpireTime != null ? !mExpireTime.equals(that.mExpireTime) : that.mExpireTime != null)
            return false;
        if (mAdCacheId != null ? !mAdCacheId.equals(that.mAdCacheId) : that.mAdCacheId != null)
            return false;
        if (mCache != null ? !mCache.equals(that.mCache) : that.mCache != null) return false;
        if (mAdSource != null ? !mAdSource.equals(that.mAdSource) : that.mAdSource != null)
            return false;
        if (mCachePath != null ? !mCachePath.equals(that.mCachePath) : that.mCachePath != null)
            return false;
        return mUuid != null ? mUuid.equals(that.mUuid) : that.mUuid == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (mCacheTime ^ (mCacheTime >>> 32));
        result = 31 * result + (mExpireTime != null ? mExpireTime.hashCode() : 0);
        result = 31 * result + (mAdCacheId != null ? mAdCacheId.hashCode() : 0);
        result = 31 * result + (mCache != null ? mCache.hashCode() : 0);
        result = 31 * result + mCacheState;
        result = 31 * result + (mAdSource != null ? mAdSource.hashCode() : 0);
        result = 31 * result + (mCachePath != null ? mCachePath.hashCode() : 0);
        result = 31 * result + (mUuid != null ? mUuid.hashCode() : 0);
        return result;
    }
}

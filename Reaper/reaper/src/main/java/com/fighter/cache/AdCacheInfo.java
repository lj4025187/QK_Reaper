package com.fighter.cache;

import java.io.Serializable;

/**
 * the ad information to cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheInfo implements Serializable {
    private static final long serialVersionUID = -4242968385056676005L;
    private String mAppPostId;

    public String getAppPostId() {
        return mAppPostId;
    }

    public void setAppPostId(String mAppPostId) {
        this.mAppPostId = mAppPostId;
    }

    @Override
    public String toString() {
        return "AdCacheInfo{" +
                "mAppPostId='" + mAppPostId + '\'' +
                '}';
    }
}

package com.fighter.reaper.sample.model;


import com.fighter.loader.AdInfo;

public abstract class BaseItem {

    private final int mViewType;
    private AdInfo mAdInfo;

    public BaseItem(int viewType) {
        mViewType = viewType;
    }

    public void setAdInfo(AdInfo adInfo) {
        mAdInfo = adInfo;
    }

    public AdInfo getAdInfo() {
        return mAdInfo;
    }

    public int getViewType() {
        return mViewType;
    }
}

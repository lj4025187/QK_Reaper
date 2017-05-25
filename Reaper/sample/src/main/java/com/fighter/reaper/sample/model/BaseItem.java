package com.fighter.reaper.sample.model;


import com.fighter.loader.AdInfo;

public abstract class BaseItem {

    protected int mViewType;
    protected AdInfo mAdInfo;

    public BaseItem(AdInfo adInfo) {
        mAdInfo = adInfo;
    }

    protected void setViewType(int viewType) {
        mViewType = viewType;
    }

    public AdInfo getAdInfo() {
        return mAdInfo;
    }

    public int getViewType() {
        return mViewType;
    }
}

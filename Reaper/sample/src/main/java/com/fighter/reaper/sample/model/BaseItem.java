package com.fighter.reaper.sample.model;


import com.fighter.loader.ReaperApi;

public abstract class BaseItem {

    private final int mViewType;
    private ReaperApi.AdInfo mAdInfo;

    public BaseItem(int viewType) {
        mViewType = viewType;
    }

    public void setAdInfo(ReaperApi.AdInfo adInfo) {
        mAdInfo = adInfo;
    }

    public ReaperApi.AdInfo getAdInfo() {
        return mAdInfo;
    }

    public int getViewType() {
        return mViewType;
    }
}

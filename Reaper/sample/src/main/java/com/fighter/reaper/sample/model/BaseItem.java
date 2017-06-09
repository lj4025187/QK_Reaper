package com.fighter.reaper.sample.model;


import android.text.TextUtils;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.config.SampleConfig;

public abstract class BaseItem {

    protected int mViewType;
    protected int mDetailType;
    protected AdInfo mAdInfo;

    public BaseItem(AdInfo adInfo) {
        mAdInfo = adInfo;
        setViewType(adInfo.getContentType());
        setDetailType(SampleConfig.getDetailType(adInfo));
    }

    protected void setViewType(int viewType) {
        mViewType = viewType;
    }

    protected void setDetailType(int detailType) {
        mDetailType = detailType;
    }

    public AdInfo getAdInfo() {
        return mAdInfo;
    }

    public int getViewType() {
        return mViewType;
    }

    public int getDetailType() {
        return mDetailType;
    }

}

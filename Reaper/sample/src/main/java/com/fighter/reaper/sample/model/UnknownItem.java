package com.fighter.reaper.sample.model;

import com.fighter.loader.AdInfo;

import static com.fighter.reaper.sample.config.SampleConfig.UNKNOWN_AD_TYPE;

/**
 * Created by Administrator on 2017/5/24.
 */

public class UnknownItem extends BaseItem{

    public UnknownItem(AdInfo adInfo) {
        super(adInfo);
        this.setViewType(UNKNOWN_AD_TYPE);
    }
}

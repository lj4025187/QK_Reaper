package com.fighter.reaper.sample.model;

import com.fighter.loader.AdInfo;

import static com.fighter.reaper.sample.config.SampleConfig.PICTURE_AD_TYPE;

public class PicItem extends BaseItem {

    public PicItem(AdInfo adInfo) {
        super(adInfo);
        this.setViewType(PICTURE_AD_TYPE);
    }

}

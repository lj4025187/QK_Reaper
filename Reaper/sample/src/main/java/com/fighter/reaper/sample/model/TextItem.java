package com.fighter.reaper.sample.model;


import com.fighter.loader.AdInfo;

import static com.fighter.reaper.sample.config.SampleConfig.TEXT_AD_TYPE;

public class TextItem extends BaseItem {

    public TextItem(AdInfo adInfo) {
        super(adInfo);
        this.setViewType(TEXT_AD_TYPE);
    }
}

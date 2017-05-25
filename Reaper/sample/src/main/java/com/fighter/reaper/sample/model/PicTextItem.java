package com.fighter.reaper.sample.model;

import com.fighter.loader.AdInfo;

import static com.fighter.reaper.sample.config.SampleConfig.PIC_TEXT_AD_TYPE;

/**
 * Created by Administrator on 2017/5/25.
 */

public class PicTextItem extends BaseItem {

    public PicTextItem(AdInfo adInfo) {
        super(adInfo);
        this.setViewType(PIC_TEXT_AD_TYPE);
    }
}

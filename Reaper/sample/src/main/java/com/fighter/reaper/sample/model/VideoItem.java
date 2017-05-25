package com.fighter.reaper.sample.model;

import com.fighter.loader.AdInfo;

import static com.fighter.reaper.sample.config.SampleConfig.VIDEO_AD_TYPE;

public class VideoItem extends BaseItem {

    public VideoItem(AdInfo adInfo) {
        super(adInfo);
        this.setViewType(VIDEO_AD_TYPE);
    }
}

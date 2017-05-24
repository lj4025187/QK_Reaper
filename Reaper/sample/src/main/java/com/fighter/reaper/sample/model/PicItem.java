package com.fighter.reaper.sample.model;

import static com.fighter.reaper.sample.config.SampleConfig.PICTURE_AD_TYPE;

public class PicItem extends BaseItem {

    private String mCoverUrl;

    public PicItem(String coverUrl) {
        super(PICTURE_AD_TYPE);
        mCoverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }
}

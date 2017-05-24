package com.fighter.reaper.sample.model;

import static com.fighter.reaper.sample.config.SampleConfig.VIDEO_AD_TYPE;

public class VideoItem extends BaseItem {
    private String mVideoUrl;
    private String mCoverUrl;

    public VideoItem(String videoUrl, String coverUrl) {
        super(VIDEO_AD_TYPE);
        mVideoUrl = videoUrl;
        mCoverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }
}

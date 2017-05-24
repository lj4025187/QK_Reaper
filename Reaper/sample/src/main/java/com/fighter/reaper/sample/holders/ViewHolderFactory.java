package com.fighter.reaper.sample.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;

/**
 * @author Wayne
 */
public class ViewHolderFactory {

    public static BaseItemHolder buildViewHolder(ViewGroup parent, int viewType) {
        BaseItemHolder baseItemHolder = null;
        switch (viewType) {
            case SampleConfig.VIDEO_AD_TYPE:
                baseItemHolder = new VideoItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_video_layout, parent, false));
                break;
            default:
                baseItemHolder = new PicItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));

        }
        return baseItemHolder;
    }

}

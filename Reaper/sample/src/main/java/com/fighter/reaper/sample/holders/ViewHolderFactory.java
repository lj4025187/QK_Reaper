package com.fighter.reaper.sample.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fighter.reaper.sample.R;

import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_APP_WALL_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_BANNER_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_FEED_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_FULL_SCREEN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_NATIVE_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_VIDEO_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_PLUG_IN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_UNKNOWN_TYPE;

/**
 * @author liujia
 */
public class ViewHolderFactory {

    public static BaseItemHolder buildViewHolder(ViewGroup parent, int detailType) {
        BaseItemHolder baseItemHolder = null;
        switch (detailType) {
            case DETAIL_BANNER_TYPE:
                baseItemHolder = new BannerItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_PLUG_IN_TYPE:
                baseItemHolder = new PlugInItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_APP_WALL_TYPE:
                baseItemHolder = new AppItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_FULL_SCREEN_TYPE:
                baseItemHolder = new FullScreenItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_FEED_TYPE:
                baseItemHolder = new FeedItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_NATIVE_TYPE:
                baseItemHolder = new NativeItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;
            case DETAIL_VIDEO_TYPE:
                baseItemHolder = new VideoItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_video_layout, parent, false));
                break;
            case DETAIL_UNKNOWN_TYPE:
            default:
                baseItemHolder = new UnknownItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ad_item_layout, parent, false));
                break;

        }
        return baseItemHolder;
    }

}

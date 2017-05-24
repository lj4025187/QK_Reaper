package com.fighter.reaper.sample.holders;

import android.widget.ImageView;
import android.widget.TextView;

import com.fighter.reaper.sample.model.PicItem;

/**
 * Created by Administrator on 2017/5/24.
 */

public class PicItemHolder extends BaseItemHolder<PicItem> {
    public TextView adTitle;
    public ImageView adView;
    public TextView adDesc;
    public TextView adAction;

    public PicItemHolder(int type) {
        super(type);
    }
}

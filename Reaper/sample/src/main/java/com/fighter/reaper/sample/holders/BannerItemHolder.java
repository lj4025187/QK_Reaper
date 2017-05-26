package com.fighter.reaper.sample.holders;

import android.view.View;

import com.fighter.reaper.sample.model.BannerItem;

/**
 * Created by Administrator on 2017/5/26.
 */

public class BannerItemHolder extends BaseItemHolder<BannerItem> {

    protected String TAG = AppItemHolder.class.getSimpleName();

    public BannerItemHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onAttachView(int position, BannerItem iItem) {
        super.onAttachView(position, iItem);
    }
}

package com.fighter.reaper.sample.holders;

import android.view.View;

import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.AppItem;

/**
 * Created by Administrator on 2017/5/24.
 */

public class AppItemHolder extends BaseItemHolder<AppItem> {

    public AppItemHolder(View view) {
        super(view);
    }

    @Override
    public int getType() {
        return SampleConfig.UNKNOWN_AD_TYPE;
    }

    @Override
    public void onAttachView(int position, AppItem iItem) {

    }
}

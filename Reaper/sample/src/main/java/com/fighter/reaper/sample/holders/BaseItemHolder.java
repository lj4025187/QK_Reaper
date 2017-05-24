package com.fighter.reaper.sample.holders;

import android.content.Context;
import android.view.View;

import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.BaseItem;

/**
 * Created by Administrator on 2017/5/24.
 */

public abstract class BaseItemHolder<T extends BaseItem> {

    public View baseView;
    protected Context context;

    public BaseItemHolder(View itemView) {
        baseView = itemView;
        context = itemView.getContext();
    }

    public int getType() {
        return SampleConfig.UNKNOWN_AD_TYPE;
    }

    public abstract void onAttachView(int position, T iItem);
}

package com.fighter.reaper.sample.holders;

import com.fighter.reaper.sample.model.BaseItem;

/**
 * Created by Administrator on 2017/5/24.
 */

public abstract class BaseItemHolder<T extends BaseItem> {

    private final int mType;

    public BaseItemHolder(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}

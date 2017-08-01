package com.fighter.cache;

import android.content.Context;

import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperConfigManager;

import java.util.Collections;
import java.util.List;

/**
 * Created by lichen on 17-6-7.
 */

public class AdRequestFirst implements IAdRequestPolicy {
    private static AdRequestFirst INSTANCE = new AdRequestFirst();

    private Context mContext;
    private String mPosId;

    public static AdRequestFirst getInstance() {
        return INSTANCE;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setPosId(String mPosId) {
        this.mPosId = mPosId;
    }

    @Override
    public ReaperAdSense next(int tryTime) {
        List<ReaperAdSense> list = ReaperConfigManager.getReaperAdSenses(mContext, mPosId);
        ReaperAdSense adSense = null;
        Collections.sort(list);
        if (tryTime < list.size()) {
            adSense = list.get(tryTime);
        }
        return adSense;
    }

    @Override
    public int size() {
        List<ReaperAdSense> list = ReaperConfigManager.getReaperAdSenses(mContext, mPosId);
        return list.size();
    }
}

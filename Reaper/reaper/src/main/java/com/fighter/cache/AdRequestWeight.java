package com.fighter.cache;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lichen on 17-6-7.
 */

public class AdRequestWeight implements IAdRequestPolicy {
    private static final String TAG = AdRequestWeight.class.getSimpleName();

    private static AdRequestWeight INSTANCE = new AdRequestWeight();

    private Context mContext;
    private String mPosId;
    private List<String> mWeight;

    private static int mLocation;
    private List<ReaperAdSense> mBeforeList = new ArrayList<>();

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setPosId(String mPosId) {
        this.mPosId = mPosId;
    }

    public void setWeight(List<String> mWeight) {
        this.mWeight = mWeight;
    }

    public static AdRequestWeight getInstance() {
        return INSTANCE;
    }

    @Override
    public ReaperAdSense next(int tryTime) {
        List<ReaperAdSense> list = ReaperConfigManager.getReaperAdSenses(mContext, mPosId);

        if (isListChanged(list)) {
            mLocation = 0;
        }

        list = expandList(list);

        if (list == null)
            return null;

        ReaperAdSense adSense = list.get(mLocation);

        if (mLocation == list.size() -1) {
            mLocation = 0;
        } else {
            mLocation++;
        }

        return adSense;
    }

    @Override
    public int size() {
        List<ReaperAdSense> list = ReaperConfigManager.getReaperAdSenses(mContext, mPosId);
        list = expandList(list);
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    private List<ReaperAdSense> expandList(List<ReaperAdSense> list) {
        if (list == null || mWeight == null)
            return null;
        if (list.size() != mWeight.size())
            return null;
        List<ReaperAdSense> expandList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int weight = Integer.parseInt(mWeight.get(i));
            for (int j = 0; j < weight; j ++) {
                expandList.add(list.get(i));
            }
        }
        return expandList;
    }

    private boolean isListChanged(List<ReaperAdSense> list) {
        if (mBeforeList.size() == 0) {
            mBeforeList.addAll(list);
            return false;
        }
        // list size changed
        if (mBeforeList.size() != list.size()) {
            mBeforeList.clear();
            mBeforeList.addAll(list);
            return true;
        }
        // list element changed
        for (int i =0; i < list.size(); i++) {
            if (!list.get(i).ads_name.equals(mBeforeList.get(i).ads_name)) {
                mBeforeList.clear();
                mBeforeList.addAll(list);
                return true;
            }
        }
        return false;
    }
}

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

public class AdRequestLoop implements IAdRequestPolicy {
    private static final String TAG = AdRequestLoop.class.getSimpleName();

    private static AdRequestLoop INSTANCE = new AdRequestLoop();

    private static int mLocation = 0;

    private Context mContext;
    private String mPosId;

    private List<ReaperAdSense> mBeforeList = new ArrayList<>();

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setPosId(String mPosId) {
        this.mPosId = mPosId;
    }

    public static AdRequestLoop getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ReaperAdSense> generateList() {
        List<ReaperAdSense> list = ReaperConfigManager.getReaperAdSenses(mContext, mPosId);
        List<ReaperAdSense> locationList = new ArrayList<>();
        locationList.addAll(list);

        if (isListChanged(list)) {
            mLocation = 0;
            mBeforeList.clear();
            mBeforeList.addAll(list);
        }

        for (int i = 0; i < mLocation; i ++) {
            locationList.remove(0);
            ReaperAdSense adSense = list.get(i);
            locationList.add(adSense);
        }
        ReaperLog.i(TAG, "list ==> " + list + "; locationList ==> " + locationList);
        if (mLocation == list.size() -1) {
            mLocation = 0;
        } else {
            mLocation++;
        }

        return locationList;
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
                return true;
            }
        }
        return false;
    }
}

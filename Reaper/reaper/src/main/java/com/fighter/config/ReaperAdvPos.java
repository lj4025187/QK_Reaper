package com.fighter.config;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a adv position defined by config server
 *
 * Created by zhangjg on 17-5-8.
 */

public class ReaperAdvPos {

    /**
     * Do not modify the filed names
     * It is used map to json and database
     */
    public String pos_id;
    public String adv_type;
    public String adv_exposure;


    private List mAdsenseList;

    /**
     * This is called when parse json
     * If an ReaperAdSense add to this ReaperAdvPos,
     * then set ReaperAdSense's pos_id as this ReaperAdvPos's pos_id
     *
     * @param adSense
     */
    public void addAdSense(ReaperAdSense adSense) {
        if (adSense == null) {
            return;
        }
        if (mAdsenseList == null) {
            mAdsenseList = new ArrayList();
        }
        adSense.setPosId(pos_id);
        mAdsenseList.add(adSense);
    }

    public List<ReaperAdSense> getAdSenseList() {
        return mAdsenseList;
    }

    @Override
    public String toString() {
        return "ReaperAdvPos{" +
                "pos_id='" + pos_id + '\'' +
                ", adv_type='" + adv_type + '\'' +
                ", adv_exposure='" + adv_exposure + '\'' +
                '}';
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ReaperConfigDBHelper.POS_COLUMN_POS_ID, pos_id);
        cv.put(ReaperConfigDBHelper.POS_COLUMN_ADV_TYPE, adv_type);
        cv.put(ReaperConfigDBHelper.POS_COLUMN_ADV_EXPOSURE, adv_exposure);
        return cv;
    }
}
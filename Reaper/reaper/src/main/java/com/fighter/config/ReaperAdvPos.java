package com.fighter.config;

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
     * It is used map to json
     */
    public String pos_id;
    public String adv_type;
    public String adv_exposure;


    private List adsenseList;

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
        if (adsenseList == null) {
            adsenseList = new ArrayList();
        }
        adSense.setPosId(pos_id);
        adsenseList.add(adSense);
    }

    @Override
    public String toString() {
        return "ReaperAdvPos{" +
                "pos_id='" + pos_id + '\'' +
                ", adv_type='" + adv_type + '\'' +
                ", adv_exposure='" + adv_exposure + '\'' +
                '}';
    }

    public static ReaperAdvPos fromJson (String json) {
        return new ReaperAdvPos();
    }

    public String toJson () {
        return "";
    }
}
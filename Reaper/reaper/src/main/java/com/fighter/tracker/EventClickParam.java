package com.fighter.tracker;

import java.util.HashMap;

/**
 * tracker click event param
 *
 * Created by lichen on 17-5-9.
 */

public class EventClickParam extends AdParam {
    /** tracker click event click position */
    public String click_pos;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put(TrackerConfig.CLICK_CLICK_POS_KEY, click_pos == null? "" : click_pos);
        return map;
    }

    @Override
    public String toString() {
        return "EventClickParam{" +
                    TrackerConfig.CLICK_CLICK_POS_KEY + "='" + click_pos + '\'' + ", " +
                    super.toString() + '}';
    }
}

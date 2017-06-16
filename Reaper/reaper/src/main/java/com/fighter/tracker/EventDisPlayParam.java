package com.fighter.tracker;

import java.util.HashMap;

/**
 * The param for tacker event display event
 *
 * Created by lichen on 17-5-9.
 */

public class EventDisPlayParam extends AdParam {

    /** display event success or fail*/
    public String result;

    /** display failed reason */
    public String reason;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put(TrackerConfig.DISPLAY_RESULT_KEY, result);
        map.put(TrackerConfig.DISPLAY_REASON_KEY, reason == null ? "" : reason);
        return map;
    }

    @Override
    public String toString() {
        return "EventDisPlayParam{" +
                TrackerConfig.DISPLAY_RESULT_KEY + "='" + result + '\'' +
                TrackerConfig.DISPLAY_REASON_KEY + "='" + reason + '\'' +
                ", " + super.toString() + '}';
    }
}

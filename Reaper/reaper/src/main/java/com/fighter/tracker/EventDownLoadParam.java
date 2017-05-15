package com.fighter.tracker;

import java.util.HashMap;

/**
 * tracker download event param
 *
 * Created by lichen on 17-5-13.
 */

public class EventDownLoadParam extends AdParam {
    /** ad download failed reason */
    public String reason;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("reason", reason);
        return map;
    }

    @Override
    public String toString() {
        return "EventDownLoadParam{" +
                "reason='" + reason + '\'' + ", "
                 + super.toString() + '}';
    }
}

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
        map.put("result", result);
        map.put("reason", reason == null ? "" : reason);
        return map;
    }
}

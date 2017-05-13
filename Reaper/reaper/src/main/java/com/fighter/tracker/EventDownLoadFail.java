package com.fighter.tracker;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-13.
 */

public class EventDownLoadFail extends AdParam {
    /** ad download failed reason */
    public String reason;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("reason", reason);
        return map;
    }
}

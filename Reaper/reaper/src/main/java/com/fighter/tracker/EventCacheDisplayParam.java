package com.fighter.tracker;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-9.
 */

public class EventCacheDisplayParam extends AdParam {
    /** display cache ad location time */
    public String cache_times;

    @Override
    public HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("cache_times", cache_times);
        return map;
    }
}

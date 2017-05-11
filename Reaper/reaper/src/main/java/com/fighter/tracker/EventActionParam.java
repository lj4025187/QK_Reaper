package com.fighter.tracker;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-9.
 */

public class EventActionParam extends AdParam {
    /** tacker event action type*/
    public String act_type;

    @Override
    public HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("act_type", act_type);
        return map;
    }
}

package com.fighter.tracker;

import java.util.HashMap;

/**
 * tracker event action param
 *
 * Created by lichen on 17-5-9.
 */

public class EventActionParam extends AdParam {
    /** tacker event action type*/
    public String act_type;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("act_type", act_type);
        return map;
    }

    @Override
    public String toString() {
        return "EventActionParam{" +
                "act_type='" + act_type + '\'' + ", " + super.toString() +
                '}';
    }
}

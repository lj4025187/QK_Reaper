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
        map.put("click_pos", click_pos == null? "" : click_pos);
        return map;
    }
}

package com.fighter.tracker;

import java.util.HashMap;

/**
 * The param for tacker event display action
 * Created by lichen on 17-5-9.
 */

public class EventDisPlayParam extends AdParam {

    /** before display event pull failed ad sources*/
    public String ad_fail_srcs;

    @Override
    public HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("ad_fail_srcs", ad_fail_srcs);
        return map;
    }
}

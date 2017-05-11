package com.fighter.tracker;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by lichen on 17-5-10.
 */

public class AdParam {
    /** ad package name */
    public String app_pkg;

    /** ad position id */
    public String ad_posid;

    /** ad source */
    public String ad_source;

    /** ad support type */
    public String ad_type;

    /** ad number */
    public String ad_num;

    /** the reserved word */
    public String reserved1;

    /** the reserved word */
    public String reserved2;

    public HashMap<String, String> generateMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_pkg", app_pkg);
        map.put("ad_posid", ad_posid);
        map.put("ad_source", ad_source);
        map.put("ad_type", ad_type);
        map.put("ad_num", ad_num);
        map.put("reserved1", reserved1);
        map.put("reserved2", reserved2);
        return map;
    }
}

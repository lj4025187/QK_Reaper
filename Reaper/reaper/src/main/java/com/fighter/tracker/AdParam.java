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
    public int ad_posid;

    /** ad source */
    public String ad_source;

    /** ad support type */
    public String ad_type;

    /** ad number */
    public int ad_num;

    /** the reserved word */
    public String reserved1;

    /** the reserved word */
    public String reserved2;

    HashMap<String, String> generateMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("app_pkg", app_pkg);
        map.put("ad_posid", String.valueOf(ad_posid));
        map.put("ad_source", ad_source);
        map.put("ad_type", ad_type);
        map.put("ad_num", String.valueOf(ad_num));
        map.put("reserved1", reserved1 == null? "" : reserved1);
        map.put("reserved2", reserved2 == null? "" : reserved2);
        return map;
    }

    @Override
    public String toString() {
        return "AdParam{" +
                "app_pkg='" + app_pkg + '\'' +
                ", ad_posid=" + ad_posid +
                ", ad_source='" + ad_source + '\'' +
                ", ad_type='" + ad_type + '\'' +
                ", ad_num=" + ad_num +
                ", reserved1='" + reserved1 + '\'' +
                ", reserved2='" + reserved2 + '\'' +
                '}';
    }
}

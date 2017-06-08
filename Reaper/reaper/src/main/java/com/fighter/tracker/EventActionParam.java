package com.fighter.tracker;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * tracker event action param
 *
 * Created by lichen on 17-5-9.
 */

public class EventActionParam extends AdParam {
    /** tracker event action type**/
    public String act_type;
    /** tracker app down fail reason**/
    public String reason;

    @Override
    HashMap<String, String> generateMap() {
        HashMap<String, String> map = super.generateMap();
        map.put("act_type", act_type);
        map.put("reason", reason);
        return map;
    }

    @Override
    public String toString() {
        return "EventActionParam{" +
                "act_type='" + act_type + '\'' + ", " +
                (!TextUtils.isEmpty(reason) ? ("reason='" + reason + '\'' + ", ") : "")+
                super.toString() +
                '}';
    }
}

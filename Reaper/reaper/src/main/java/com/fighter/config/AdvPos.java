package com.fighter.config;

/**
 * This is a adv position entity
 *
 * Created by zhangjg on 17-5-8.
 */

public class AdvPos {

    public String id;
    public String size;
    public String desc;
    public String source;
    public String category;
    public boolean enable;

    public static AdvPos fromJson (String json) {
        return new AdvPos();
    }

    public String toJson () {
        return "";
    }
}
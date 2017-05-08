package com.fighter.config;

/**
 * This is a config entity
 *
 * Created by zhangjg on 17-5-8.
 */

public class Config {

    public String id;
    public String adv;
    public String cacheTime;

    public static Config fromJson(String json) {
        return new Config();
    }

    public String toJson () {
        return "";
    }

    public int saveToDB() {
        return 0;
    }
}
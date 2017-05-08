package com.fighter.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage the config
 *
 * Created by zhangjg on 17-5-8.
 */

public class ConfigManager {

    /**
     * Get current config
     *
     * @return
     */
    public static Config getCurrentConfig() {
        return new Config();
    }

    /**
     * Sync config from server
     */
    public static void syncConfig() {

    }

    /**
     * Get adv position of a package
     *
     * @param pkgName
     * @return
     */
    public static List<AdvPos> getAdvPos(String pkgName) {
        return new ArrayList<>();
    }

}
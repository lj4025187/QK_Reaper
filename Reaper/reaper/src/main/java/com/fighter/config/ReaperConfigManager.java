package com.fighter.config;

import android.content.Context;

import com.fighter.config.db.ReaperConfigDB;

import java.util.List;

/**
 * Manage the config
 *
 * It is a high level interface my be used by other module
 *
 * Created by zhangjg on 17-5-8.
 */

public class ReaperConfigManager {


    /**
     * Fetch config messages from server
     * It is a sync http or https request
     *
     * If fetch success, the config will be saved to database automatically
     *
     * @return true when fetch success or false when fetch fail
     */
    public static boolean fetchReaperConfigFromServer(Context context, String pkg,
                                                      String salt, String appKey, String appId) {

        // check should request or not
        if (!ReaperConfigHttpHelper.shouldRequestAgain(context)) {
            // use current config
            return true;
        }

        return ReaperConfigFetcher.fetchWithRetry(context, pkg, salt, appKey, appId);
    }

    /**
     * Get all adv positions from configs
     *
     * @param context
     * @return
     */
    public static List<ReaperAdvPos> getAllReaperAdvPos(Context context) {
        return ReaperConfigDB.getInstance(context).queryAllAdvPos();
    }
    /**
     * Get adv pos message by pos id
     *
     * @param context
     * @param posId
     * @return
     */
    public static ReaperAdvPos getReaperAdvPos(Context context, String posId) {
        return ReaperConfigDB.getInstance(context).queryAdvPos(posId);
    }

    /**
     * Get best ad sense
     *
     * @param context
     * @param posId
     * @return
     */
    public static ReaperAdSense getReaperAdSens(Context context, String posId) {
        return ReaperConfigDB.getInstance(context).queryAdSense(posId);
    }

}
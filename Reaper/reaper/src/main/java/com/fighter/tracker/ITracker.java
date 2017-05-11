package com.fighter.tracker;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-8.
 */

public interface ITracker {
    /**
     *  init tracker
     *  @param context the sdk context
     * */
    void init(Context context);

    /**
     * tracker report event
     * @param context the caller app context
     * @param event_id the event id
     * @param map the report message
     */
    void onEvent(Context context, String event_id, HashMap map);

    /**
     * tracker report event
     * @param context the caller app context
     * @param event_id the event id
     * @param param the report message param object
     */
    void onEvent(Context context, String event_id, AdParam param);
}

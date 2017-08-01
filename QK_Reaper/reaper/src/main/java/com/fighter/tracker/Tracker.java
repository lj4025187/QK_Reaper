package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;

import java.util.HashMap;

/**
 * the class track the reaper event and report to qdas.
 *
 * Created by lichen on 17-5-8.
 */
public class Tracker {
    private static final String TAG = Tracker.class.getSimpleName();

    private static Tracker sTracker = new Tracker();

    public static Tracker getTracker() {
        return sTracker;
    }

    private Tracker() {
    }

    /**
     *  init tracker
     *  @param context the sdk context
     * */
    public void init(Context context) {
        CommonParam.init(context);
        TrackerStatAgent.init(context);
    }

    private void onEvent(Context context, String event_id, HashMap map) {
        TrackerStatAgent.onEvent(context, event_id, map);
    }

    private void onEvent(Context context, String event_id, AdParam param) {
        if (context == null)
            return;
        HashMap<String, String> map = CommonParam.generateMap();
        if (!map.isEmpty() && param != null) {

            HashMap<String, String> adMap = param.generateMap();

            map.putAll(adMap);

            onEvent(context, event_id, map);
        }
    }

    /**
     * tracker display event.
     *
     * @param context the context
     * @param param the display event param
     */
    public void trackDisplayEvent(Context context, EventDisPlayParam param) {
        ReaperLog.i(TAG, "tracker event: " + TrackerEventType.AD_DISPLAY_EVENT + ",param: " + param);
        onEvent(context, TrackerEventType.AD_DISPLAY_EVENT, param);
    }

    /**
     * track click event.
     *
     * @param context the context
     * @param param the click event param
     */
    public void trackClickEvent(Context context, EventClickParam param) {
        ReaperLog.i(TAG, "tracker event: " + TrackerEventType.AD_CLICK_EVENT + ",param: " + param);
        onEvent(context, TrackerEventType.AD_CLICK_EVENT, param);
    }

    /**
     * tracker action event
     *
     * @param context the context
     * @param param the action event param
     */
    public void trackActionEvent(Context context, EventActionParam param) {
        ReaperLog.i(TAG, "tracker event: " + TrackerEventType.AD_ACTION_EVENT + ",param: " + param);
        onEvent(context, TrackerEventType.AD_ACTION_EVENT, param);
    }

    /**
     * tracker download fail event
     *
     * @param context the context
     * @param param the download event param
     */
    public void trackDownloadEvent(Context context, EventDownLoadParam param) {
        ReaperLog.i(TAG, "tracker event: " + TrackerEventType.AD_DOWNLOAD_FAILED_EVENT + ",param: " + param);
        onEvent(context, TrackerEventType.AD_DOWNLOAD_FAILED_EVENT, param);
    }
}

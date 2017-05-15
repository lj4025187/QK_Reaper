package com.fighter.tracker;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-8.
 */
public class Tracker implements ITracker {
    private static final String TAG = Tracker.class.getSimpleName();

    private static Tracker sTracker = new Tracker();

    public static Tracker getTracker() {
        return sTracker;
    }

    private Tracker() {
    }

    @Override
    public void init(Context context) {
        DeviceParam.init(context);
        TrackerStatAgent.init(context);
    }

    private void onEvent(Context context, String event_id, HashMap map) {
        ReaperLog.i(TAG, "event_id = " + event_id + ";Hash map = " + map);
        TrackerStatAgent.onEvent(context, event_id, map);
    }

    private void onEvent(Context context, String event_id, AdParam param) {
        if (context == null)
            return;
        HashMap<String, String> map = DeviceParam.generateMap();
        map = InstantParam.append(context, map);
        if (map != null && param != null) {

            HashMap<String, String> adMap = param.generateMap();

            map.putAll(adMap);

            onEvent(context, event_id, map);
        }
    }

    @Override
    public void trackDisplayEvent(Context context, EventDisPlayParam param) {
        onEvent(context, TrackerEventType.AD_DISPLAY_EVENT, param);
    }

    @Override
    public void trackClickEvent(Context context, EventClickParam param) {
        onEvent(context, TrackerEventType.AD_CLICK_EVENT, param);
    }

    @Override
    public void trackActionEvent(Context context, EventActionParam param) {
        onEvent(context, TrackerEventType.AD_ACTION_EVENT, param);
    }

    @Override
    public void trackDownloadEvent(Context context, EventDownLoadFail param) {
        onEvent(context, TrackerEventType.AD_DOWNLOAD_FAILED_EVENT, param);
    }
}

package com.fighter.tracker;

import android.content.Context;
import android.util.Log;

import com.fighter.common.Device;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-8.
 */
public class Tracker implements ITracker {
    private static final String TAG = Tracker.class.getSimpleName();

    private Context mContext;

    private static Tracker sTracker = new Tracker();

    public static Tracker getTracker() {
        return sTracker;
    }

    private Tracker() {
    }

    @Override
    public void init(Context context) {
        mContext = context;
        DeviceParam.init(context);
    }

    @Override
    public void onEvent(Context context, String event_id, HashMap map) {
        Log.d(TAG, "Hash map = " + map);
    }

    @Override
    public void onEvent(Context context, String event_id, AdParam param) {
        HashMap<String, String> map = DeviceParam.generateMap();
        map = InstantParam.append(context, map);
        if (map != null) {

            HashMap<String, String> adMap = param.generateMap();

            map.putAll(adMap);

            onEvent(context, event_id, map);
        }
    }
}

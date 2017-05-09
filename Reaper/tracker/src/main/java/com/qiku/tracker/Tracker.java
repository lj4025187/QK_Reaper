package com.qiku.tracker;

/**
 * Created by lichen on 17-5-8.
 */
public class Tracker {
    private static final String TAG = Tracker.class.getSimpleName();

    private static Tracker sTracker = new Tracker();

    public static Tracker getsTracker() {
        return sTracker;
    }

    private Tracker() {
    }
}

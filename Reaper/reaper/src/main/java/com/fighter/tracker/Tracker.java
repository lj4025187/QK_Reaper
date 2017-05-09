package com.fighter.tracker;

/**
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
}

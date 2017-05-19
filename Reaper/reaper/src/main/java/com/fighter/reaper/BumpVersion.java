package com.fighter.reaper;

/**
 * Created by wxthon on 5/5/17.
 */


import com.qiku.proguard.annotations.NoProguard;

/**
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ! Don't modified variables, just bump version value, it's dangerous !
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
@NoProguard
public class BumpVersion {

    // Must be final constant
    public static final int RELEASE = 1;
    public static final int SECOND = 0;
    public static final int REVISION = 6;
    public static final String SUFFIX = "-alpha";

    @NoProguard
    public static String value() {
        String v = "" + RELEASE + "." + SECOND + "." + REVISION;
        if (SUFFIX != null)
            v += SUFFIX;
        return v;
    }

    @NoProguard
    public static boolean isValid() {
        return RELEASE > 0 && SECOND >= 0 && REVISION >= 0;
    }
}

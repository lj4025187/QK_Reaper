package com.fighter.reaper;

/**
 * Created by wxthon on 5/5/17.
 */


/**
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ! Don't modified variables, just bump version value, it's dangerous !
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

public class BumpVersion {

    // Must be final constant
    public static final int RELEASE = 1;
    public static final int SECOND = 0;
    public static final int REVISION = 0;
    public static final String SUFFIX = "-alpha";


    public static String value() {
        String v = "" + RELEASE + "." + SECOND + "." + REVISION;
        if (SUFFIX != null)
            v += SUFFIX;
        return v;
    }
}

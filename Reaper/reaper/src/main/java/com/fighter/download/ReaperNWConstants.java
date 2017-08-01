package com.fighter.download;

import android.os.Environment;

import java.io.File;

/**
 * Created by Matti on 2017/5/17.
 */

public class ReaperNWConstants {

    //ServerSdk request params
    public static final String SERVER_SDK_CONF = "{'baseUrl':'https://api.os.qiku.com','resourceUrl':'api/list'}";
    public static final String SERVER_SDK_APP = "Reaper";
    public static final String SERVER_SDK_VERSION = "1.0.0";
    public static final String SERVER_SDK_API = "version";

    public static final String SERVER_COMPATIBLE_API = "compatible_version";
    //SP
    public static final String SP_REAPER_NETWORK = "reaper_network";
    public static final String KEY_TIME = "reaper_time";

    //Download dir
    public static final String DOWNLOAD_DIR =
            Environment.getExternalStorageDirectory().toString() +
                    File.separator + ".reapers" + File.separator + "download";

    public static final String DOWNLOAD_BOTH = "both";
    public static final String DOWNLOAD_WIFI = "wifi";
    public static final String DOWNLOAD_MOBILE = "mobile";
}

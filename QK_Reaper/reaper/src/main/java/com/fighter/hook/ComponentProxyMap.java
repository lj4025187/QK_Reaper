package com.fighter.hook;

import android.text.TextUtils;

import java.util.HashMap;

/**
 * Proxy activity name
 *
 * Created by zhangjigang
 */

public final class ComponentProxyMap {

    public static final String PROXY_ACTIVITY = "com.fighter.proxy.ReaperProxyActivity";
    public static final String AKAD_ACTIVITY = "com.ak.android.bridge.AKActivity";

    public static final String PROXY_WEB_VIEW_ACTIVITY = "com.fighter.proxy.ReaperWebViewActivity";
    public static final String REAL_WEB_VIEW_ACTIVITY = "com.fighter.activities.ReaperWebViewActivity";


    /**
     * Proxy activities => Real activities
     */
    private static final HashMap<String, String> sProxyActivitiesToRealMap = new HashMap<>();

    /**
     * Real activities => Proxy activities
     */
    private static final HashMap<String, String> sRealActivitiesToProxyMap = new HashMap<>();


    static {
        sProxyActivitiesToRealMap.put(PROXY_ACTIVITY, AKAD_ACTIVITY);
        sProxyActivitiesToRealMap.put(PROXY_WEB_VIEW_ACTIVITY, REAL_WEB_VIEW_ACTIVITY);

        sRealActivitiesToProxyMap.put(AKAD_ACTIVITY, PROXY_ACTIVITY);
        sRealActivitiesToProxyMap.put(REAL_WEB_VIEW_ACTIVITY, PROXY_WEB_VIEW_ACTIVITY);
    }


    public static String getRealActivityByProxy(String proxyActivity) {
        if (TextUtils.isEmpty(proxyActivity)) {
            return null;
        }
        return sProxyActivitiesToRealMap.get(proxyActivity);
    }

    public static String getProxyActivityByReal(String realActivity) {
        if (TextUtils.isEmpty(realActivity)) {
            return null;
        }
        return sRealActivitiesToProxyMap.get(realActivity);
    }

    private ComponentProxyMap() {}
}

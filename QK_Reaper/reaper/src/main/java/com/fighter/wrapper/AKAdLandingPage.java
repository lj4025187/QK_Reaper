package com.fighter.wrapper;

import android.content.Context;
import android.content.Intent;

import com.ak.android.base.landingpage.ILandingPageListener;
import com.ak.android.base.landingpage.ILandingPageView;
import com.fighter.common.utils.OpenUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.hook.ComponentProxyMap;

/**
 * Created by jia on 8/29/17.
 */
public class AKAdLandingPage implements ILandingPageView {

    private static final String TAG = "AKAdLandingPage";
    private static final int REQUEST_CODE = 8888;
    private static AKAdLandingPage sInstance;
    private static ILandingPageListener sLandingPageListener;

    public static AKAdLandingPage newInstance() {
        if (sInstance == null)
            sInstance = new AKAdLandingPage();
        return sInstance;
    }

    @Override
    public void open(Context context, String url, ILandingPageListener iLandingPageListener) {
        ReaperLog.i(TAG, "open web view " + context.getPackageName() + " URL " + url);
        sLandingPageListener = iLandingPageListener;
        try {
            Class<?> reaperClass =
                    Class.forName(ComponentProxyMap.PROXY_WEB_VIEW_ACTIVITY, true,
                            context.getClassLoader());
            Intent intent = new Intent(context, reaperClass);
            intent.putExtra("url", url);
            intent.putExtra("requestCode", REQUEST_CODE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            OpenUtils.openWebUrl(context, url);
            e.printStackTrace();
        }
    }

    public ILandingPageListener getPageListener() {
        return sLandingPageListener;
    }
}

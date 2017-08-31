package com.fighter.wrapper;

import android.content.Context;
import android.content.Intent;

import com.ak.android.base.landingpage.ILandingPageListener;
import com.ak.android.base.landingpage.ILandingPageView;
import com.fighter.common.utils.OpenUtils;
import com.fighter.common.utils.ReaperLog;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.fighter.activities.ReaperWebViewActivity;
import com.fighter.common.utils.RefInvoker;
import com.fighter.hook.ComponentProxyMap;
import com.fighter.hook.ReaperGlobal;

/**
 * Created by jia on 8/29/17.
 */
public class AKAdLandingPage implements ILandingPageView {

    private static final String TAG = "AKAdLandingPage";
    private static AKAdLandingPage sInstance;

    public static AKAdLandingPage newInstance() {
        if (sInstance == null)
            sInstance = new AKAdLandingPage();
        return sInstance;
    }

    @Override
    public void open(Context context, String url, ILandingPageListener iLandingPageListener) {
        ReaperLog.i(TAG, "open web view " + ReaperGlobal.getContext().getPackageName() + " URL " + url);
        try {
            Class<?> reaperClass =
                    Class.forName(ComponentProxyMap.PROXY_WEB_VIEW_ACTIVITY, true,
                            ReaperGlobal.getContext().getClassLoader());
            Intent intent = new Intent(ReaperGlobal.getContext(), reaperClass);
            intent.putExtra("url", url);
            Bundle extras = (Bundle) RefInvoker.getField(intent, Intent.class, "mExtras");
            if(extras != null) {
                AKAdWebViewCallback callback = new AKAdWebViewCallback(iLandingPageListener);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    extras.putBinder(ReaperWebViewActivity.EXTRA_WEBVIEW_CALLBACK, callback);
                } else {
                    RefInvoker.invokeMethod(extras, Bundle.class, "putIBinder",
                            new Class[]{String.class, IBinder.class},
                            new Object[]{ReaperWebViewActivity.EXTRA_WEBVIEW_CALLBACK, callback});
                }
            } else {
                ReaperLog.e(TAG, " getExtras == NULL");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            OpenUtils.openWebUrl(context, url);
            e.printStackTrace();
        }
    }
}

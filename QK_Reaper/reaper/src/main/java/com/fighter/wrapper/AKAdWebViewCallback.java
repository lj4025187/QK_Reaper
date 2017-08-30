package com.fighter.wrapper;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.ak.android.base.landingpage.ILandingPageListener;
import com.fighter.common.utils.ReaperLog;
import com.fighter.webview.ReaperWebViewCallback;

/**
 * Created by jia on 8/30/17.
 */
public class AKAdWebViewCallback extends ReaperWebViewCallback {

    private final static String TAG = "AKAdWebViewCallback";

    private ILandingPageListener mListener;

    public AKAdWebViewCallback(ILandingPageListener listener) {
        mListener = listener;
    }

    @Override
    public void shouldOverrideUrlLoading(String url) throws RemoteException {
        ReaperLog.i(TAG, "shouldOverrideUrlLoading " + url);
        if (mListener != null)
            mListener.shouldOverrideUrlLoading(url);
    }

    @Override
    public void onPageFinished(String url) throws RemoteException {
        ReaperLog.i(TAG, "onPageFinished " + url);
        if (mListener != null)
            mListener.onPageFinished(url);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) throws RemoteException {
        ReaperLog.i(TAG, "onPageStarted " + url + " favicon " + favicon);
        if (mListener != null)
            mListener.onPageStarted(url, favicon);
    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl) throws RemoteException {
        ReaperLog.i(TAG, "onReceivedError errorCode " + errorCode
                + " description " + description
                + " failingUrl " + failingUrl);
        if (mListener != null)
            mListener.onReceivedError(errorCode, description, failingUrl);
    }

    @Override
    public void onPageExit() throws RemoteException {
        ReaperLog.i(TAG, "onPageExit");
        if (mListener != null)
            mListener.onPageExit();
    }
}

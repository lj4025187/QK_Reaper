package com.fighter.webview;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.fighter.reaper.webview.IWebViewCallback;

/**
 * Created by jia on 8/30/17.
 */
public class ReaperWebViewCallback extends IWebViewCallback.Stub {

    private final static String TAG = "ReaperWebViewCallback";

    @Override
    public void shouldOverrideUrlLoading(String url) throws RemoteException {

    }

    @Override
    public void onPageFinished(String url) throws RemoteException {

    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) throws RemoteException {

    }

    @Override
    public void onReceivedError(int errorCode, String description, String failingUrl)
            throws RemoteException {

    }

    @Override
    public void onPageExit() throws RemoteException {

    }
}

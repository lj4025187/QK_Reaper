// IWebViewCallback.aidl
package com.fighter.reaper.webview;

// Declare any non-default types here with import statements

interface IWebViewCallback {

    void shouldOverrideUrlLoading(String url);

    void onPageFinished(String url);

    void onPageStarted(String url, in Bitmap favicon);

    void onReceivedError(int errorCode, String description, String failingUrl);

    void onPageExit();
}

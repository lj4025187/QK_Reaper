package com.fighter.reaper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Reaper Activity
 *
 * Created by lichen on 17-6-10.
 */

public class ReaperActivity extends Activity {
    private static final String TAG = ReaperActivity.class.getSimpleName();

    private WebView mReaperWebView;
    private WebViewClient mReaperWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//                if (listener != null) {
//                    listener.onPageStarted(url, favicon);
//                }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//                if (listener != null) {
//                    listener.onPageFinished(url);
//                }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
//                if (listener != null) {
//                    listener.onReceivedError(errorCode, description, failingUrl);
//                }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
//                if (listener != null) {
//                    listener.onReceivedError(error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
//                }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (listener != null && listener.shouldOverrideUrlLoading(url)) {
//                    return true;
//                }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReaperWebView = new WebView(this);
        mReaperWebView.setWebViewClient(mReaperWebViewClient);
        Intent intent = getIntent();
        if (intent != null) {
            String url = intent.getStringExtra("url");
            if (url != null) {
                mReaperWebView.loadUrl(url);
            }
        }
        setContentView(mReaperWebView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

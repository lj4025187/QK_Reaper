package com.fighter.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends BaseActivity {

    private final static String TAG = WebViewActivity.class.getSimpleName();
    private WebView mWebView;
    private static String mUrl;

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();

            }
        } catch (Exception e) {
            SampleLog.e(TAG, "get intent has balabala");
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_web_view);
        mWebView = (WebView) findViewById(R.id.id_web_view);
        handleWebViewSettings();
    }

    private void handleWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        if (!TextUtils.isEmpty(mUrl))
            mWebView.loadUrl(mUrl);
    }
}

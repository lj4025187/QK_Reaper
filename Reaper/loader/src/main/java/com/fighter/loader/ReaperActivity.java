package com.fighter.loader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fighter.utils.LoaderLog;


/**
 * Reaper Activity
 * <p/>
 * Created by lichen on 17-6-10.
 */

public class ReaperActivity extends Activity {

    private final static String TAG = ReaperActivity.class.getSimpleName();

    private Context mContext;
    private RelativeLayout mRootView;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private LinearLayout mBottomBar;
    private WebViewClient mClient = new WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            loadUrl(view, uri.toString());
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            view.stopLoading();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        private void loadUrl(WebView view, String url) {
            if (TextUtils.isEmpty(url))
                return;
            LoaderLog.i(TAG, " load url " + url);
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                view.loadUrl(url);
            }
        }
    };

    private WebChromeClient mChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReaperActivity.this);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setProgress(newProgress);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    };

    private WebSettings mSettings;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        initView();
        Intent intent = getIntent();
        if (intent != null) {
            String url = intent.getStringExtra("url");
            if (!TextUtils.isEmpty(url)) {
                mUrl = url;
                reloadUrl();
            }
        }
    }

    private void initView() {
        mRootView = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mRootView.setBackground(new ColorDrawable(Color.parseColor("#f9f9f9")));
        mRootView.setLayoutParams(params);

        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams proParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 12);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        mProgressBar.setLayoutParams(proParams);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        mProgressBar.setId(View.generateViewId());
        mRootView.addView(mProgressBar);

        mWebView = new WebView(mContext);
        RelativeLayout.LayoutParams webParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        webParam.addRule(RelativeLayout.BELOW, mProgressBar.getId());
        mWebView.setLayoutParams(webParam);
        initWebView();
        mRootView.addView(mWebView);

        mBottomBar = new LinearLayout(mContext);
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBottomBar.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable bottomBack =
                new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0x1F000000, 0xFFFFFFFF});
        bottomBack.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mBottomBar.setBackground(bottomBack);
        mBottomBar.setLayoutParams(bottomParams);
        mBottomBar.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        ImageView refresh = new ImageView(mContext);
        refresh.setPadding(9, 9, 9, 9);
        refresh.setImageResource(android.R.drawable.stat_notify_sync_noanim);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadUrl();
            }
        });

        ImageView close = new ImageView(mContext);
        close.setPadding(9, 9, 9, 9);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBottomBar.addView(refresh);
        mBottomBar.addView(close);
        mRootView.addView(mBottomBar);

        setContentView(mRootView);
    }

    private void initWebView() {
        mWebView.setWebViewClient(mClient);
        mWebView.setWebChromeClient(mChromeClient);
        mWebView.requestFocusFromTouch();
        initWebSettings();
    }

    private void initWebSettings() {
        if (mWebView == null)
            return;
        mSettings = mWebView.getSettings();
        if (mSettings == null)
            return;
        //将图片调整到适合webview的大小
        mSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        mSettings.setLoadWithOverviewMode(true);
        //支持缩放，默认为true。是下面那个的前提。
        mSettings.setSupportZoom(true);
        //设置内置的缩放控件。若为false，则该WebView不可缩放
        mSettings.setBuiltInZoomControls(true);
        //隐藏原生的缩放控件
        mSettings.setDisplayZoomControls(false);
        //支持内容重新布局
        mSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置支持多窗口
        mSettings.supportMultipleWindows();
        //关闭webview中缓存
        mSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启 DOM storage API 功能
        mSettings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        mSettings.setDatabaseEnabled(true);
        //开启 Application Caches 功能
        mSettings.setAppCacheEnabled(true);
        //设置可以访问文件
        mSettings.setAllowFileAccess(true);
        //设置是否允许通过 file url 加载的 Js代码读取其他的本地文件
        mSettings.setAllowFileAccessFromFileURLs(false);
        //设置是否允许通过 file url 加载的 Javascript 可以访问其他的源(包括http、https等源)
        mSettings.setAllowUniversalAccessFromFileURLs(false);
        //支持通过JS打开新窗口
        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //支持自动加载图片
        mSettings.setLoadsImagesAutomatically(true);
        //设置编码格式
        mSettings.setDefaultTextEncodingName("utf-8");
        //不需要保存密码
        mSettings.setSavePassword(false);
    }

    private void reloadUrl() {
        if (TextUtils.isEmpty(mUrl)) {
            LoaderLog.e(TAG, "can not load null url");
            return;
        }
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        mSettings.setJavaScriptEnabled(!mUrl.startsWith("file://"));
        //支持通过JS打开新窗口
        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.loadUrl(mUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView == null)
            return;
        mWebView.onPause();
        mWebView.pauseTimers();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView == null)
            return;
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView == null)
            return;
        mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        mWebView.clearHistory();
        mWebView.clearFormData();
        ((ViewGroup) mWebView.getParent()).removeView(mWebView);
        mWebView.destroy();
        mWebView = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

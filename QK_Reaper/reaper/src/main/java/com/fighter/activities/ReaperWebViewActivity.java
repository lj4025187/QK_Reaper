package com.fighter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
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

import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.RefInvoker;
import com.fighter.reaper.webview.IWebViewCallback;
import com.qiku.proguard.annotations.NoProguard;


/**
 * Reaper WebView Activity used to browse ad
 * 
 * Created by lichen on 17-6-10.
 */
@NoProguard
public class ReaperWebViewActivity extends Activity {

    private final static String TAG = "ReaperWebViewActivity";

    public final static String EXTRA_WEBVIEW_CALLBACK = "WebViewCallBack";

    private Context mContext;
    private RelativeLayout mRootView;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private LinearLayout mBottomBar;
    private WebViewClient mClient = new WebViewClient() {

        boolean visible = false;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            loadUrl(view, uri.toString());
            if(mWebViewCallBack != null) {
                try {
                    mWebViewCallBack.shouldOverrideUrlLoading(uri.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            if(isReload)
                loadUrl(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ReaperLog.i(TAG, "onPageStarted");
            view.setVisibility(View.INVISIBLE);
            if(mWebViewCallBack != null) {
                try {
                    mWebViewCallBack.onPageStarted(url, favicon);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            ReaperLog.i(TAG, "onPageCommitVisible");
            visible = true;
            view.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            ReaperLog.i(TAG, "onPageFinished");
            if(mWebViewCallBack != null) {
                try {
                    mWebViewCallBack.onPageFinished(url);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if(visible) {
                view.setVisibility(View.VISIBLE);
            } else {
                ReaperLog.i(TAG, "page finished not visible open in browser");
                startInBrowser(Uri.parse(url));
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if(mWebViewCallBack != null) {
                try {
                    mWebViewCallBack.onReceivedError(error.getErrorCode(),
                            error.getDescription().toString(),
                            request.getUrl().toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if(!visible) return;
            ReaperLog.i(TAG, "receive err not visible open in browser");
            Uri uri = request.getUrl();
            view.stopLoading();
            startInBrowser(uri);
        }

        private void loadUrl(WebView view, String url) {
            if (TextUtils.isEmpty(url))
                return;
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else  if (url.startsWith("sms:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ReaperWebViewActivity.this);
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
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                  JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
        }
    };

    private WebSettings mSettings;
    private String mUrl;
    private IWebViewCallback mWebViewCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        mContext = getApplicationContext();
        Intent intent = getIntent();
        if (intent == null) return;
        try {
            handleIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.i(TAG, "handleIntent exception : " + e.getClass().getName());
        }
    }

    private void handleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        if (TextUtils.isEmpty(url)) return;
        mUrl = url;
        initWebRootView();
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mWebViewCallBack = (IWebViewCallback) extras.getBinder(EXTRA_WEBVIEW_CALLBACK);
            } else {
                mWebViewCallBack = (IWebViewCallback) RefInvoker.invokeMethod(extras, Bundle.class,
                        "getIBinder", new Class[]{String.class},
                        new Object[]{EXTRA_WEBVIEW_CALLBACK});
            }
        } else {
            ReaperLog.e(TAG, " getExtras == NULL");
        }
        reloadUrl();
     }

    private void initWebRootView() {
        setTheme(android.R.style.Theme_Black_NoTitleBar);
        mRootView = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
        mRootView.setBackground(new ColorDrawable(Color.parseColor("#f9f9f9")));
        mRootView.setLayoutParams(params);

        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams proParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 12);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        mProgressBar.setLayoutParams(proParams);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        mProgressBar.setId(View.generateViewId());
        mRootView.addView(mProgressBar);

        try {
            mWebView = new WebView(mContext);
        } catch (Exception e) {
            ReaperLog.i(TAG, "web view init exception " + e.toString() + " start in browser");
            if(!TextUtils.isEmpty(mUrl))
                startInBrowser(Uri.parse(mUrl.trim()));
            return;
        }
        RelativeLayout.LayoutParams webParam =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
        webParam.addRule(RelativeLayout.BELOW, mProgressBar.getId());
        mWebView.setLayoutParams(webParam);
        initWebView();
        mRootView.addView(mWebView);

        mBottomBar = new LinearLayout(mContext);
        RelativeLayout.LayoutParams bottomParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
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
        if (TextUtils.isEmpty(mUrl) || mSettings == null || mWebView == null) {
            ReaperLog.e(TAG, "can not load null url");
            return;
        }
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        mSettings.setJavaScriptEnabled(!mUrl.startsWith("file://"));
        //支持通过JS打开新窗口
        mSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        ReaperLog.i(TAG, "url : " + mUrl);
        mWebView.loadUrl(mUrl);
    }

    private void startInBrowser(Uri uri) {
        if (!TextUtils.isEmpty(uri.toString())) {
            openWebUrl(uri);
        }
        finish();
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
    public void finish() {
        super.finish();
        if(mWebViewCallBack != null) {
            try {
                mWebViewCallBack.onPageExit();
            } catch (RemoteException e) {
                ReaperLog.e(TAG, " e to string " + e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView == null)
            return super.onKeyDown(keyCode, event);
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean openWebUrl(Uri uri) {
        boolean startSuccess = false;
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(uri);
            startActivity(intent);
            startSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return startSuccess;
    }
}

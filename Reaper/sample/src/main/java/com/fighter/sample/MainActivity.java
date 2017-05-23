package com.fighter.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fighter.loader.ReaperApi;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        ReaperApi.AdRequestCallback,
        AbsListView.OnScrollListener,
        Handler.Callback {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int BAIDU_TYPE = 0x01, TENCENT_TYPE = 0x02, QIHOO_TYPE = 0x03;
    private final static int NOTIFY_DATA_CHANGED = 0x10, NOTIFY_DATA_FAILED = 0x11;

    private CheckBox baidu, tencent, qihoo;
    private boolean baiduChecked, tencentChecked, qihooChecked;
    private ListView mAdsListView;
    private TextView mEmptyText;
    private boolean mShouldLoad;
    private View mFooterView, mProgress;
    private AdAdapter mAdAdapter;
    private List<ReaperApi.AdInfo> mListData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baidu = (CheckBox) findViewById(R.id.id_baidu_check);
        baidu.setOnCheckedChangeListener(this);
        tencent = (CheckBox) findViewById(R.id.id_tencent_check);
        tencent.setOnCheckedChangeListener(this);
        qihoo = (CheckBox) findViewById(R.id.id_qihoo_check);
        qihoo.setOnCheckedChangeListener(this);
        findViewById(R.id.id_start_pull_ads).setOnClickListener(this);
        mAdsListView = (ListView) findViewById(R.id.id_ads_list);
        mEmptyText = (TextView) findViewById(R.id.id_empty_view);
        mProgress = findViewById(R.id.id_progress_view);
        mFooterView = getLayoutInflater().inflate(R.layout.list_view_foot, null);
        mAdAdapter = new AdAdapter(mContext);
        mAdAdapter.setData(mListData);
        mAdsListView.setAdapter(mAdAdapter);
        mAdsListView.setOnScrollListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_start_pull_ads:
                showLoadingView(true);
                showEmptyView(false);
                startPullAds();
                break;
            default:
                break;
        }
    }

    private void startPullAds() {
        if (baiduChecked) pullAdsType(BAIDU_TYPE);
        if (tencentChecked) pullAdsType(TENCENT_TYPE);
        if (qihooChecked) pullAdsType(QIHOO_TYPE);
    }

    private void pullAdsType(int adType) {
        if (mReaperApi == null)
            SampleLog.e(TAG, "ReaperApi init fail");
        mReaperApi.init(mContext, "10010", "not_a_real_key", null);
        ReaperApi.AdRequester adRequester = mReaperApi.getAdRequester("323232", this, null);
        adRequester.requestAd();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.id_baidu_check:
                baiduChecked = isChecked;
                break;
            case R.id.id_tencent_check:
                tencentChecked = isChecked;
                break;
            case R.id.id_qihoo_check:
                qihooChecked = isChecked;
                break;
        }
    }

    @Override
    public void onSuccess(final List<ReaperApi.AdInfo> list) {
        if (list == null || list.isEmpty()) {
            SampleLog.e(TAG, "get ads success but list is null");
            return;
        }
        SampleLog.i(TAG, " on success ads size is " + list.size());
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mListData.addAll(list);
                showLoadingView(false);
                if (mListData.isEmpty()) {
                    mMainHandler.sendEmptyMessage(NOTIFY_DATA_FAILED);
                } else {
                    notifyDataChanged();
                }
                SampleLog.i(TAG, " on success ads size is " + mListData.size()
                        + " list to string" + mListData.toString());
            }
        });
    }

    @Override
    public void onFailed(String s) {
        SampleLog.e(TAG, " get ads fail err msg is:" + s);
        mMainHandler.sendEmptyMessage(NOTIFY_DATA_FAILED);
    }

    private void notifyDataChanged() {
        mAdAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case NOTIFY_DATA_CHANGED:
                mAdAdapter.notifyDataSetChanged();
                showEmptyView(false);
                showLoadingView(false);
                break;
            case NOTIFY_DATA_FAILED:
                if (mListData.isEmpty()) {
                    showEmptyView(true);
                } else {
                    showFooterView(false);
                }
                showLoadingView(false);
                break;
        }
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mShouldLoad && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            showFooterView(true);
            startPullAds();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mShouldLoad = (firstVisibleItem + visibleItemCount <= totalItemCount);
    }

    private void showLoadingView(boolean show) {
        if (mAdsListView == null || mProgress == null)
            return;
        mAdsListView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showFooterView(boolean show) {
        if (mAdsListView == null || mFooterView == null)
            return;
        if (show) {
            if (mAdsListView.getFooterViewsCount() < 1) {
                mAdsListView.addFooterView(mFooterView);
            }
        } else {
            if (mAdsListView.getFooterViewsCount() > 0) {
                mAdsListView.removeFooterView(mFooterView);
            }
        }
    }

    private void showEmptyView(boolean empty) {
        mAdsListView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}

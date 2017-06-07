package com.fighter.reaper.sample.activities;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fighter.loader.AdInfo;
import com.fighter.loader.AdRequester;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.adapter.AdAdapter;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.AppItem;
import com.fighter.reaper.sample.model.BannerItem;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.model.FeedItem;
import com.fighter.reaper.sample.model.FullScreenItem;
import com.fighter.reaper.sample.model.NativeItem;
import com.fighter.reaper.sample.model.PlugInItem;
import com.fighter.reaper.sample.model.UnknownItem;
import com.fighter.reaper.sample.utils.SampleLog;
import com.fighter.reaper.sample.utils.ToastUtil;
import com.fighter.reaper.sample.utils.ViewUtils;
import com.fighter.reaper.sample.videolist.visibility.calculator.SingleListViewItemActiveCalculator;
import com.fighter.reaper.sample.videolist.visibility.scroll.ListViewItemPositionGetter;

import java.util.ArrayList;
import java.util.List;

import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_APP_WALL_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_BANNER_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_FEED_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_FULL_SCREEN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_NATIVE_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_NATIVE_VIDEO_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_PLUG_IN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_UNKNOWN_TYPE;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        AdRequester.AdRequestCallback,
        AbsListView.OnScrollListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int BAIDU_TYPE = 0x01, TENCENT_TYPE = 0x02, QIHOO_TYPE = 0x03;
    private final static int NOTIFY_DATA_CHANGED = 0x10, NOTIFY_DATA_FAILED = 0x11;

    private CheckBox baidu, tencent, qihoo;
    private boolean baiduChecked, tencentChecked, qihooChecked;
    private ViewGroup mServerView;
    private EditText mAppKeyEdit, mAppIdEdit, mPosIdEdit;
    private final static String DEFAULT_POS_ID = "1019";

    private ListView mAdsListView;
    private TextView mEmptyText;
    private boolean mShouldLoad;
    private View mFooterView, mProgress;
    private AdAdapter mAdAdapter;
    private List<BaseItem> mListData = new ArrayList<>();

    /**
     * for handle auto play video
     */
    private int mScrollState;
    private SingleListViewItemActiveCalculator mCalculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mReaperApi.setTagetConfig(SampleConfig.RESPONSE);
    }

    public void initView() {
        setContentView(R.layout.activity_main);
        mAdsListView = (ListView) findViewById(R.id.id_ads_list);
        mAdAdapter = new AdAdapter(mContext);
        mAdAdapter.setData(mListData);
        mAdAdapter.setAttachView(mAdsListView);
        mAdsListView.setAdapter(mAdAdapter);
        mCalculator = new SingleListViewItemActiveCalculator(mAdAdapter, new ListViewItemPositionGetter(mAdsListView));
        mAdsListView.setOnScrollListener(this);

        baidu = (CheckBox) findViewById(R.id.id_baidu_check);
        baidu.setOnCheckedChangeListener(this);
        tencent = (CheckBox) findViewById(R.id.id_tencent_check);
        tencent.setOnCheckedChangeListener(this);
        qihoo = (CheckBox) findViewById(R.id.id_qihoo_check);
        qihoo.setOnCheckedChangeListener(this);

        mServerView = (ViewGroup) findViewById(R.id.id_for_server);
        if(!SampleConfig.IS_FOR_SERVER) ViewUtils.setViewVisibility(mServerView, View.GONE);
        mAppKeyEdit = (EditText) findViewById(R.id.id_app_key_edit);
        mAppIdEdit = (EditText) findViewById(R.id.id_app_id_edit);
        mPosIdEdit = (EditText) findViewById(R.id.id_app_pos_edit);

        findViewById(R.id.id_start_pull_ads).setOnClickListener(this);
        mEmptyText = (TextView) findViewById(R.id.id_empty_view);
        mProgress = findViewById(R.id.id_progress_view);
        mFooterView = getLayoutInflater().inflate(R.layout.list_view_foot, null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_start_pull_ads:
                if(mListData.isEmpty()) {
                    showLoadingView(true);
                    showEmptyView(false);
                } else {
                    showFooterView(true);
                }
                startPullAds();
                break;
            default:
                break;
        }
    }

    private void startPullAds() {
//        if (baiduChecked) pullAdsType(BAIDU_TYPE);
//        if (tencentChecked) pullAdsType(TENCENT_TYPE);
//        if (qihooChecked) pullAdsType(QIHOO_TYPE);
        pullAdsType(BAIDU_TYPE);
    }

    private void pullAdsType(int adType) {
        if (mReaperApi == null) {
            SampleLog.e(TAG, "ReaperApi init fail");
            ToastUtil.getInstance(mContext).showSingletonToast(getString(R.string.ad_reaper_init_failed));
            return;
        }
        String appKey = mAppKeyEdit.getText().toString().trim();
        String appId = mAppIdEdit.getText().toString().trim();
        String posId = mPosIdEdit.getText().toString().trim();
        AdRequester adRequester;
        if(TextUtils.isEmpty(appKey) ||
                TextUtils.isEmpty(appId) ||
                 TextUtils.isEmpty(posId)) {
            if(SampleConfig.IS_FOR_SERVER) {
                ToastUtil.getInstance(mContext).showSingletonToast(R.string.toast_edit_text_null);
                showLoadingView(false);
                showEmptyView(true);
                return;
            } else {
                adRequester = mReaperApi.getAdRequester("1", this);
            }
        } else {
            mReaperApi.init(mContext, appId, appKey, true);
            adRequester = mReaperApi.getAdRequester(posId, this);
        }
        adRequester.requestAd(1);
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
    
    /**
     * add list from call back to mListData
     *
     * @param adInfo
     */
    private void generateAdData(AdInfo adInfo) {
        BaseItem baseItem = parseBaseItem(adInfo);
        if (mListData.contains(baseItem)) {
            SampleLog.i(TAG, " data has exists " + adInfo.getTitle());
            return;
        }
        mListData.add(baseItem);
        SampleLog.i(TAG, " on success ads size is " + mListData.size());
        if (mListData.isEmpty()) {
            mMainHandler.sendEmptyMessage(NOTIFY_DATA_FAILED);
        } else {
            mMainHandler.sendEmptyMessage(NOTIFY_DATA_CHANGED);
        }
    }

    private BaseItem parseBaseItem(AdInfo adInfo) {
        adInfo.onAdShow(null);
        BaseItem baseItem;
        switch (SampleConfig.getDetailType(adInfo)) {
            case DETAIL_BANNER_TYPE:
                baseItem = new BannerItem(adInfo);
                break;
            case DETAIL_PLUG_IN_TYPE:
                baseItem = new PlugInItem(adInfo);
                break;
            case DETAIL_APP_WALL_TYPE:
                baseItem = new AppItem(adInfo);
                break;
            case DETAIL_FULL_SCREEN_TYPE:
                baseItem = new FullScreenItem(adInfo);
                break;
            case DETAIL_FEED_TYPE:
                baseItem = new FeedItem(adInfo);
                break;
            case DETAIL_NATIVE_TYPE:
                baseItem = new NativeItem(adInfo);
                break;
            case DETAIL_NATIVE_VIDEO_TYPE:
                baseItem = new NativeItem(adInfo);
                break;
            case DETAIL_UNKNOWN_TYPE:
            default:
                baseItem = new UnknownItem(adInfo);
                break;
        }
        SampleLog.i(TAG, " data should add " + adInfo.getTitle());
        return baseItem;
    }

    @Override
    public void onSuccess(AdInfo adInfo) {
        if (adInfo == null) {
            SampleLog.e(TAG, " on success ads but ad is null");
            return;
        }
        SampleLog.i(TAG, "on success ad uuid ==> " + adInfo.getUuid());
        generateAdData(adInfo);
    }

    @Override
    public void onFailed(String s) {
        SampleLog.e(TAG, " on fail ads err msg is:" + s);
        ToastUtil.getInstance(mContext).showSingletonToast(R.string.ad_load_failed_toast);
        mMainHandler.sendEmptyMessage(NOTIFY_DATA_FAILED);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case NOTIFY_DATA_CHANGED:
                mAdAdapter.notifyDataSetChanged();
                break;
            case NOTIFY_DATA_FAILED:
                showFooterView(mListData.isEmpty());
                break;
        }
        showEmptyView(mListData.isEmpty());
        showLoadingView(false);
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && mAdAdapter.getCount() > 0) {
            if (mAdAdapter.getCount() > 0)
                mCalculator.onScrollStateIdle();
            if (mShouldLoad) {
                showFooterView(true);
                startPullAds();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mShouldLoad = (firstVisibleItem + visibleItemCount <= totalItemCount);
        if (mCalculator != null)
            mCalculator.onScrolled(mScrollState);
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
        SampleLog.i(TAG, " show empty view " + empty);
        mAdsListView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

}

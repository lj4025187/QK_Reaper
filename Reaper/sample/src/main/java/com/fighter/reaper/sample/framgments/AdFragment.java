package com.fighter.reaper.sample.framgments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.fighter.loader.AdInfo;
import com.fighter.loader.AdRequester;
import com.fighter.loader.ReaperApi;
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

/**
 * Created by liujia on 6/5/17.
 */

public class AdFragment extends Fragment implements Handler.Callback,
        AbsListView.OnScrollListener, AdRequester.AdRequestCallback,
        View.OnClickListener {

    private final static String TAG = AdFragment.class.getSimpleName();
    private final static String AD_CATEGORY = "ad_category";
    private final static int NOTIFY_DATA_CHANGED = 0x01, NOTIFY_EMPTY_VIEW = 0x02, NOTIFY_DATA_FAILED = 0x03;

    public final static int BANNER_AD_CATEGORY = 0x10;

    private ReaperApi mReaperApi;
    private int mCategory;
    private String mSrcName;
    private Context mContext;
    private Handler mMainHandler;

    private ListView mListView;
    private AdAdapter mAdAdapter;
    private List<BaseItem> mListData = new ArrayList<>();
    ;
    private boolean mShouldLoad = false;

    /**
     * for handle auto play video
     */
    private int mScrollState;
    private SingleListViewItemActiveCalculator mCalculator;


    private ViewGroup mFooterView, mProgress;
    private TextView mEmptyView;

    private void setReaperApi(ReaperApi reaperApi) {
        mReaperApi = reaperApi;
    }

    private void setReaperSrc(String srcName) {
        mSrcName = srcName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mCategory = bundle.getInt(AD_CATEGORY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = initView(inflater);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPullAds();
    }

    private View initView(LayoutInflater inflater) {
        View contentView = inflater.inflate(R.layout.layout_ad_fragment, null, false);
        mListView = (ListView) contentView.findViewById(R.id.id_ad_fragment_list);
        mEmptyView = (TextView) contentView.findViewById(R.id.id_ad_fragment_empty);
        mProgress = (ViewGroup) contentView.findViewById(R.id.id_ad_fragment_progress);
        mFooterView = (ViewGroup) inflater.inflate(R.layout.list_view_foot, null);

        mEmptyView.setOnClickListener(this);
        initData();
        return contentView;
    }

    private void initData() {
        mContext = getContext();
        mMainHandler = new Handler(Looper.getMainLooper(), this);
        mAdAdapter = new AdAdapter(mContext);
        mAdAdapter.setData(mListData);
        mAdAdapter.setAttachView(mListView);
        mListView.setAdapter(mAdAdapter);
        mCalculator = new SingleListViewItemActiveCalculator(mAdAdapter, new ListViewItemPositionGetter(mListView));
        mListView.setOnScrollListener(this);
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

    private void showLoadingView(boolean show) {
        if (mListView == null || mProgress == null)
            return;
        mListView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showFooterView(boolean show) {
        if (mListView == null || mFooterView == null)
            return;
        if (show) {
            if (mListView.getFooterViewsCount() < 1) {
                mListView.addFooterView(mFooterView);
            }
        } else {
            if (mListView.getFooterViewsCount() > 0) {
                mListView.removeFooterView(mFooterView);
            }
        }
    }

    private void showEmptyView(boolean empty) {
        SampleLog.i(TAG, " show empty view " + empty);
        mListView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }


    private void startPullAds() {
        if (mReaperApi == null)
            return;
        switch (mCategory) {
            default:
                break;
        }
        mReaperApi.init(mContext, "10010", "not_a_real_key", true);
        AdRequester adRequester = mReaperApi.getAdRequester("1", this);
        adRequester.requestAd();
    }

    public String getAdCategory() {
        String category = null;
        switch (mCategory) {
            case SampleConfig.TEXT_AD_TYPE:
                category = "text";
                break;
            case SampleConfig.PICTURE_AD_TYPE:
                category = "picture";
                break;
            case SampleConfig.PIC_TEXT_AD_TYPE:
                category = "pic_text";
                break;
            case SampleConfig.VIDEO_AD_TYPE:
                category = "video";
                break;
            case SampleConfig.UNKNOWN_AD_TYPE:
            default:
                category = "unknown";
                break;
        }
        return mSrcName + "_" + category;
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
        if (mListData.size() < 5) {
            startPullAds();
        }
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.id_ad_fragment_empty:
                startPullAds();
                break;
        }
    }

    public static class Builder {
        private AdFragment mAdFragment;

        public Builder() {

        }

        public Builder setAdCategory(int adCategory) {
            mAdFragment = new AdFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(AD_CATEGORY, adCategory);
            mAdFragment.setArguments(bundle);
            return this;
        }

        public Builder setReaperApi(ReaperApi reaperApi) {
            mAdFragment.setReaperApi(reaperApi);
            return this;
        }

        public Builder setReaperSrc(String src) {
            mAdFragment.setReaperSrc(src);
            return this;
        }

        public AdFragment create() {
            return mAdFragment;
        }
    }
}

package com.fighter.reaper.sample.framgments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.fighter.reaper.sample.model.VideoItem;
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
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_PLUG_IN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_UNKNOWN_TYPE;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_VIDEO_TYPE;

/**
 * Created by liujia on 6/5/17.
 */

public class AdFragment extends Fragment implements Handler.Callback,
        AbsListView.OnScrollListener, AdRequester.AdRequestCallback,
        View.OnClickListener {

    private final static String TAG = AdFragment.class.getSimpleName();

    private ReaperApi mReaperApi;
    private String mCategory;
    private String mSrcName;
    private Context mContext;
    private Handler mMainHandler;

    private ListView mListView;
    private AdAdapter mAdAdapter;
    private List<BaseItem> mListData = new ArrayList<>();
    private List<String> mFailedData = new ArrayList<>();

    private int mRequestCount;
    /**
     * for handle auto play video
     */
    private int mScrollState;
    private SingleListViewItemActiveCalculator mCalculator;


    private ViewGroup mFooterView, mProgress;
    private TextView mEmptyView;

    public void setReaperApi(ReaperApi reaperApi) {
        mReaperApi = reaperApi;
    }

    public void setReaperSrc(String srcName) {
        mSrcName = srcName;
    }

    public void setAdCategory(String category) {
        mCategory = category;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if(mListData.isEmpty()) {
            startPullAds();
        }
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
        mAdAdapter = new AdAdapter(getActivity());
        mAdAdapter.setData(mListData);
        mAdAdapter.setAttachView(mListView);
        mListView.setAdapter(mAdAdapter);
        mCalculator = new SingleListViewItemActiveCalculator(mAdAdapter, new ListViewItemPositionGetter(mListView));
        mListView.setOnScrollListener(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
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
        mListView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.INVISIBLE);
    }

    private void startPullAds() {
        if (mReaperApi == null)
            return;
        if (mListData.isEmpty()) {
            showLoadingView(true);
            showEmptyView(false);
        }
        String posId = generatePosId();
        boolean isSupport = TextUtils.equals(SampleConfig.QIHOO_VIDEO_ADV/*"5"*/, posId)    //Qihoo video
                        ||  TextUtils.equals(SampleConfig.QIHOO_ORIGINAL_ADV/*"6"*/, posId)    //Qihoo original
                        ||  TextUtils.equals(SampleConfig.TENCENT_INSERT_ADV/*"7"*/, posId)    //Tencent insert
                        ||  TextUtils.equals(SampleConfig.TENCENT_BANNER_ADV/*"8"*/, posId)      //Tencent banner
                        ||  TextUtils.equals(SampleConfig.TENCENT_OPEN_ADV/*"9"*/, posId)      //Tencent openapp
                        ||  TextUtils.equals(SampleConfig.TENCENT_FEED_ADV/*"10"*/, posId)     //Tencent feed
                        ||  TextUtils.equals(SampleConfig.TENCENT_ORIGINAL_ADV/*"11"*/, posId)     //Tencent original
                        ||  TextUtils.equals(SampleConfig.BAIDU_INSERT_ADV/*"13"*/, posId)     //Baidu insert
                        ||  TextUtils.equals(SampleConfig.BAIDU_BANNER_ADV/*"14"*/, posId)     //Baidu banner
                        ||  TextUtils.equals(SampleConfig.BAIDU_OPEN_ADV/*"15"*/, posId);    //Baidu openapp


        if (!isSupport) {
            String toast = String.format(getResources().getString(R.string.toast_dis_support_ad), mSrcName, mCategory);
            ToastUtil.getInstance(mContext).showSingletonToast(toast);
            showLoadingView(false);
            showEmptyView(true);
            return;
        }
        AdRequester adRequester = mReaperApi.getAdRequester(posId, this, true);
        if (adRequester == null)
            return;
        adRequester.requestAd(SampleConfig.REQUEST_COUNT_PER_TIME);
    }

    private String generatePosId() {
        String posId = "";
        switch (mCategory) {
            case SampleConfig.TYPE_PLUG_IN:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = "1";
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = SampleConfig.TENCENT_INSERT_ADV/*"7"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = SampleConfig.BAIDU_INSERT_ADV/*"13"*/;
                break;
            case SampleConfig.TYPE_BANNER:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = "2";
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = SampleConfig.TENCENT_BANNER_ADV/*"8"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = SampleConfig.BAIDU_BANNER_ADV/*"14"*/;
                break;
            case SampleConfig.TYPE_FULL_SCREEN:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = "3";
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = SampleConfig.TENCENT_OPEN_ADV/*"9"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = SampleConfig.BAIDU_OPEN_ADV/*"15"*/;
                break;
            case SampleConfig.TYPE_FEED:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = "4";
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = SampleConfig.TENCENT_FEED_ADV/*"10"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = "16";
                break;
            case SampleConfig.TYPE_VIDEO:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = SampleConfig.QIHOO_VIDEO_ADV/*"5"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = "11";
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = "17";
                break;
            case SampleConfig.TYPE_NATIVE:
                if (TextUtils.equals(mSrcName, SampleConfig.QIHOO_SRC_NAME)) posId = SampleConfig.QIHOO_ORIGINAL_ADV /*"6"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.TENCENT_SRC_NAME)) posId = SampleConfig.TENCENT_ORIGINAL_ADV/*"12"*/;
                if (TextUtils.equals(mSrcName, SampleConfig.BAIDU_SRC_NAME)) posId = "18";
                break;

        }
        return posId;
    }

    public String getAdCategory() {
        return mSrcName + "_" + mCategory;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && mAdAdapter.getCount() > 0) {
            if (mAdAdapter.getCount() > 0)
                mCalculator.onScrollStateIdle();
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                showFooterView(true);
                startPullAds();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mCalculator != null)
            mCalculator.onScrolled(mScrollState);
    }

    @Override
    public void onSuccess(AdInfo adInfo) {
        if (adInfo == null) {
            SampleLog.e(TAG, " on success ads but ad is null");
            return;
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            SampleLog.e(TAG, " onSuccess is not in main thread");
        }
        generateAdData(adInfo);
    }

    @Override
    public void onFailed(String s) {
        SampleLog.e(TAG, " on fail ads err msg is:" + s);
        if (Looper.getMainLooper() != Looper.myLooper()) {
            SampleLog.e(TAG, " onFailed is not in main thread");
        }
        ToastUtil.getInstance(mContext).showSingletonToast(R.string.ad_load_failed_toast);
        mFailedData.add(s);
        if (mListData.size() + mFailedData.size() >= SampleConfig.REQUEST_COUNT_PER_TIME){
            mAdAdapter.notifyDataSetChanged();
            showFooterView(false);
            showLoadingView(false);
            showEmptyView(mListData.isEmpty());
        }
    }

    /**
     * add list from call back to mListData
     *
     * @param adInfo
     */
    private void generateAdData(AdInfo adInfo) {
        BaseItem baseItem = parseBaseItem(adInfo);
        mListData.add(baseItem);
        SampleLog.i(TAG, " on success ads size is " + mListData.size());
        SampleLog.i(TAG, " on success ads uuid " + adInfo.getUuid());
        SampleLog.i(TAG, " on success ads isAvailable " + adInfo.isAvailable());

        if (mListData.size() + mFailedData.size() >= SampleConfig.REQUEST_COUNT_PER_TIME){
            showFooterView(false);
            showLoadingView(false);
            showEmptyView(false);
            mAdAdapter.notifyDataSetChanged();
        }
    }

    private BaseItem parseBaseItem(AdInfo adInfo) {
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
            case DETAIL_VIDEO_TYPE:
                baseItem = new VideoItem(adInfo);
                break;
            case DETAIL_UNKNOWN_TYPE:
            default:
                baseItem = new UnknownItem(adInfo);
                break;
        }
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
            mAdFragment = new AdFragment();
        }

        public Builder setAdCategory(String adCategory) {
            mAdFragment.setAdCategory(adCategory);
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

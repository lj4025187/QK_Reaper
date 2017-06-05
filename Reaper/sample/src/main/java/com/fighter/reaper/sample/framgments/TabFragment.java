package com.fighter.reaper.sample.framgments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fighter.loader.ReaperApi;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.activities.TabMainActivity;
import com.fighter.reaper.sample.adapter.TabViewPagerAdapter;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.widget.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment will handle a viewpager to show ad
 * <p>
 * Created by liujia on 6/5/17.
 */

public class TabFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private final static String TAG = TabFragment.class.getSimpleName();
    public final static String SRC_NAME = "src_name";

    private String mSrcName;
    private ReaperApi mReaperApi;

    private static TabFragment sInstance;
    private static Context sContext;
    private PagerSlidingTabStrip mTabStrip;
    private ViewPager mViewPager;
    private TabViewPagerAdapter mAdapter;
    private List<AdFragment> mAdFragments = new ArrayList<>();

    public static TabFragment newInstance(){
        if(sInstance == null)
            sInstance = new TabFragment();
        return sInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null)
            return;
        mSrcName = arguments.getString(SRC_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = initView(inflater);
        return contentView;
    }

    private View initView(LayoutInflater inflater) {
        View contentView = inflater.inflate(R.layout.layout_fragment_tab, null, false);
        mTabStrip = (PagerSlidingTabStrip) contentView.findViewById(R.id.id_tab_fragment_strip);
        mViewPager = (ViewPager) contentView.findViewById(R.id.id_tab_fragment_viewpager);
        initData();
        return contentView;
    }

    private void initData() {
        mAdapter = new TabViewPagerAdapter(getChildFragmentManager());
        FragmentActivity activity = getActivity();
        if(activity instanceof TabMainActivity){
            TabMainActivity main = (TabMainActivity) activity;
            mReaperApi = main.mReaperApi;
        }
        mAdFragments.add(new AdFragment.Builder().setAdCategory(SampleConfig.TEXT_AD_TYPE).setReaperApi(mReaperApi).setReaperSrc(mSrcName).create());
        mAdFragments.add(new AdFragment.Builder().setAdCategory(SampleConfig.PICTURE_AD_TYPE).setReaperApi(mReaperApi).setReaperSrc(mSrcName).create());
        mAdFragments.add(new AdFragment.Builder().setAdCategory(SampleConfig.PIC_TEXT_AD_TYPE).setReaperApi(mReaperApi).setReaperSrc(mSrcName).create());
        mAdFragments.add(new AdFragment.Builder().setAdCategory(SampleConfig.VIDEO_AD_TYPE).setReaperApi(mReaperApi).setReaperSrc(mSrcName).create());
        mAdapter.setData(mAdFragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mViewPager.setPageMargin(pageMargin);

        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setIndicatorHeight(6);
        mTabStrip.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

}


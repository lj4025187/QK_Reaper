package com.fighter.reaper.sample.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.framgments.TabFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujia on 6/5/17.
 */

public class TabMainActivity extends BaseActivity {

    private final static String TAG = TabMainActivity.class.getSimpleName();

    private FragmentTabHost mTabHost;
    private List<TabItem> mTabList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    public void initView() {
        setContentView(R.layout.layout_activity_tab_main);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.getTabWidget().setDividerDrawable(null);
        initTabData();
    }

    public void initTabData() {
        mTabList = new ArrayList<>();
        mTabList.add(new TabItem(getString(R.string.tencent_ad_src_name), R.mipmap.tencent, TabFragment.newInstance()));
        mTabList.add(new TabItem(getString(R.string.baidu_ad_src_name), R.mipmap.baidu, TabFragment.newInstance()));
        mTabList.add(new TabItem(getString(R.string.qihoo_ad_src_name), R.mipmap.qihoo, TabFragment.newInstance()));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < mTabList.size(); i++) {
                    TabItem tabItem = mTabList.get(i);
                    if (TextUtils.equals(tabItem.getTitleString(), tabId)) {
                        tabItem.setChecked(true);
                    } else {
                        tabItem.setChecked(false);
                    }
                }
            }
        });

        for (int i = 0; i < mTabList.size(); i++) {
            TabItem item = mTabList.get(i);
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(item.getTitleString()).setIndicator(item.getView());
            Bundle bundle = new Bundle();
            String value;
            switch (i) {
                case 0:
                    value = SampleConfig.TENCENT_SRC_NAME;
                    break;
                case 1:
                    value = SampleConfig.BAIDU_SRC_NAME;
                    break;
                case 2:
                    value = SampleConfig.QIHOO_SRC_NAME;
                    break;
                default:
                    value = SampleConfig.UNKNOWN_SRC_NAME;
                    break;
            }
            bundle.putString(TabFragment.SRC_NAME, value);
            mTabHost.addTab(tabSpec, item.fragment.getClass(), bundle);
            if (TextUtils.equals(getString(R.string.tencent_ad_src_name), item.getTitleString()))
                item.setChecked(true);
        }
    }

    /**
     * Tab item view class
     */
    class TabItem {
        private String title;

        public TabFragment fragment;
        public View view;
        public ImageView imageView;
        private int imageSrc;
        public TextView textView;

        public TabItem(String title, int imageSrc, TabFragment tabFragment) {
            this.title = title;
            this.imageSrc = imageSrc;
            this.fragment = tabFragment;
        }

        public String getTitleString() {
            if (TextUtils.isEmpty(title)) {
                return getString(R.string.ad_src_unknown);
            }
            return title;
        }

        public View getView() {
            if (this.view == null) {
                this.view = getLayoutInflater().inflate(R.layout.view_tab_indicator, null);
                this.imageView = (ImageView) this.view.findViewById(R.id.id_tab_view);
                this.textView = (TextView) this.view.findViewById(R.id.id_tab_title);
                this.textView.setVisibility(View.VISIBLE);
                this.textView.setText(getTitleString());
            }
            return this.view;
        }


        public void setChecked(boolean isChecked) {
            if (textView != null) {
                if (isChecked) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(imageSrc);
                    textView.setTextColor(getResources().getColor(R.color.color_tab_indicator_pressed));
                } else {
                    imageView.setVisibility(View.GONE);
                    textView.setTextColor(getResources().getColor(R.color.color_tab_indicator_normal));
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TabItem) {
                TabItem item = (TabItem) obj;
                return TextUtils.equals(this.title, item.title);
            }
            return false;
        }
    }

}

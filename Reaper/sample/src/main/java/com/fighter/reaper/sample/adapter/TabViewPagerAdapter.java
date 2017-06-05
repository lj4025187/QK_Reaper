package com.fighter.reaper.sample.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import com.fighter.reaper.sample.framgments.AdFragment;

import java.util.List;

/**
 * Created by liujia on 6/5/17.
 */

public class TabViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<AdFragment> mFragments;

    public TabViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setData(List<AdFragment> fragments){
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public String getPageTitle(int position){
        return mFragments.get(position).getAdCategory();
    }
}

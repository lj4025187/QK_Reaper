package com.fighter.reaper.sample.videolist.visibility.scroll;

import android.view.View;
import android.widget.ListView;

/**
 * This class is an API for {@link com.fighter.sample.videolist.visibility.calculator.ListItemsVisibilityCalculator}
 * Using this class is can access all the data from ListView
 *
 * @author Wayne
 */
public class ListViewItemPositionGetter implements ItemsPositionGetter {

    private final ListView mListView;

    public ListViewItemPositionGetter(ListView listView) {
        mListView = listView;
    }

    @Override
    public View getChildAt(int position) {
        if (mListView == null) {
            return null;
        }
        return mListView.getChildAt(position);
    }

    @Override
    public int indexOfChild(View view) {
        if (view == null)
            return 0;
        return mListView.indexOfChild(view);
    }

    @Override
    public int getChildCount() {
        if (mListView == null)
            return 0;
        return mListView.getChildCount();
    }

    @Override
    public int getLastVisiblePosition() {
        if (mListView == null)
            return 0;
        return mListView.getLastVisiblePosition();
    }

    @Override
    public int getFirstVisiblePosition() {
        if (mListView == null)
            return 0;
        return mListView.getFirstVisiblePosition();
    }
}

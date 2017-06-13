package com.fighter.reaper.sample.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.holders.BaseItemHolder;
import com.fighter.reaper.sample.holders.VideoItemHolder;
import com.fighter.reaper.sample.holders.ViewHolderFactory;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.videolist.visibility.items.ListItem;
import com.fighter.reaper.sample.videolist.visibility.scroll.ItemsProvider;

import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class AdAdapter extends BaseAdapter implements ItemsProvider {

    private final static String TAG = Adapter.class.getSimpleName();

    private int VIEW_TYPE_COUNT = 8;
    private Activity mActivity;
    private Context mContext;
    private ListView mListView;
    private List<? extends BaseItem> mList;
    private ArrayMap<BaseItemHolder, Integer> mHolderHelper;

    public AdAdapter(Context context) {
        mContext = context;
        mHolderHelper = new ArrayMap<>();
    }

    public AdAdapter(Activity activity) {
        mActivity = activity;
        mHolderHelper = new ArrayMap<>();
    }


    public void setData(List<BaseItem> list) {
        mList = list;
    }

    public void setAttachView(ListView listView) {
        mListView = listView;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public BaseItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseItem baseItem = mList.get(position);
        final AdInfo adInfo = baseItem.getAdInfo();
        BaseItemHolder baseItemHolder;
        int detailType = SampleConfig.getDetailType(adInfo);
        if (convertView == null) {
            baseItemHolder = ViewHolderFactory.buildViewHolder(parent, detailType);
            convertView = baseItemHolder.baseView;
            convertView.setTag(baseItemHolder);
        } else {
            baseItemHolder = (BaseItemHolder) convertView.getTag();
        }
        BaseItem item = getItem(position);
        adInfo.onAdShow(convertView);
        baseItemHolder.onAttachView(position, item);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                adInfo.onAdClicked(null, null, 0, 0, 0, 0);
                adInfo.onAdClicked(mActivity, v, 0, 0, 0, 0);
            }
        });
        mHolderHelper.put(baseItemHolder, position);
        return convertView;
    }

    @Override
    public ListItem getListItem(int position) {
        int childCount = mListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mListView.getChildAt(i);
            if (view != null) {
                if (view.getTag() instanceof VideoItemHolder) {
                    VideoItemHolder holder = (VideoItemHolder) view.getTag();
                    int holderPosition = mHolderHelper.get(holder);
                    if (holderPosition == position) {
                        return holder;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int listItemSize() {
        return getCount();
    }
}

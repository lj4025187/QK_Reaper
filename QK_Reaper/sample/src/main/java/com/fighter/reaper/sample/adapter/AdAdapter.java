package com.fighter.reaper.sample.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.util.ArrayMap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.holders.BaseItemHolder;
import com.fighter.reaper.sample.holders.VideoItemHolder;
import com.fighter.reaper.sample.holders.ViewHolderFactory;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.utils.ToastUtil;
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
        mContext = activity;
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
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int downX = 0, downY = 0, upX = 0, upY = 0;
                long downtime = 0, uptime = 0;
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();
                    downtime = event.getEventTime();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    upX = (int) event.getRawX();
                    upY = (int) event.getRawY();
                    uptime = event.getEventTime();
                }
                if (adInfo.getActionType() == 2) {
                    if(uptime - downtime > 1000)
                        showDownloadDialog(adInfo, v, downX, downY,upX, upY);
                } else if (adInfo.getActionType() == 1) {
                    if(uptime - downtime > 1000)
                        adInfo.onAdClicked(mActivity, v, downX, downY,upX, upY);
                }
                return true;
            }
        });
        mHolderHelper.put(baseItemHolder, position);
        return convertView;
    }

    private void showDownloadDialog(final AdInfo adInfo, final View view,
                                    final int downX, final int downY,
                                    final int upX, final int upY) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.download_dialog_title)
                .setMessage(R.string.download_dialog_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtil.getInstance(mContext).showSingletonToast(R.string.toast_start_download);
                        adInfo.onAdClicked(mActivity, view, downX, downY,upX, upY);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtil.getInstance(mContext).showSingletonToast(R.string.toast_cancel_download);
                    }
                });
        builder.create().show();
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

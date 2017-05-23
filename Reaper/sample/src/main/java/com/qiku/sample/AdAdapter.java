package com.qiku.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fighter.loader.ReaperApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class AdAdapter extends BaseAdapter {

    private final int AD_TEXT_TYPE = 0x01, AD_IMAGE_TYPE = 0x02,
            AD_VIDEO_TYPE = 0x03, AD_TEXT_IMAGE_TYPE = 0x04;
    private int VIEW_TYPE_COUNT = 4;
    private Context mContext;
    private List<ReaperApi.AdInfo> mList;

    public AdAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }

    public void setData(List<ReaperApi.AdInfo> list) {
        this.mList.addAll(list);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReaperApi.AdInfo adInfo = mList.get(position);
        int contentType = adInfo.getContentType();
        ViewHolder viewHolder;
        if (convertView == null) {
            switch (contentType) {
                case AD_TEXT_TYPE:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item_layout, null);
                    break;
                case AD_IMAGE_TYPE:
                    break;
                case AD_VIDEO_TYPE:
                    break;
                case AD_TEXT_IMAGE_TYPE:
                    break;
            }
            viewHolder = new ViewHolder();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    class ViewHolder {
        TextView adDes;
        ImageView adImageView;
    }
}

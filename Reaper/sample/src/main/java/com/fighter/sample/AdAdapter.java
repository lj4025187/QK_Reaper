package com.fighter.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fighter.loader.ReaperApi;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/5/22.
 */

public class AdAdapter extends BaseAdapter {

    private final static String TAG = Adapter.class.getSimpleName();
    private final int AD_BANNER_TYPE = 0x01,
            AD_PLUG_TYPE = 0x02,
            AD_APP_WALL_TYPE = 0x03,
            AD_FULL_SCREEN_TYPE = 0x04,
            AD_FEED_TYPE = 0x05,
            AD_NATIVE_TYPE = 0x08,
            AD_VIDEO_TYPE = 0x09;
    private int VIEW_TYPE_COUNT = 4;
    private Context mContext;
    private List<ReaperApi.AdInfo> mList;

    public AdAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<ReaperApi.AdInfo> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? "null" : mList.get(position);
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
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_banner_item_layout, null);
            viewHolder.adTitle = (TextView) convertView.findViewById(R.id.id_ad_banner_title);
            viewHolder.adBannerView = (ImageView) convertView.findViewById(R.id.id_ad_banner_view);
            viewHolder.adDesc = (TextView) convertView.findViewById(R.id.id_ad_banner_desc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        attachInfoToView(adInfo, viewHolder);
        return convertView;
    }

    private void attachInfoToView(ReaperApi.AdInfo adInfo, ViewHolder viewHolder) {
        String title = adInfo.getTitle();
        if (!TextUtils.isEmpty(title)) {
            viewHolder.adTitle.setText(title);
        } else {
            Log.e(TAG, "attach view title " + title);
        }
        String desc = adInfo.getDesc();
        if (!TextUtils.isEmpty(desc)) {
            viewHolder.adDesc.setText(desc);
        } else {
            Log.e(TAG, "attach view dec " + desc);
        }
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            viewHolder.adBannerView.setImageBitmap(bitmap);
        }
    }

    class ViewHolder {
        TextView adTitle;
        ImageView adBannerView;
        TextView adDesc;
    }
}

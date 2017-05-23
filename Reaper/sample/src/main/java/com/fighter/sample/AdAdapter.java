package com.fighter.sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private final int ACTION_TYPE_BROWSER = 0x01, ACTION_TYPE_DOWNLOAD = 0x02;
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
        SampleLog.i(TAG, " ad " + position + " toString : " + adInfo.toString());
        ViewHolder viewHolder = null;
        VideoViewHolder videoViewHolder = null;
        if (convertView == null) {
            switch (adInfo.getContentType()) {
                case AD_VIDEO_TYPE:
                    videoViewHolder = new VideoViewHolder();
                    convertView = getVideoAdView(videoViewHolder);
                    convertView.setTag(videoViewHolder);
                    break;
                default:
                    viewHolder = new ViewHolder();
                    convertView = getCustomAdView(viewHolder);
                    convertView.setTag(viewHolder);
                    break;
            }
        } else {
            switch (adInfo.getContentType()) {
                case AD_VIDEO_TYPE:
                    videoViewHolder = (VideoViewHolder) convertView.getTag();
                    break;
                default:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
            }
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleConfig.OPEN_WEB_ACTION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        switch (adInfo.getContentType()) {
            case AD_VIDEO_TYPE:
                attachVideoToView(adInfo, videoViewHolder);
                break;
            default:
                attachInfoToView(adInfo, viewHolder);
                break;
        }
        return convertView;
    }

    private View getVideoAdView(VideoViewHolder videoViewHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item_video_layout, null);
        videoViewHolder.adVideoTitle = (TextView) convertView.findViewById(R.id.id_video_ad_title);
        videoViewHolder.adVideoTexture = (TextureView) convertView.findViewById(R.id.id_video_texture_view);
        videoViewHolder.adVideoThumb = (ImageView) convertView.findViewById(R.id.id_video_view_thumb);
        videoViewHolder.adVideoCotroller = (ImageView) convertView.findViewById(R.id.id_video_controller);
        videoViewHolder.adVideoProgress = (ProgressBar) convertView.findViewById(R.id.id_video_progress);
        videoViewHolder.adVideoDesc = (TextView) convertView.findViewById(R.id.id_ad_custom_desc);
        videoViewHolder.adVideoAction = (TextView) convertView.findViewById(R.id.id_ad_custom_action);
        return convertView;
    }

    private View getCustomAdView(ViewHolder viewHolder) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item_layout, null);
        viewHolder.adTitle = (TextView) convertView.findViewById(R.id.id_ad_custom_title);
        viewHolder.adView = (ImageView) convertView.findViewById(R.id.id_ad_image_view);
        viewHolder.adDesc = (TextView) convertView.findViewById(R.id.id_ad_custom_desc);
        viewHolder.adAction = (TextView) convertView.findViewById(R.id.id_ad_custom_action);
        return convertView;
    }

    /**
     * attach custom ad info to view holder
     *
     * @param adInfo
     * @param viewHolder
     */
    private void attachInfoToView(ReaperApi.AdInfo adInfo, ViewHolder viewHolder) {
        String title = adInfo.getTitle();
        if (!TextUtils.isEmpty(title)) {
            viewHolder.adTitle.setText(title);
        } else {
            SampleLog.e(TAG, "attach view title " + title);
        }
        String desc = adInfo.getDesc();
        if (!TextUtils.isEmpty(desc)) {
            viewHolder.adDesc.setText(desc);
        } else {
            SampleLog.e(TAG, "attach view dec " + desc);
        }
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            viewHolder.adView.setImageBitmap(bitmap);
        }
        int actionType = adInfo.getActionType();
        switch (actionType) {
            case ACTION_TYPE_BROWSER:
                viewHolder.adAction.setText("点击查看");
                break;
            case ACTION_TYPE_DOWNLOAD:
                viewHolder.adAction.setText("点击下载");
                break;
            default:
                viewHolder.adAction.setText("什么鬼。。。");
                break;
        }
    }

    /**
     * Attach video info to video view holder
     *
     * @param adInfo
     * @param videoHolder
     */
    private void attachVideoToView(ReaperApi.AdInfo adInfo, VideoViewHolder videoHolder) {
        String title = adInfo.getTitle();
        if (!TextUtils.isEmpty(title)) {
            videoHolder.adVideoTitle.setText(title);
        } else {
            SampleLog.e(TAG, "attach view title " + title);
        }
        String desc = adInfo.getDesc();
        if (!TextUtils.isEmpty(desc)) {
            videoHolder.adVideoDesc.setText(desc);
        } else {
            SampleLog.e(TAG, "attach view dec " + desc);
        }
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            videoHolder.adVideoThumb.setImageBitmap(bitmap);
        }
        int actionType = adInfo.getActionType();
        switch (actionType) {
            case ACTION_TYPE_BROWSER:
                videoHolder.adVideoAction.setText("点击查看");
                break;
            case ACTION_TYPE_DOWNLOAD:
                videoHolder.adVideoAction.setText("点击下载");
                break;
            default:
                videoHolder.adVideoAction.setText("什么鬼。。。");
                break;
        }
    }

    class ViewHolder {
        TextView adTitle;
        ImageView adView;
        TextView adDesc;
        TextView adAction;
    }

    class VideoViewHolder {
        TextView adVideoTitle;
        TextureView adVideoTexture;
        ImageView adVideoThumb;
        ImageView adVideoCotroller;
        ProgressBar adVideoProgress;
        TextView adVideoDesc;
        TextView adVideoAction;
    }
}

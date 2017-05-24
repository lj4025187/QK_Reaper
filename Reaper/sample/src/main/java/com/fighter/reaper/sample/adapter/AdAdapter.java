package com.fighter.reaper.sample.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fighter.loader.ReaperApi;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.activities.WebViewActivity;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.holders.BaseItemHolder;
import com.fighter.reaper.sample.holders.PicItemHolder;
import com.fighter.reaper.sample.holders.VideoItemHolder;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.utils.SampleLog;
import com.fighter.reaper.sample.utils.ToastUtil;
import com.fighter.reaper.sample.videolist.visibility.items.ListItem;
import com.fighter.reaper.sample.videolist.visibility.scroll.ItemsProvider;
import com.fighter.reaper.sample.videolist.widget.CircularProgressBar;
import com.fighter.reaper.sample.videolist.widget.TextureVideoView;

import java.io.File;
import java.util.List;

import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_BROWSER;
import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_DOWNLOAD;
import static com.fighter.reaper.sample.config.SampleConfig.VIDEO_AD_TYPE;

/**
 * Created by Administrator on 2017/5/22.
 */

public class AdAdapter extends BaseAdapter implements ItemsProvider {

    private final static String TAG = Adapter.class.getSimpleName();

    private int VIEW_TYPE_COUNT = 4;
    private Context mContext;
    private ListView mListView;
    private List<? extends BaseItem> mList;
    private ArrayMap<BaseItemHolder, Integer> mHolderHelper;

    public AdAdapter(Context context) {
        mContext = context;
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
        ReaperApi.AdInfo adInfo = mList.get(position).getAdInfo();
        SampleLog.i(TAG, " ad " + position + " contentType : " + adInfo.getContentType());
        BaseItemHolder baseItemHolder = null;
        int contentType = adInfo.getContentType();
        if (convertView == null) {
            switch (contentType) {
                case VIDEO_AD_TYPE:
                    baseItemHolder = new VideoItemHolder(contentType);
                    convertView = getBaseAdView(baseItemHolder);
                    convertView.setTag(baseItemHolder);
                    break;
                default:
                    baseItemHolder = new PicItemHolder(contentType);
                    convertView = getBaseAdView(baseItemHolder);
                    convertView.setTag(baseItemHolder);
                    break;
            }
        } else {
            baseItemHolder = (BaseItemHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.setAction(SampleConfig.OPEN_WEB_ACTION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        attachInfoToBase(adInfo, baseItemHolder);
        mHolderHelper.put(baseItemHolder, position);
        return convertView;
    }


    /**
     * find every view to holder
     *
     * @param baseItemHolder
     * @return
     */
    private View getBaseAdView(BaseItemHolder baseItemHolder) {
        View convertView = null;
        int holderType = baseItemHolder.getType();
        switch (holderType) {
            case VIDEO_AD_TYPE:
                convertView = getVideoAdView(baseItemHolder);
                break;
            default:
                convertView = getCustomAdView(baseItemHolder);
                break;
        }
        if (convertView == null) {
            TextView errView = new TextView(mContext);
            errView.setText("广告界面飞走了。。。");
            return errView;
        }
        return convertView;
    }

    private View getVideoAdView(BaseItemHolder baseItemHolder) {
        if (!(baseItemHolder instanceof VideoItemHolder))
            return null;
        VideoItemHolder videoItemHolder = (VideoItemHolder) baseItemHolder;
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item_video_layout, null);
        videoItemHolder.adVideoTitle = (TextView) convertView.findViewById(R.id.id_video_ad_title);
        videoItemHolder.adVideoTexture = (TextureVideoView) convertView.findViewById(R.id.id_video_texture_view);
        videoItemHolder.adVideoController = (ImageView) convertView.findViewById(R.id.id_video_controller);
        videoItemHolder.adVideoProgress = (CircularProgressBar) convertView.findViewById(R.id.id_video_progress);
        videoItemHolder.adVideoDesc = (TextView) convertView.findViewById(R.id.id_ad_custom_desc);
        videoItemHolder.adVideoAction = (TextView) convertView.findViewById(R.id.id_ad_custom_action);
        return convertView;
    }

    private View getCustomAdView(BaseItemHolder baseItemHolder) {
        if (!(baseItemHolder instanceof PicItemHolder))
            return null;
        PicItemHolder picItemHolder = (PicItemHolder) baseItemHolder;
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item_layout, null);
        picItemHolder.adTitle = (TextView) convertView.findViewById(R.id.id_ad_custom_title);
        picItemHolder.adView = (ImageView) convertView.findViewById(R.id.id_ad_image_view);
        picItemHolder.adDesc = (TextView) convertView.findViewById(R.id.id_ad_custom_desc);
        picItemHolder.adAction = (TextView) convertView.findViewById(R.id.id_ad_custom_action);
        return convertView;
    }


    /**
     * attach base info
     *
     * @param adInfo
     * @param baseItemHolder
     */
    private void attachInfoToBase(ReaperApi.AdInfo adInfo, BaseItemHolder baseItemHolder) {
        int contentType = adInfo.getContentType();
        switch (contentType) {
            case VIDEO_AD_TYPE:
                attachVideoToView(adInfo, baseItemHolder);
                break;
            default:
                attachInfoToView(adInfo, baseItemHolder);
                break;
        }
    }

    /**
     * attach custom ad info to view holder
     *
     * @param adInfo
     * @param baseItemHolder
     */
    private void attachInfoToView(ReaperApi.AdInfo adInfo, BaseItemHolder baseItemHolder) {
        if (!(baseItemHolder instanceof PicItemHolder))
            return;
        PicItemHolder viewHolder = (PicItemHolder) baseItemHolder;
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
     * @param baseItemHolder
     */
    private void attachVideoToView(final ReaperApi.AdInfo adInfo, BaseItemHolder baseItemHolder) {
        if (!(baseItemHolder instanceof VideoItemHolder))
            return;
        VideoItemHolder videoHolder = (VideoItemHolder) baseItemHolder;
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
        }
        videoHolder.adVideoAction.setText("羞羞羞。。。");
        videoHolder.adVideoController.setImageResource(R.mipmap.bt_play);
        videoHolder.adVideoController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.getInstance(mContext).showSingletonToast("又挑逗我");
                String videoUrl = adInfo.getVideoUrl();
            }
        });
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

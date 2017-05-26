package com.fighter.reaper.sample.holders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.utils.SampleLog;


import java.io.File;

import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_DOWNLOAD;
import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_BROWSER;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_TYPE_KEY;

/**
 * Created by Administrator on 2017/5/24.
 */

public class BaseItemHolder<T extends BaseItem> {

    protected String TAG = BaseItemHolder.class.getSimpleName();

    public View baseView;
    protected Context context;

    protected TextView viewType;
    protected TextView detailType;
    protected TextView srcName;

    public TextView adTitle;
    public ImageView adView;
    public TextView adDesc;
    public TextView adAction;

    public BaseItemHolder(View itemView) {
        baseView = itemView;
        context = itemView.getContext();
        initView(itemView);
    }

    private void initView(View itemView) {
        viewType = (TextView) itemView.findViewById(R.id.id_ad_view_type);
        detailType = (TextView) itemView.findViewById(R.id.id_ad_detail_type);
        srcName = (TextView) itemView.findViewById(R.id.id_ad_src_name);
        adTitle = (TextView) itemView.findViewById(R.id.id_ad_custom_title);
        adView = (ImageView) itemView.findViewById(R.id.id_ad_image_view);
        adDesc = (TextView) itemView.findViewById(R.id.id_ad_custom_desc);
        adAction = (TextView) itemView.findViewById(R.id.id_ad_custom_action);
    }

    public void onAttachView(int position, T iItem) {
        AdInfo adInfo = iItem.getAdInfo();
        viewType.setText(SampleConfig.getViewTypeString(context, iItem.getViewType()));
        detailType.setText((String) adInfo.getExtra(DETAIL_TYPE_KEY));
        srcName.setText(SampleConfig.getAdSrcName(context, adInfo));

        String title = adInfo.getTitle();
        adTitle.setText(TextUtils.isEmpty(title) ? context.getString(R.string.ad_item_title_default) : title);
        String desc = adInfo.getDesc();
        adDesc.setText(TextUtils.isEmpty(desc) ? context.getString(R.string.ad_item_desc_default) : desc);
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            SampleLog.i(TAG, " file path " + imageFile.getAbsolutePath());
            if (imageFile.getName().endsWith(".gif")) {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(adView);
            } else {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asBitmap()
                        .into(adView);
            }
        } else {
//            Glide.with(baseView.getContext())
//                    .load(adInfo.getImgUrl())
//                    .into(adView);
        }
        int actionType = adInfo.getActionType();
        switch (actionType) {
            case ACTION_TYPE_BROWSER:
                adAction.setText(context.getString(R.string.ad_pic_action));
                break;
            case ACTION_TYPE_DOWNLOAD:
                adAction.setText(context.getString(R.string.ad_app_action));
                break;
            default:
                adAction.setText(context.getString(R.string.ad_unknown_action));
                break;
        }
    }
}

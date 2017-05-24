package com.fighter.reaper.sample.holders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.PicItem;

import java.io.File;

import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_BROWSER;
import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_DOWNLOAD;

/**
 * Created by Administrator on 2017/5/24.
 */

public class PicItemHolder extends BaseItemHolder<PicItem> {

    private static final String TAG = PicItemHolder.class.getSimpleName();

    public TextView adTitle;
    public ImageView adView;
    public TextView adDesc;
    public TextView adAction;

    public PicItemHolder(View view) {
        super(view);
        initView(view);
    }

    private void initView(View view) {
        adTitle = (TextView) view.findViewById(R.id.id_ad_custom_title);
        adView = (ImageView) view.findViewById(R.id.id_ad_image_view);
        adDesc = (TextView) view.findViewById(R.id.id_ad_custom_desc);
        adAction = (TextView) view.findViewById(R.id.id_ad_custom_action);
    }

    @Override
    public int getType() {
        return SampleConfig.PICTURE_AD_TYPE;
    }

    @Override
    public void onAttachView(int position, PicItem iItem) {
        AdInfo adInfo = iItem.getAdInfo();
        String title = adInfo.getTitle();
        adTitle.setText(TextUtils.isEmpty(title) ? context.getString(R.string.ad_item_title_default) : title);
        String desc = adInfo.getDesc();
        adDesc.setText(TextUtils.isEmpty(desc) ? context.getString(R.string.ad_item_desc_default) : desc);
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            if(imageFile.getName().endsWith(".gif")) {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(adView);
            } else {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(adView);
            }
        } else {
            Glide.with(baseView.getContext())
                    .load(adInfo.getImgUrl())
                    .into(adView);
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

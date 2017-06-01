package com.fighter.reaper.sample.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.BaseItem;
import com.fighter.reaper.sample.utils.SampleLog;
import com.fighter.reaper.sample.utils.ViewUtils;


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

    protected TextView uuid;

    protected TextView imageSize;

    public TextView adTitle;
    public ImageView adView;

    public ViewGroup adDesParent;
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

        uuid = (TextView) itemView.findViewById(R.id.id_ad_uuid);

        imageSize = (TextView) itemView.findViewById(R.id.id_ad_image_size);

        adTitle = (TextView) itemView.findViewById(R.id.id_ad_custom_title);
        adView = (ImageView) itemView.findViewById(R.id.id_ad_image_view);
        adDesParent = (ViewGroup) itemView.findViewById(R.id.id_ad_custom_desc_action);
        adDesc = (TextView) itemView.findViewById(R.id.id_ad_custom_desc);
        adAction = (TextView) itemView.findViewById(R.id.id_ad_custom_action);
    }

    public void onAttachView(int position, T iItem) {
        AdInfo adInfo = iItem.getAdInfo();
        viewType.setText(SampleConfig.getViewTypeString(context, iItem.getViewType()));
        detailType.setText((String) adInfo.getExtra(DETAIL_TYPE_KEY));
        srcName.setText(SampleConfig.getAdSrcName(context, adInfo));
        uuid.setText("uuid:" + adInfo.getUuid());

        String title = adInfo.getTitle();
        if(TextUtils.isEmpty(title)) {
            ViewUtils.setViewVisibility(adTitle, View.GONE);
        } else {
            adTitle.setText(title);
        }

        String desc = adInfo.getDesc();
        if(TextUtils.isEmpty(desc)) {
            ViewUtils.setViewVisibility(adDesc, View.GONE);
        } else {
            adDesc.setText(desc);
        }

        final File imageFile = adInfo.getImgFile();
        String imageUrl = adInfo.getImgUrl();
        if(imageFile == null) {
            if(TextUtils.isEmpty(imageUrl)) {
                ViewUtils.setViewVisibility(adView, View.GONE);
            } else {
                Glide.with(baseView.getContext())
                    .load(imageUrl)
                    .into(adView);
            }
        } else {
            SampleLog.i(TAG, " file path " + imageFile.getAbsolutePath());
            if (imageFile.getName().endsWith(".gif")) {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(adView);
            } else {
//                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//                adView.setImageBitmap(bitmap);
            }
            Glide.with(baseView.getContext())
                    .load(imageFile)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            int imageWidth = resource.getWidth();
                            int imageHeight = resource.getHeight();
                            boolean isGif = imageFile.getName().endsWith(".gif");
                            if(!isGif) {
                                adView.setImageBitmap(resource);
                            }
                            imageSize.setText((isGif ? "gif-" : "jpg-") + "W:H---" + imageWidth + "*" + imageHeight);
                        }
                    });

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

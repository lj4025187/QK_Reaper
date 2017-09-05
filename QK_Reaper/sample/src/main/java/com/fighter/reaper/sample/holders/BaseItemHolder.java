package com.fighter.reaper.sample.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
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
import java.util.List;

import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_BROWSER;
import static com.fighter.reaper.sample.config.SampleConfig.ACTION_TYPE_DOWNLOAD;
import static com.fighter.reaper.sample.config.SampleConfig.DETAIL_TYPE_KEY;

/**
 * Created by Administrator on 2017/5/24.
 */

public class BaseItemHolder<T extends BaseItem> {

    protected String TAG = BaseItemHolder.class.getSimpleName();

    public View baseView;
    protected Context context;

    protected TextView indexView;
    protected TextView viewType;
    protected TextView detailType;
    protected TextView srcName;

    protected TextView uuid;

    protected TextView imageSize;

    public TextView adTitle;
    public ImageView adViewFront;
    public ImageView adView;
    public ImageView adViewBehind;

    public ViewGroup adDesParent;
    public TextView adDesc;
    public TextView adAction;

    public BaseItemHolder(View itemView) {
        baseView = itemView;
        context = itemView.getContext();
        initView(itemView);
    }

    private void initView(View itemView) {
        indexView = (TextView) itemView.findViewById(R.id.id_ad_item_index);
        viewType = (TextView) itemView.findViewById(R.id.id_ad_view_type);
        detailType = (TextView) itemView.findViewById(R.id.id_ad_detail_type);
        srcName = (TextView) itemView.findViewById(R.id.id_ad_src_name);

        uuid = (TextView) itemView.findViewById(R.id.id_ad_uuid);

        imageSize = (TextView) itemView.findViewById(R.id.id_ad_image_size);

        adTitle = (TextView) itemView.findViewById(R.id.id_ad_custom_title);
        adViewFront = (ImageView) itemView.findViewById(R.id.id_ad_image_view_front);
        adViewBehind = (ImageView) itemView.findViewById(R.id.id_ad_image_view_behind);
        adView = (ImageView) itemView.findViewById(R.id.id_ad_image_view);
        adDesParent = (ViewGroup) itemView.findViewById(R.id.id_ad_custom_desc_action);
        adDesc = (TextView) itemView.findViewById(R.id.id_ad_custom_desc);
        adAction = (TextView) itemView.findViewById(R.id.id_ad_custom_action);
    }

    public void onAttachView(int position, T iItem) {
        int index = position / SampleConfig.REQUEST_COUNT_PER_TIME;
        indexView.setText(String.valueOf(index + 1));
        if (index % 2 != 0) {
            baseView.setBackgroundColor(Color.CYAN);
            adDesParent.setBackground(new ColorDrawable(Color.CYAN));
        } else {
            baseView.setBackgroundColor(Color.parseColor("#FFFBFBFC"));
            adDesParent.setBackgroundColor(Color.parseColor("#FFFBFBFC"));
        }
        final AdInfo adInfo = iItem.getAdInfo();
        String adSrcName = SampleConfig.getAdSrcName(context, adInfo);
        viewType.setText(SampleConfig.getViewTypeString(context, iItem.getViewType()));
        detailType.setText((String) adInfo.getExtra(DETAIL_TYPE_KEY));
        srcName.setText(adSrcName);
        uuid.setText("uuid:" + adInfo.getUuid());
        boolean isBaxin = TextUtils.equals(SampleConfig.BAXIN_SRC_NAME, adSrcName);

        String title = isBaxin ? getBullEyeValue(adInfo, SampleConfig.BullsEyeKey.KEY_TITLE) : adInfo.getTitle();
        if (TextUtils.isEmpty(title)) {
            ViewUtils.setViewVisibility(adTitle, View.GONE);
        } else {
            adTitle.setText(title);
        }

        String desc = adInfo.getDesc();
        if (TextUtils.isEmpty(desc)) {
            ViewUtils.setViewVisibility(adDesc, View.GONE);
        } else {
            adDesc.setText(desc);
        }

        final String localId = (String) adInfo.getExtra("adLocalPosId");
        if (adInfo.getContentType() == AdInfo.CONTENT_MULTI_PICTURES) {//多图,目前是三图
            List<String> imgUrls = adInfo.getImgUrls();
            List<File> imgFiles = adInfo.getImgFiles();
            if (imgUrls == null || imgUrls.isEmpty()) {
                SampleLog.e(TAG, "CONTENT_MULTI_PICTURES get urls null ");
            } else {
                SampleLog.i(TAG, "CONTENT_MULTI_PICTURES get urls size " + imgUrls.size());
            }
            ViewUtils.setViewVisibility(adViewFront, View.VISIBLE);
            ViewUtils.setViewVisibility(adViewBehind, View.VISIBLE);
            setImageSize(null, false, localId);
            bindViewImage(imgUrls, imgFiles);
        } else {//单图
            final File imageFile = adInfo.getImgFile();
            String imageUrl = adInfo.getImgUrl();
            if (imageFile == null) {//图片文件未缓存
                if (TextUtils.isEmpty(imageUrl)) {
                    setImageSize(null, false, localId);
                    ViewUtils.setViewVisibility(adView, View.GONE);
                } else {
                    Glide.with(baseView.getContext())
                            .load(imageUrl)
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    setImageSize(resource, false, localId);
                                }
                            });
                }
            } else {//图片文件已缓存
                final boolean isGif = imageFile.getName().endsWith(".gif");
                if (isGif) {
                    Glide.with(baseView.getContext())
                            .load(imageFile)
                            .asGif()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .listener(new RequestListener<File, GifDrawable>() {
                                @Override
                                public boolean onException(Exception e, File file, Target<GifDrawable> target, boolean b) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GifDrawable gifDrawable, File file, Target<GifDrawable> target, boolean b, boolean b1) {
                                    Bitmap resource = gifDrawable.getFirstFrame();
                                    setImageSize(resource, isGif, localId);
                                    return false;
                                }
                            })
                            .into(adView);
                } else {
                    Glide.with(baseView.getContext())
                            .load(imageFile)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    setImageSize(resource, isGif, localId);
                                }
                            });
//                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//                adView.setImageBitmap(bitmap);
                }

            }
        }

        int actionType = adInfo.getActionType();
        String btnText = (String) adInfo.getExtra("btnText");
        switch (actionType) {
            case ACTION_TYPE_BROWSER:
                adAction.setText(TextUtils.isEmpty(btnText) ? context.getString(R.string.ad_pic_action) : btnText);
                break;
            case ACTION_TYPE_DOWNLOAD:
                adAction.setText(TextUtils.isEmpty(btnText) ? context.getString(R.string.ad_app_action) : btnText);
                break;
            default:
                adAction.setText(TextUtils.isEmpty(btnText) ? context.getString(R.string.ad_unknown_action) : btnText);
                break;
        }
    }

    private void bindViewImage(List<String> imgUrls, List<File> imgFiles) {
        int size = imgUrls.size();
        for (int i = 0; i < size; i++) {
            setBitmap(i, imgUrls.get(i), imgFiles == null ? null : imgFiles.get(i));
        }
    }

    private void setBitmap(int flag, String imageUrl, File imageFile) {
        switch (flag) {
            case 0:
                useGlideLoadImage(adViewFront, imageUrl, imageFile);
                break;
            case 1:
                useGlideLoadImage(adView, imageUrl, imageFile);
                break;
            case 2:
                useGlideLoadImage(adViewBehind, imageUrl, imageFile);
                break;
        }
    }

    private void useGlideLoadImage(final ImageView imageView, String imageUrl, File imageFile) {
        SampleLog.i(TAG, "useGlideLoadImage url " + imageUrl);
        if (imageFile == null || !imageFile.exists()) {
            Glide.with(baseView.getContext())
                    .load(imageUrl)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            imageView.setImageBitmap(resource);
                        }
                    });
        } else {
            final boolean isGif = imageFile.getName().endsWith(".gif");
            if (isGif) {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(new RequestListener<File, GifDrawable>() {
                            @Override
                            public boolean onException(Exception e, File file, Target<GifDrawable> target, boolean b) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable gifDrawable, File file, Target<GifDrawable> target, boolean b, boolean b1) {
                                Bitmap resource = gifDrawable.getFirstFrame();
                                imageView.setImageBitmap(resource);
                                return false;
                            }
                        })
                        .into(imageView);
            } else {
                Glide.with(baseView.getContext())
                        .load(imageFile)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                imageView.setImageBitmap(resource);
                            }
                        });
            }
        }
    }

    /**
     * For set image size width * height
     *
     * @param resource
     * @param isGif
     */
    private void setImageSize(Bitmap resource, boolean isGif, String localId) {
        if (resource == null) {
            imageSize.setText(localId);
            return;
        }
        int imageWidth = resource.getWidth();
        int imageHeight = resource.getHeight();
        if (!isGif) {
            adView.setImageBitmap(resource);
        }

        imageSize.setText((isGif ? "gif-" : "jpg-") + "W:H---" + imageWidth + "*" + imageHeight + "\n" + localId);
    }

    private String getBullEyeValue(AdInfo adInfo, String key) {
        String detailType = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.ADV_TYPE);
        boolean isMovie = false;
        boolean isCate = false;
        if (TextUtils.equals("2", detailType)) {
            isMovie = true;
        } else if (TextUtils.equals("3", detailType)) {
            isCate = true;
        }
        String result = "";
        String adv_source = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.ADV_SOURCE);
        String adv_type = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.ADV_TYPE);
        switch (key) {
            case SampleConfig.BullsEyeKey.KEY_TITLE:
                if (isMovie) {
                    result = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_NAME);
                    String movie_name = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_NAME);
                    String movie_rate = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_RATE);
                    String movie_show = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_SHOW);
                    String movie_dir = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_DIRECTOR);
                    String movie_star = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_STAR);
                    String movie_dur = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_DURATION);
                    String movie_ver = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_VERSION);
                    String movie_state = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.MOVIE_STATE);
                    SampleLog.i(TAG, " adv_source = " + adv_source + ";" +
                            "adv_type = " + adv_type + ";" +
                            "movie_name = " + movie_name + ";" +
                            "movie_rate = " + movie_rate + ";" +
                            "movie_show = " + movie_show + ";" +
                            "movie_dir = " + movie_dir + ";" +
                            "movie_star = " + movie_star + ";" +
                            "movie_dur = " + movie_dur + ";" +
                            "movie_ver = " + movie_ver + ";" +
                            "movie_state = " + movie_state + "(1=即将上映;3=正在热映;4=预售)");
                }
                if (isCate) {
                    result = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_SHOP);
                    String cate_city = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_CITY);
                    String cate_shop = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_SHOP);
                    String cate_class_name = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_CLASS_NAME);
                    String cate_type_name = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_TYPE_NAME);
                    String cate_type = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_TYPE);
                    String cate_area = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_AREA);
                    String cate_dist = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_DISTRICT);
                    String cate_lat = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_LATITUDE);
                    String cate_lon = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_LONGITUDE);
                    String cate_distance = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_DISTANCE);
                    String cate_adr = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_ADDRESS);
                    String cate_phone = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_PHONE);
                    String cate_rate = (String) adInfo.getExtra(SampleConfig.BullsEyeKey.CATE_RATE);
                    SampleLog.i(TAG, " adv_source = " + adv_source + ";" +
                            "adv_type = " + adv_type + ";" +
                            "cate_city = " + cate_city + ";" +
                            "cate_shop = " + cate_shop + ";" +
                            "cate_class_name = " + cate_class_name + ";" +
                            "cate_type_name = " + cate_type_name + ";" +
                            "cate_type = " + cate_type + ";" +
                            "cate_area = " + cate_area + ";" +
                            "cate_dist = " + cate_dist + ";" +
                            "cate_lat = " + cate_lat + ";" +
                            "cate_lon = " + cate_lon + ";" +
                            "cate_distance = " + cate_distance + ";" +
                            "cate_adr = " + cate_adr + ";" +
                            "cate_phone = " + cate_phone + ";" +
                            "cate_rate = " + cate_rate);
                }
                break;
        }
        return result;
    }
}

package com.fighter.reaper.sample.holders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.fighter.loader.AdInfo;
import com.fighter.reaper.sample.R;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.model.VideoItem;
import com.fighter.reaper.sample.model.VideoLoadMvpView;
import com.fighter.reaper.sample.target.VideoListGlideModule;
import com.fighter.reaper.sample.target.VideoLoadTarget;
import com.fighter.reaper.sample.target.VideoProgressTarget;
import com.fighter.reaper.sample.utils.ToastUtil;
import com.fighter.reaper.sample.videolist.visibility.items.ListItem;
import com.fighter.reaper.sample.videolist.widget.CircularProgressBar;
import com.fighter.reaper.sample.videolist.widget.TextureVideoView;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/5/24.
 */

public class VideoItemHolder extends BaseItemHolder<VideoItem>
        implements VideoLoadMvpView, ViewPropertyAnimatorListener, ListItem {

    private final static String TAG = VideoItemHolder.class.getSimpleName();

    public TextView adVideoTitle;
    public TextureVideoView adVideoTexture;
    public ImageView adVideoThumb;//image view
    public CircularProgressBar adVideoProgress;//progress bar
    public TextView adVideoDesc;
    public TextView adVideoAction;

    private String adVideoPath;
    private final VideoProgressTarget progressTarget;
    private final VideoLoadTarget videoTarget;

    private int videoState = STATE_IDLE;
    private static final int STATE_IDLE = 0;
    private static final int STATE_POSITIVE = 1;
    private static final int STATE_NEGATIVE = 2;

    public VideoItemHolder(View view) {
        super(view);
        initView(view);
        adVideoTexture.setAlpha(0);
        videoTarget = new VideoLoadTarget(this);
        progressTarget = new VideoProgressTarget(videoTarget, adVideoProgress);
    }

    private void initView(View view) {
        adVideoTitle = (TextView) view.findViewById(R.id.id_video_ad_title);
        adVideoTexture = (TextureVideoView) view.findViewById(R.id.id_video_texture_view);
        adVideoThumb = (ImageView) view.findViewById(R.id.id_video_thumb);
        adVideoProgress = (CircularProgressBar) view.findViewById(R.id.id_video_progress);
        adVideoDesc = (TextView) view.findViewById(R.id.id_ad_custom_desc);
        adVideoAction = (TextView) view.findViewById(R.id.id_ad_custom_action);
    }

    @Override
    public int getType() {
        return SampleConfig.VIDEO_AD_TYPE;
    }

    @Override
    public void onAttachView(int position, VideoItem iItem) {
        reset();
        final AdInfo adInfo = iItem.getAdInfo();
        String title = adInfo.getTitle();
        adVideoTitle.setText(TextUtils.isEmpty(title) ? context.getString(R.string.ad_item_title_default) : title);
        String desc = adInfo.getDesc();
        adVideoDesc.setText(TextUtils.isEmpty(desc) ? context.getString(R.string.ad_item_desc_default) : desc);
        File imageFile = adInfo.getImgFile();
        if (imageFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }
        adVideoAction.setText(context.getString(R.string.ad_video_action));
        adVideoThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.getInstance(v.getContext()).showSingletonToast(R.string.ad_video_play_toast);
                String videoUrl = adInfo.getVideoUrl();
            }
        });
        String videoUrl = adInfo.getVideoUrl();
        if (!TextUtils.isEmpty(videoUrl)) {
            Glide.with(baseView.getContext())
                    .using(VideoListGlideModule.getOkHttpUrlLoader(), InputStream.class)
                    .load(new GlideUrl(videoUrl))
                    .as(File.class)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(progressTarget);
        } else {
            Glide.with(baseView.getContext())
                    .load(adInfo.getImgUrl())
                    .placeholder(new ColorDrawable(0xffdcdcdc))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(adVideoThumb);
        }
    }

    private void reset() {
        videoState = STATE_IDLE;
        adVideoTexture.stop();
        adVideoPath = null;
        videoStopped();
    }

    @Override
    public void onAnimationStart(View view) {

    }

    @Override
    public void onAnimationEnd(View view) {
        view.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationCancel(View view) {

    }

    @Override
    public TextureVideoView getVideoView() {
        return adVideoTexture;
    }

    @Override
    public void videoBeginning() {
        adVideoTexture.setAlpha(1.f);
        cancelAlphaAnimate(adVideoThumb);
        startAlphaAnimate(adVideoThumb);
    }

    @Override
    public void videoStopped() {
        cancelAlphaAnimate(adVideoThumb);
        adVideoTexture.setAlpha(0);
        adVideoThumb.setAlpha(1.f);
        adVideoThumb.setVisibility(View.VISIBLE);
    }

    @Override
    public void videoPrepared(MediaPlayer player) {

    }

    @Override
    public void videoResourceReady(String videoPath) {
        adVideoPath = videoPath;
        if (adVideoPath != null) {
            adVideoTexture.setVideoPath(videoPath);
            if (videoState == STATE_POSITIVE) {
                adVideoTexture.start();
            }
        }
    }

    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {
        videoState = STATE_POSITIVE;
        if (adVideoPath != null) {
            adVideoTexture.setVideoPath(adVideoPath);
            adVideoTexture.start();
        }
    }

    @Override
    public void deactivate(View currentView, int position) {
        videoState = STATE_NEGATIVE;
        adVideoTexture.stop();
        videoStopped();
    }

    private void cancelAlphaAnimate(View v) {
        ViewCompat.animate(v).cancel();
    }

    private void startAlphaAnimate(View v) {
        ViewCompat.animate(v).setListener(this).alpha(0f);
    }
}

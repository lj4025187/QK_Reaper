package com.fighter.reaper.sample.holders;

import android.media.MediaPlayer;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fighter.reaper.sample.model.VideoItem;
import com.fighter.reaper.sample.model.VideoLoadMvpView;
import com.fighter.reaper.sample.videolist.visibility.items.ListItem;
import com.fighter.reaper.sample.videolist.widget.CircularProgressBar;
import com.fighter.reaper.sample.videolist.widget.TextureVideoView;

/**
 * Created by Administrator on 2017/5/24.
 */

public class VideoItemHolder extends BaseItemHolder<VideoItem>
        implements VideoLoadMvpView, ViewPropertyAnimatorListener, ListItem {
    public TextView adVideoTitle;
    public TextureVideoView adVideoTexture;
    public ImageView adVideoController;//control view play,pause,stop
    public CircularProgressBar adVideoProgress;//progress bar
    public TextView adVideoDesc;
    public TextView adVideoAction;

    public VideoItemHolder(int type) {
        super(type);
    }

    @Override
    public void onAnimationStart(View view) {

    }

    @Override
    public void onAnimationEnd(View view) {

    }

    @Override
    public void onAnimationCancel(View view) {

    }

    @Override
    public TextureVideoView getVideoView() {
        return null;
    }

    @Override
    public void videoBeginning() {

    }

    @Override
    public void videoStopped() {

    }

    @Override
    public void videoPrepared(MediaPlayer player) {

    }

    @Override
    public void videoResourceReady(String videoPath) {

    }

    @Override
    public void setActive(View newActiveView, int newActiveViewPosition) {

    }

    @Override
    public void deactivate(View currentView, int position) {

    }
}

package com.fighter.reaper.sample.model;

import android.media.MediaPlayer;

import com.fighter.reaper.sample.videolist.widget.TextureVideoView;

public interface VideoLoadMvpView {

    TextureVideoView getVideoView();

    void videoBeginning();

    void videoStopped();

    void videoPrepared(MediaPlayer player);

    void videoResourceReady(String videoPath);
}

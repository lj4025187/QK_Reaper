package com.fighter.ad;

public class AdEvent {
    // ----------------------------------------------------

    // 通用事件

    /**
     * 广告被曝光
     */
    public static final int EVENT_VIEW = 0;
    /**
     * 广告被点击
     */
    public static final int EVENT_CLICK = 1;
    /**
     * 广告被关闭
     */
    public static final int EVENT_CLOSE = 2;

    // ----------------------------------------------------

    // App类事件

    /**
     * App开始下载
     */
    public static final int EVENT_APP_START_DOWNLOAD = 10;
    /**
     * App下载完成
     */
    public static final int EVENT_APP_DOWNLOAD_COMPLETE = 11;
    /**
     * App下载失败
     */
    public static final int EVENT_APP_DOWNLOAD_FAILED = 12;
    /**
     * App下载取消
     */
    public static final int EVENT_APP_DOWNLOAD_CANCELED = 13;
    /**
     * App成功安装
     */
    public static final int EVENT_APP_INSTALL = 14;
    /**
     * App被用户激活
     */
    public static final int EVENT_APP_ACTIVE = 15;

    // ----------------------------------------------------

    // 视频事件

    /**
     * 点击预览图播放视频
     */
    public static final int EVENT_VIDEO_CARD_CLICK = 20;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_START_PLAY = 21;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_PAUSE = 22;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_CONTINUE = 23;
    /**
     * 视频播放完成
     */
    public static final int EVENT_VIDEO_PLAY_COMPLETE = 24;
    /**
     * 视频进入全屏
     */
    public static final int EVENT_VIDEO_FULLSCREEN = 25;
    /**
     * 视频中途被关闭
     */
    public static final int EVENT_VIDEO_EXIT = 26;

    // ----------------------------------------------------

    private AdEvent() {

    }
}

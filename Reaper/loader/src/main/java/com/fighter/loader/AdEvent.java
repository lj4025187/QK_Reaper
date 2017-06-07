package com.fighter.loader;

/**
 * 广告事件
 */

class AdEvent {
    /**
     * 广告曝光失败
     */
    public static final int EVENT_VIEW_FAIL = -1;
    /**
     * 广告被曝光
     */
    public static final int EVENT_VIEW_SUCCESS = 0;
    /**
     * 广告被点击
     */
    public static final int EVENT_CLICK = 1;
    /**
     * 广告被关闭
     */
    public static final int EVENT_CLOSE = 2;
    /**
     * App开始下载
     */
    public static final int EVENT_APP_START_DOWNLOAD = 10;
    /**
     * App下载完成
     */
    public static final int EVENT_APP_DOWNLOAD_COMPLETE = 11;
    /**
     * App成功安装
     */
    public static final int EVENT_APP_INSTALL = 12;
    /**
     * App被用户激活
     */
    public static final int EVENT_APP_ACTIVE = 13;
    /**
     * 点击预览图播放视频
     */
    public static final int EVENT_VIDEO_CARD_CLICK = 14;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_START_PLAY = 15;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_PAUSE = 16;
    /**
     * 视频开始播放
     */
    public static final int EVENT_VIDEO_CONTINUE = 17;
    /**
     * 视频播放完成
     */
    public static final int EVENT_VIDEO_PLAY_COMPLETE = 18;
    /**
     * 视频进入全屏
     */
    public static final int EVENT_VIDEO_FULLSCREEN = 19;
    /**
     * 视频中途被关闭
     */
    public static final int EVENT_VIDEO_EXIT = 20;
}

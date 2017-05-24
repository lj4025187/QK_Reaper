package com.fighter.loader;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.ArrayMap;
import android.view.View;

import java.io.File;
import java.util.Map;

/**
 * 广告信息
 */

public class AdInfo {

    /**
     * 文字类型
     */
    public static final int CONTENT_TYPE_TEXT = 1;
    /**
     * 纯图片类型
     */
    public static final int CONTENT_TYPE_PICTURE = 2;
    /**
     * 图文混合类型
     */
    public static final int CONTENT_TYPE_PICTURE_WITH_TEXT = 3;
    /**
     * 视频类型
     */
    public static final int CONTENT_TYPE_VIDEO = 4;

    /**
     * 点击跳转浏览器
     */
    public static final int ACTION_TYPE_BROWSER = 1;
    /**
     * 点击开始下载APP
     */
    public static final int ACTION_TYPE_APP_DOWNLOAD = 2;

    // ----------------------------------------------------

    Map<String, Object> mParams;
    private ReaperApi mReaperApi;

    AdInfo(ReaperApi reaperApi) {
        mReaperApi = reaperApi;
    }

    /**
     * 广告被展示
     *
     * @param v 展示广告所使用的view (不传时聚效将无法上报)
     */
    public void onAdShow(View v) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "view", v);
        onEvent(AdEvent.EVENT_VIEW, params);
    }

    /**
     * 广告被点击
     *
     * @param activity 广告所在activity (不传时聚效无法正常处理点击)
     * @param v        广告展示所在view (不传时聚效无法正常处理点击)
     * @param downX    广告所在view按下时的x坐标，获取不到填-999
     * @param downY    广告所在view按下时的y坐标，获取不到填-999
     * @param upX      广告所在view抬起时的x坐标，获取不到填-999
     * @param upY      广告所在view抬起时的y坐标，获取不到填-999
     */
    public void onAdClicked(Activity activity, View v,
                            int downX, int downY,
                            int upX, int upY) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "activity", activity);
        ReaperApi.putParam(params, "view", v);
        ReaperApi.putParam(params, "downX", downX);
        ReaperApi.putParam(params, "downY", downY);
        ReaperApi.putParam(params, "upX", upX);
        ReaperApi.putParam(params, "upY", upY);

        onEvent(AdEvent.EVENT_CLICK, params);
    }

    /**
     * 广告被用户关闭
     */
    public void onAdClose() {
        onEvent(AdEvent.EVENT_CLOSE, null);
    }

    /**
     * 用户点击视频广告预览界面，准备开始播放视频
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdCardClick(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_CARD_CLICK, params);
    }

    /**
     * 开始播放视频广告
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdStartPlay(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_START_PLAY, params);
    }

    /**
     * 视频广告被暂停
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdPause(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_PAUSE, params);
    }

    /**
     * 视频广告继续播放
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdContinue(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_CONTINUE, params);
    }

    /**
     * 视频广告播放完成
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdPlayComplete(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_PLAY_COMPLETE, params);
    }

    /**
     * 视频广告进入全屏播放
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdFullScreen(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_FULLSCREEN, params);
    }

    /**
     * 视频广告播放中途被退出
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdExit(int position) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_EXIT, params);
    }

    public int getContentType() {
        Object o = mParams.get("contentType");
        return o == null ? 0 : (int) o;
    }

    public int getActionType() {
        Object o = mParams.get("actionType");
        return o == null ? 0 : (int) o;
    }

    public String getImgUrl() {
        return (String) mParams.get("imgUrl");
    }

    public File getImgFile() {
        return (File) mParams.get("imgFile");
    }

    public String getVideoUrl() {
        return (String) mParams.get("videoUrl");
    }

    public String getTitle() {
        return (String) mParams.get("title");
    }

    public String getDesc() {
        return (String) mParams.get("desc");
    }

    public String getAppIconUrl() {
        return (String) mParams.get("appIconUrl");
    }

    public String getAppName() {
        return (String) mParams.get("appName");
    }

    public String getAppPackageName() {
        return (String) mParams.get("appPackageName");
    }

    public Object getExtra(String key) {
        return mParams.get(key);
    }

    @Override
    public String toString() {
        return "AdInfo{" +
                "mContentType=" + getContentType() +
                ", mActionType=" + getActionType() +
                ", mImgUrl='" + getImgUrl() + '\'' +
                ", mImgFile=" + getImgFile() +
                ", mVideoUrl='" + getVideoUrl() + '\'' +
                ", mTitle='" + getTitle() + '\'' +
                ", mDesc='" + getDesc() + '\'' +
                ", mAppIconUrl='" + getAppIconUrl() + '\'' +
                ", mAppName='" + getAppName() + '\'' +
                ", mAppPackageName='" + getAppPackageName() + '\'' +
                '}';
    }

    // ----------------------------------------------------

    private void onEvent(int event, Map<String, Object> extras) {
        Map<String, Object> params = new ArrayMap<>();
        ReaperApi.putParam(params, "event", event);
        ReaperApi.putParam(params, extras);

        mReaperApi.invokeReaperApi("onEvent", extras);
    }
}

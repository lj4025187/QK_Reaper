package com.fighter.loader;

import android.app.Activity;
import android.media.MediaPlayer;
import android.support.annotation.Keep;
import android.view.View;

import com.fighter.utils.LoaderLog;
import com.qiku.proguard.annotations.KeepAll;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告信息
 */
@KeepAll
public class AdInfo {

    /**
     * 文字类型
     */
    @Keep
    public static final int CONTENT_TYPE_TEXT = 1;
    /**
     * 纯图片类型
     */
    @Keep
    public static final int CONTENT_TYPE_PICTURE = 2;
    /**
     * 图文混合类型
     */
    @Keep
    public static final int CONTENT_TYPE_PICTURE_WITH_TEXT = 3;
    /**
     * 视频类型
     */
    @Keep
    public static final int CONTENT_TYPE_VIDEO = 4;
    /**
     * 多图
     */
    @Keep
    public static final int CONTENT_MULTI_PICTURES = 5;

    /**
     * 点击跳转浏览器
     */
    @Keep
    public static final int ACTION_TYPE_BROWSER = 1;
    /**
     * 点击开始下载APP
     */
    @Keep
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
     * @param v 展示广告所使用的view (不传时聚效广告源将无法上报)
     */
    public void onAdShow(View v) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "view", v);
        onEvent(v == null ? AdEvent.EVENT_VIEW_FAIL : AdEvent.EVENT_VIEW_SUCCESS, params);
    }

    /**
     * 广告被点击，点击后，由SDK处理点击事件(打开浏览器或是开始下载APP)
     *
     * @param activity 广告所在activity (不传时聚效无法正常处理点击)
     * @param v        广告展示所在view (不传时聚效无法正常处理点击)
     * @param downX    广告所在view按下时的x坐标，获取不到填-999(若为负值，直接返回，不进行事件上报)
     * @param downY    广告所在view按下时的y坐标，获取不到填-999(若为负值，直接返回，不进行事件上报)
     * @param upX      广告所在view抬起时的x坐标，获取不到填-999(若为负值，直接返回，不进行事件上报)
     * @param upY      广告所在view抬起时的y坐标，获取不到填-999(若为负值，直接返回，不进行事件上报)
     */
    public void onAdClicked(Activity activity, View v,
                            int downX, int downY,
                            int upX, int upY) {
        if(downX < 0 || downY < 0 || upX < 0 || upY < 0){
            LoaderLog.e("onAdClicked coordinate has negative number is invalid " +
                            "downX: " + downX + " downY: " + downY + " upX: " + upX + " upY: " + upY);
            return;
        }
        Map<String, Object> params = new HashMap<>();
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
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_CARD_CLICK, params);
    }

    /**
     * 开始播放视频广告
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdStartPlay(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_START_PLAY, params);
    }

    /**
     * 视频广告被暂停
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdPause(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_PAUSE, params);
    }

    /**
     * 视频广告继续播放
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdContinue(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_CONTINUE, params);
    }

    /**
     * 视频广告播放完成
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdPlayComplete(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_PLAY_COMPLETE, params);
    }

    /**
     * 视频广告进入全屏播放
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdFullScreen(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        onEvent(AdEvent.EVENT_VIDEO_FULLSCREEN, params);
    }

    /**
     * 视频广告播放中途被退出
     *
     * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
     */
    public void onVideoAdExit(int position) {
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "position", position);
        ReaperApi.putParam(params, mParams);
        onEvent(AdEvent.EVENT_VIDEO_EXIT, params);
    }

    /**
     * 获取广告返回的内容类型
     *
     * @return 广告内容类型
     * @see #CONTENT_TYPE_TEXT
     * @see #CONTENT_TYPE_PICTURE
     * @see #CONTENT_TYPE_PICTURE_WITH_TEXT
     * @see #CONTENT_TYPE_VIDEO
     */
    public int getContentType() {
        Object o = mParams.get("contentType");
        return o == null ? 0 : (int) o;
    }

    /**
     * 获取广告点击后的表现，如跳转浏览器展示网页，或者开始下载APP
     *
     * @return ActionType
     * @see #ACTION_TYPE_BROWSER
     * @see #ACTION_TYPE_APP_DOWNLOAD
     */
    public int getActionType() {
        Object o = mParams.get("actionType");
        return o == null ? 0 : (int) o;
    }

    /**
     * 返回展示图片的链接，{@link #getImgFile()}可返回已缓存好的图片文件，
     * 在图片文件失效时，可通过此链接重新获取并展示图片
     *
     * @return 图片URL链接
     */
    public String getImgUrl() {
        return (String) mParams.get("imgUrl");
    }

    /**
     * 返回展示图片的链接，{@link #getImgFiles()}可返回已缓存好的图片文件，
     * 在图片文件失效时，可通过此链接重新获取并展示图片
     *
     * @return 图片URL链接集合
     */
    public List<String> getImgUrls() {
        return (List<String>) mParams.get("imgUrls");
    }

    /**
     * 获取{@link #getImgUrl()}对应的图片文件，可以直接用来展示，不必再下载。
     * 图片文件可能格式包括 {@code .png}、{@code .jpg}、{@code .gif}，
     * 需注意兼容性，如选择{@code glide}等支持{@code .gif}播放的库作为图片展示工具。
     * 图片文件将在成功曝光后删除，调用曝光后，若需重新展示广告，请勿读取文件，而应重新请求新广告。
     * 在图片文件失效时，可通过{@link #getImgUrl()}请求图片。
     *
     * @return 图片文件
     */
    public File getImgFile() {
        String imgPath = (String) mParams.get("imgFile");
        if (imgPath != null) {
            File imgFile =  new File(imgPath);
            if (imgFile.exists()) {
                return imgFile;
            }
        }
        return null;
    }

    /**
     * 获取{@link #getImgUrls()}对应的图片文件，可以直接用来展示，不必再下载。
     * 图片文件可能格式包括 {@code .png}、{@code .jpg}、{@code .gif}，
     * 需注意兼容性，如选择{@code glide}等支持{@code .gif}播放的库作为图片展示工具。
     * 图片文件将在成功曝光后删除，调用曝光后，若需重新展示广告，请勿读取文件，而应重新请求新广告。
     * 在图片文件失效时，可通过{@link #getImgUrls()}请求图片。
     *
     * @return 图片文件
     */
    public List<File> getImgFiles() {
        return (List<File>) mParams.get("imgFiles");
    }

    /**
     * 对{@link #getContentType()}为{@link #CONTENT_TYPE_VIDEO}内容类型的广告，
     * 可以通过此方法获取视频广告的链接。
     *
     * @return 视频广告链接
     */
    public String getVideoUrl() {
        return (String) mParams.get("videoUrl");
    }

    /**
     * 广告标题(仅部分广告存在)
     *
     * @return 广告标题
     */
    public String getTitle() {
        return (String) mParams.get("title");
    }

    /**
     * 广告详细描述(仅部分广告存在)
     *
     * @return 广告描述
     */
    public String getDesc() {
        return (String) mParams.get("desc");
    }

    /**
     * 下载APP类的广告，可通过此链接获取APP图标(可能为空)
     *
     * @return APP图标链接
     */
    public String getAppIconUrl() {
        return (String) mParams.get("appIconUrl");
    }

    /**
     * 下载APP类的广告，获取APP名称(可能为空)
     *
     * @return APP名称
     */
    public String getAppName() {
        return (String) mParams.get("appName");
    }

    /**
     * 下载APP类的广告，获取APP的包名(可能为空)
     *
     * @return APP软件包名
     */
    public String getAppPackageName() {
        return (String) mParams.get("appPackageName");
    }

    /**
     *　广告的唯一标识
     *
     * @return 返回广告的唯一标识
     */
    public String getUuid() {
        return (String)mParams.get("uuid");
    }

    /**
     * 判断广告是否可用
     *
     * @return adInfo is available or not
     */
    public boolean isAvailable() {
        if (mParams.containsKey("isAvail")) {
            return (boolean) mParams.get("isAvail");
        } else {
            return true;
        }
    }

    /**
     * 对于不满足需求的业务，可通过此方法获取到更多信息。
     * 具体请于我们沟通。
     *
     * @param key 属性key值
     * @return 属性value值
     */
    public Object getExtra(String key) {
        return mParams.get(key);
    }



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
        Map<String, Object> params = new HashMap<>();
        ReaperApi.putParam(params, "event", event);
        ReaperApi.putParam(params, extras);
        ReaperApi.putParam(params, mParams);

        mReaperApi.invokeReaperApi("onEvent", params);
    }
}

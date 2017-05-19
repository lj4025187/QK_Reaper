package com.fighter.wrapper;

import android.text.TextUtils;
import android.util.ArrayMap;

import java.io.File;
import java.util.Map;

/**
 * 广告信息
 */
public class AdInfo {
    /**
     * 广告返回的内容类型
     */
    public interface ContentType {
        /**
         * 文字
         */
        int TEXT = 1;
        /**
         * 纯图片
         */
        int PICTURE = 2;
        /**
         * 图文混合
         */
        int PICTURE_WITH_TEXT = 3;
        /**
         * 视频
         */
        int VIDEO = 4;
    }

    public interface ActionType {
        /**
         * 跳转浏览器查看
         */
        int BROWSER = 1;
        /**
         * 开始下载广告展示的APP
         */
        int APP_DOWNLOAD = 2;
    }

    /**
     * 广告内容类型
     */
    private static final String KEY_CONTENT_TYPE = "contentType";
    /**
     * 广告点击表现类型
     */
    private static final String KEY_ACTION_TYPE = "actionType";
    /**
     * 广告图片链接，纯文字广告类型时该字段为空
     */
    private static final String KEY_IMG_URL = "imgUrl";
    /**
     * 广告图片文件
     */
    private static final String KEY_IMG_FILE = "imgFile";
    /**
     * 视频广告类型，视频内容链接
     */
    private static final String KEY_VIDEO_URL = "videoUrl";
    /**
     * 标题
     */
    private static final String KEY_TITLE = "title";
    /**
     * 描述
     */
    private static final String KEY_DESC = "desc";
    /**
     * 广告目标APP的logo链接，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_APP_ICON_URL = "appIconUrl";
    /**
     * 广告目标APP的名称，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_APP_NAME = "appName";
    /**
     * 广告目标APP的包名，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_APP_PACKAGE_NAME = "appPackageName";

    private Map<String, Object> mAdParams;

    public AdInfo() {
        mAdParams = new ArrayMap<>();
    }

    public int getContentType() {
        Object o = mAdParams.get(KEY_CONTENT_TYPE);
        return o == null ? ContentType.PICTURE : (int) o;
    }

    public void setContentType(int contentType) {
        putParam(KEY_CONTENT_TYPE, contentType);
    }

    public int getActionType() {
        Object o = mAdParams.get(KEY_ACTION_TYPE);
        return o == null ? ActionType.BROWSER : (int) o;
    }

    public void setActionType(int actionType) {
        putParam(KEY_ACTION_TYPE, actionType);
    }

    public String getImgUrl() {
        return (String) mAdParams.get(KEY_IMG_URL);
    }

    public void setImgUrl(String imgUrl) {
        putParam(KEY_IMG_URL, imgUrl);
    }

    public File getImgFile() {
        return (File) mAdParams.get(KEY_IMG_FILE);
    }

    public void setImgFile(File imgFile) {
        putParam(KEY_IMG_FILE, imgFile);
    }

    public String getVideoUrl() {
        return (String) mAdParams.get(KEY_VIDEO_URL);
    }

    public void setVideoUrl(String videoUrl) {
        putParam(KEY_VIDEO_URL, videoUrl);
    }

    public String getTitle() {
        return (String) mAdParams.get(KEY_TITLE);
    }

    public void setTitle(String title) {
        putParam(KEY_TITLE, title);
    }

    public String getDesc() {
        return (String) mAdParams.get(KEY_DESC);
    }

    public void setDesc(String desc) {
        putParam(KEY_DESC, desc);
    }

    public String getAppIconUrl() {
        return (String) mAdParams.get(KEY_APP_ICON_URL);
    }

    public void setAppIconUrl(String appIconUrl) {
        putParam(KEY_APP_ICON_URL, appIconUrl);
    }

    public String getAppName() {
        return (String) mAdParams.get(KEY_APP_NAME);
    }

    public void setAppName(String appName) {
        putParam(KEY_APP_NAME, appName);
    }

    public String getAppPackageName() {
        return (String) mAdParams.get(KEY_APP_PACKAGE_NAME);
    }

    public void setAppPackageName(String appPackageName) {
        putParam(KEY_APP_PACKAGE_NAME, appPackageName);
    }

    public Object getExtra(String key) {
        return mAdParams.get(key);
    }

    public void setExtra(String key, Object value) {
        putParam(key, value);
    }

    public Map<String, Object> getAdAllParams() {
        return mAdParams;
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

    private void putParam(String key, Object value) {
        if (!TextUtils.isEmpty(key) && value != null) {
            mAdParams.put(key, value);
        }
    }
}

package com.fighter.wrapper;

import java.util.Map;

/**
 * 广告信息
 */
public class AdInfo {
    /**
     * 广告返回的内容类型
     */
    public enum ContentType {
        /**
         * 文字
         */
        TEXT,
        /**
         * 纯图片
         */
        PICTURE,
        /**
         * 图文混合
         */
        PICTURE_WITH_TEXT,
        /**
         * 视频
         */
        VIDEO
    }

    public enum ActionType {
        /**
         * 跳转浏览器查看
         */
        BROWSER,
        /**
         * 开始下载广告展示的APP
         */
        APP_DOWNLOAD
    }

    /**
     * 广告内容类型
     */
    private ContentType mContentType;
    /**
     * 广告点击表现类型
     */
    private ActionType mActionType;
    /**
     * 广告图片链接，纯文字广告类型时该字段为空
     */
    private String mImgUrl;
    /**
     * 视频广告类型，视频内容链接
     */
    private String mVideoUrl;
    /**
     * 标题
     */
    private String mTitle;
    /**
     * 描述
     */
    private String mDesc;
    /**
     * 广告目标APP的logo链接，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private String mAppIconUrl;
    /**
     * 广告目标APP的名称，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private String mAppName;
    /**
     * 广告目标APP的包名，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private String mAppPackageName;

    /**
     * 扩展字段
     */
    private Map<String, Object> mExtras;

    public ContentType getContentType() {
        return mContentType;
    }

    public void setContentType(ContentType contentType) {
        mContentType = contentType;
    }

    public ActionType getActionType() {
        return mActionType;
    }

    public void setActionType(ActionType actionType) {
        mActionType = actionType;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        mImgUrl = imgUrl;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }

    public String getAppIconUrl() {
        return mAppIconUrl;
    }

    public void setAppIconUrl(String appIconUrl) {
        mAppIconUrl = appIconUrl;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        mAppPackageName = appPackageName;
    }

    public Map<String, Object> getExtras() {
        return mExtras;
    }

    public void setExtras(Map<String, Object> extras) {
        mExtras = extras;
    }

    @Override
    public String toString() {
        return "AdInfo{" +
                "mContentType=" + mContentType +
                ", mActionType=" + mActionType +
                ", mImgUrl='" + mImgUrl + '\'' +
                ", mVideoUrl='" + mVideoUrl + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mDesc='" + mDesc + '\'' +
                ", mAppIconUrl='" + mAppIconUrl + '\'' +
                ", mAppName='" + mAppName + '\'' +
                ", mAppPackageName='" + mAppPackageName + '\'' +
                ", mExtras=" + mExtras +
                '}';
    }
}

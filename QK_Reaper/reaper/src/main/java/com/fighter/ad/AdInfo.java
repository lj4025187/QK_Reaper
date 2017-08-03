package com.fighter.ad;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * 广告信息
 */
public class AdInfo {
    /**
     * 广告返回的内容类型
     */
    public class ContentType {
        /**
         * 文字
         */
        public static final int TEXT = 1;
        /**
         * 纯图片
         */
        public static final int PICTURE = 2;
        /**
         * 图文混合
         */
        public static final int PICTURE_WITH_TEXT = 3;
        /**
         * 视频
         */
        public static final int VIDEO = 4;

        private ContentType() {

        }
    }

    public class ActionType {
        /**
         * 跳转浏览器查看
         */
        public static final int BROWSER = 1;
        /**
         * 开始下载广告展示的APP
         */
        public static final int APP_DOWNLOAD = 2;

        private ActionType() {

        }
    }

    // ----------------------------------------------------

    private static final String CACHE_KEY = "_CACHE_KEY_";

    /**---------------------以下KEY开头的final String不能修改值，loader需要反射获取-------------------------------**/

    /**
     * 广告唯一标志 uuid生成
     */
    private static final String KEY_UUID = "uuid";
    /**
     * 广告超时失效时间
     */
    private static final String KEY_EXPIRE_TIME = "expire_time";
    /**
     * 广告是否支持静默安装
     */
    private static final String KEY_SILENT_INSTALL = "silent_install";
    /**
     * 广告内容类型
     */
    private static final String KEY_CONTENT_TYPE = "contentType";
    /**
     * 广告点击表现类型
     */
    private static final String KEY_ACTION_TYPE = "actionType";
    /**
     * 广告是否可缓存
     */
    private static final String KEY_CAN_CACHE = "canCache";
    /**
     * 广告来源
     *
     * @see SdkName
     */
    private static final String KEY_AD_NAME = "adName";
    /**
     * 360OS广告平台广告位ID
     */
    private static final String KEY_AD_POS_ID = "adPosId";
    /**
     * 360OS广告类型
     *
     * @see AdType
     */
    private static final String KEY_AD_TYPE = "adType";
    /**
     * 对应广告商平台的真实APP ID
     */
    private static final String KEY_AD_LOCAL_APP_ID = "adLocalAppId";
    /**
     * 对应广告商平台的真实广告位ID
     */
    private static final String KEY_AD_LOCAL_POS_ID = "adLocalPosId";
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
     * 广告文字内容
     */
    private static final String KEY_TEXT = "text";
    /**
     * 广告按钮文字
     */
    private static final String KEY_BTN_TEXT = "btntext";
    /**
     * 广告按钮url
     */
    private static final String KEY_BTN_URL = "btnurl";
    /**
     * 广告品牌名称
     */
    private static final String KEY_BRAND_NAME = "brandName";
    /**
     * 广告目标APP的logo链接，
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_APP_ICON_URL = "appIconUrl";
    /**
     * 返回的广告是否可用
     */
    private static final String KEY_ADINFO_AVAIL = "isAvail";

    /**
     * 广告目标APP的包名
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_DOWN_PKG_NAME = "appPackageName";
    /**
     * 广告目标APP的应用名称
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_DOWN_APP_NAME = "appName";
    /**
     * 广告目标APP的应用名称
     * 仅在表现类型为{@link ActionType#APP_DOWNLOAD}时有效
     */
    private static final String KEY_DOWN_APP_URL = "download_url";
    /**
     * App下载路径
     */
    private static final String KEY_APP_DOWNLOAD_FILE = "appDownloadFile";

    /**
     * AdInfo 对应的CacheInfo
     */
    private static final String KEY_CACHE_START_TIME = "adCacheStartTime";
    /**---------------------以上KEY开头的final String不能修改值，loader需要反射获取-------------------------------**/

    private Map<String, Object> mAdParams;

    public AdInfo() {
        mAdParams = new ArrayMap<>();
        setConstructTime(System.currentTimeMillis());
    }

    public String getUUID() {
        return (String) mAdParams.get(KEY_UUID);
    }

    public void generateUUID() {
        if (!mAdParams.containsKey(KEY_UUID)) {
            putParam(KEY_UUID, UUID.randomUUID().toString());
        }
    }

    public long getExpireTime() {
        Object o = mAdParams.get(KEY_EXPIRE_TIME);
        return o == null ? 0 : Long.valueOf(o.toString());
    }

    public void setExpireTime(long expireTime) {
        mAdParams.put(KEY_EXPIRE_TIME, expireTime);
    }

    public boolean getSilentInstall() {
        Object o = mAdParams.get(KEY_SILENT_INSTALL);
        return o != null && (boolean) o;
    }

    public void setSilentInstall(boolean silentInstall) {
        mAdParams.put(KEY_SILENT_INSTALL, silentInstall);
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

    public boolean canCache() {
        Object o = mAdParams.get(KEY_CAN_CACHE);
        return o != null && (boolean) o;
    }

    public void setCanCache(boolean canCache) {
        putParam(KEY_CAN_CACHE, canCache);
    }

    public String getAdName() {
        return (String) mAdParams.get(KEY_AD_NAME);
    }

    public void setAdName(String adName) {
        putParam(KEY_AD_NAME, adName);
    }

    public String getAdType() {
        return (String) mAdParams.get(KEY_AD_TYPE);
    }

    public void setAdType(String adType) {
        putParam(KEY_AD_TYPE, adType);
    }

    public String getAdPosId() {
        return (String) mAdParams.get(KEY_AD_POS_ID);
    }

    public void setAdPosId(String adPosId) {
        putParam(KEY_AD_POS_ID, adPosId);
    }

    public String getAdLocalAppId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_APP_ID);
    }

    public void setAdLocalAppId(String adLocalAppId) {
        putParam(KEY_AD_LOCAL_APP_ID, adLocalAppId);
    }

    public String getAdLocalPosId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_POS_ID);
    }

    public void setAdLocalPosId(String adLocalPosId) {
        putParam(KEY_AD_LOCAL_POS_ID, adLocalPosId);
    }

    public String getImgUrl() {
        return (String) mAdParams.get(KEY_IMG_URL);
    }

    public void setImgUrl(String imgUrl) {
        putParam(KEY_IMG_URL, imgUrl);
    }

    public File getImgFile() {
        String imgPath = (String) mAdParams.get(KEY_IMG_FILE);
        if (imgPath != null) {
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                return imgFile;
            }
        }
        return null;
    }

    public void setImgFile(String imgFilePath) {
        putParam(KEY_IMG_FILE, imgFilePath);
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

    public String getText() {
        return (String) mAdParams.get(KEY_TEXT);
    }

    public void setText(String text) {
        putParam(KEY_TEXT, text);
    }

    public String getBtnText(){
        return (String) mAdParams.get(KEY_BTN_TEXT);
    }

    public void setBtnText(String btnText) {
        putParam(KEY_BTN_TEXT, btnText);
    }

    public String getBtnUrl(){
        return (String) mAdParams.get(KEY_BTN_URL);
    }

    public void setBtnUrl(String btnUrl) {
        putParam(KEY_BTN_TEXT, btnUrl);
    }

    public String getBrandName(){
        return (String) mAdParams.get(KEY_BRAND_NAME);
    }

    public void setBrandName(String brandName) {
        putParam(KEY_BRAND_NAME, brandName);
    }

    public String getAppIconUrl() {
        return (String) mAdParams.get(KEY_APP_ICON_URL);
    }

    public void setAppIconUrl(String appIconUrl) {
        putParam(KEY_APP_ICON_URL, appIconUrl);
    }

    public String getDownPkgName() {
        return (String) mAdParams.get(KEY_DOWN_PKG_NAME);
    }

    public void setDownPkgName(String appPackageName) {
        putParam(KEY_DOWN_PKG_NAME, appPackageName);
    }

    public String getDownAppName() {
        return (String) mAdParams.get(KEY_DOWN_APP_NAME);
    }

    public void setDownAppName(String downAppName) {
        putParam(KEY_DOWN_APP_NAME, downAppName);
    }

    public String getDownAppUrl() {
        return (String) mAdParams.get(KEY_DOWN_APP_URL);
    }

    public void setDownAppUrl(String downAppUrl) {
        putParam(KEY_DOWN_APP_URL, downAppUrl);
    }

    public boolean getAdInfoAvailable() {
        Object available = mAdParams.get(KEY_ADINFO_AVAIL);
        if(available == null) return true;
        return (boolean) available;
    }

    public void setAdInfoAvailable(boolean state) {
        putParam(KEY_ADINFO_AVAIL, state);
    }

    public Object getExtra(String key) {
        return mAdParams.get(key);
    }

    public void setExtra(String key, Object value) {
        putParam(key, value);
    }

    public void setExtras(Map<String, Object> extras) {
        if (extras != null) {
            mAdParams.putAll(extras);
        }
    }

    public Map<String, Object> getAdAllParams() {
        return mAdParams;
    }

    public String getAppDownloadFile() {
        return (String) mAdParams.get(KEY_APP_DOWNLOAD_FILE);
    }

    public void setAppDownloadFile(String path) {
        putParam(KEY_APP_DOWNLOAD_FILE, path);
    }

    public long getConstructTime() {
        Object object = mAdParams.get(KEY_CACHE_START_TIME);
        if(object == null) return 0;
        return (long) object;
    }

    public void setConstructTime(long startMillions) {
        putParam(KEY_CACHE_START_TIME, startMillions);
    }

//    public void deleteImgFile() {
//        File f = getImgFile();
//        if (f != null && f.exists()) {
//            f.delete();
//        }
//    }

    public void deleteAppDownloadFile() {
        String path = getAppDownloadFile();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File f = new File(path);
        if (f != null && f.exists()) {
            f.delete();
        }
    }

    @Override
    public String toString() {
        return "AdInfo{" +
                "mContentType=" + getContentType() +
                ", mActionType=" + getActionType() +
                ", mUuid=" + getUUID() +
                ", mExpireTime=" + getExpireTime() +
                ", mSilentInstall=" + getSilentInstall() +
                ", mCanCache=" + canCache() +
                ", mAdSource=" + getAdName() +
                ", mAdPosId=" + getAdPosId() +
                ", mAdLocalAppId=" + getAdLocalAppId() +
                ", mAdLocalPosId=" + getAdLocalPosId() +
                ", mImgUrl='" + getImgUrl() + '\'' +
                ", mImgFile=" + getImgFile() +
                ", mVideoUrl='" + getVideoUrl() + '\'' +
                ", mTitle='" + getTitle() + '\'' +
                ", mDesc='" + getDesc() + '\'' +
                ", mExtText='" + getText() + '\'' +
                ", mBtnText='" + getBtnText() + '\'' +
                ", mBtnUrl='" + getBtnUrl() + '\'' +
                ", mBrandName='" + getBrandName() + '\'' +
                ", mAppIconUrl='" + getAppIconUrl() + '\'' +
                ", mAppName='" + getDownAppName() + '\'' +
                ", mAppPackageName='" + getDownPkgName() + '\'' +
                '}';
    }

    // ----------------------------------------------------

    public static String convertToString(AdInfo adInfo) {
        if (!adInfo.canCache()) {
            return null;
        }
        try {
            JSONObject json = new JSONObject();
            json.put(CACHE_KEY, adInfo.getAdAllParams());
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static AdInfo convertFromString(String cache) {
        try {
            JSONObject json = JSONObject.parseObject(cache);
            Map<String, Object> params = (Map<String, Object>) json.get(CACHE_KEY);
            if (params != null) {
                AdInfo adInfo = new AdInfo();
                adInfo.setExtras(params);
                return adInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------

    private void putParam(String key, Object value) {
        if (!TextUtils.isEmpty(key) && value != null) {
            mAdParams.put(key, value);
        }
    }
}

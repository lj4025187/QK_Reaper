package com.fighter.wrapper;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.ad.AdType;

import java.util.List;
import java.util.Map;

public class AdRequest {
    // ----------------------------------------------------
    // 必填
    // ----------------------------------------------------
    /**
     * 360OS广告平台Pos Id
     */
    private static final String KEY_AD_POS_ID = "adPosId";
    /**
     * 广告位超时失效时间
     */
    private static final String KEY_EXPIRE_TIME = "expire_time";
    /**
     * 广告位是否支持静默安装
     */
    private static final String KEY_SILENT_INSTALL = "silent_install";
    /**
     * 申请时广告商下发的App ID
     */
    private static final String KEY_AD_LOCAL_APP_ID = "adLocalAppId";
    /**
     * 申请时广告商广告位ID
     */
    private static final String KEY_AD_LOCAL_POSITION_ID = "adLocalPositionId";
    /**
     * 广告类型
     * {@link AdType}
     */
    private static final String KEY_AD_TYPE = "adType";
    /**
     * 广告位请求的广告数量<br></br>
     * <b>注意：</b>返回数量不一定就是设置的数量
     */
    private static final String KEY_AD_COUNT = "adCount";
    /**
     * 广告宽
     * 若为比例请填写到extras
     */
    private static final String KEY_AD_WIDTH = "adWidth";
    /**
     * 广告高
     * 若为比例请填写到extras
     */
    private static final String KEY_AD_HEIGHT = "adHeight";
    // ----------------------------------------------------
    // 选填 extras中可能有必填项，请参考各个SDKWrapper
    // ----------------------------------------------------
    /**
     * 广告关键词，广告商可能根据关键词优化广告返回结果
     */
    private static final String KEY_AD_KEY_WORDS = "adKeyWords";

    private Map<String, Object> mAdParams;

    // ----------------------------------------------------

    private AdRequest() {
        mAdParams = new ArrayMap<>();
    }

    // ----------------------------------------------------

    public String getAdPosId() {
        return (String) mAdParams.get(KEY_AD_POS_ID);
    }

    public long getExpireTime() {
        Object o = mAdParams.get(KEY_EXPIRE_TIME);
        return o == null ? 0 : (long) o;
    }

    public boolean getSilentInstall() {
        Object o = mAdParams.get(KEY_SILENT_INSTALL);
        return o != null && (boolean) o;
    }

    public String getAdLocalAppId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_APP_ID);
    }

    public String getAdLocalPositionId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_POSITION_ID);
    }

    public String getAdType() {
        return (String) mAdParams.get(KEY_AD_TYPE);
    }

    public int getAdCount() {
        Object o = mAdParams.get(KEY_AD_COUNT);
        return o == null ? 0 : (int) o;
    }

    public int getAdWidth() {
        Object o = mAdParams.get(KEY_AD_WIDTH);
        return o == null ? 0 : (int) o;
    }

    public int getAdHeight() {
        Object o = mAdParams.get(KEY_AD_HEIGHT);
        return o == null ? 0 : (int) o;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAdKeyWords() {
        return (List<String>) mAdParams.get(KEY_AD_KEY_WORDS);
    }

    public Object getAdExtra(String key) {
        return mAdParams.get(key);
    }

    public Map<String, Object> getAdAllParams() {
        return mAdParams;
    }

    @Override
    public String toString() {
        return "AdRequest{" +
                "mAdPosId='" + getAdPosId() + '\'' +
                ", mExpireTime='" + getExpireTime() + '\'' +
                ", mAdLocalAppId='" + getAdLocalAppId() + '\'' +
                ", mAdLocalPositionId='" + getAdLocalPositionId() + '\'' +
                ", mAdType=" + getAdType() +
                ", mAdCount=" + getAdCount() +
                ", mAdWidth=" + getAdWidth() +
                ", mAdHeight=" + getAdHeight() +
                ", mAdKeyWords=" + getAdKeyWords() +
                '}';
    }

    // ----------------------------------------------------
    public static class Builder {
        private Map<String, Object> mAdParams;

        public Builder() {
            mAdParams = new ArrayMap<>();
        }

        /**
         * 360OS 广告平台位置ID
         *
         * @param adPosId 奇酷广告位ID
         * @return
         */
        public Builder adPosId(String adPosId) {
            putParam(KEY_AD_POS_ID, adPosId);
            return this;
        }

        /**
         * 广告位超时失效时间
         *
         * @param expireTime 超时时间 单位：ms
         * @return 广告创建器
         */
        public Builder adExpireTime(long expireTime) {
            putParam(KEY_EXPIRE_TIME, expireTime);
            return this;
        }

        /**
         * 是否支持静默安装
         *
         * @param silentInstall 是否支持静默安装
         * @return 广告创建器
         */
        public Builder adSilentInstall(boolean silentInstall) {
            putParam(KEY_SILENT_INSTALL, silentInstall);
            return this;
        }

        /**
         * 填写应用向各个广告商申请的app id<br></br>
         * <b>注意：各个广告商申请到的ID不同</b>
         *
         * @param appId 应用ID
         * @return 广告创建器
         */
        public Builder adLocalAppId(String appId) {
            putParam(KEY_AD_LOCAL_APP_ID, appId);
            return this;
        }

        /**
         * 广告商处设置的广告位置ID
         *
         * @param positionId 广告位置ID
         * @return 广告创建器
         */
        public Builder adLocalPositionId(String positionId) {
            putParam(KEY_AD_LOCAL_POSITION_ID, positionId);
            return this;
        }

        /**
         * 广告类型
         *
         * @param type 广告类型{@link AdType}
         * @return 广告创建器
         */
        public Builder adType(String type) {
            putParam(KEY_AD_TYPE, type);
            return this;
        }

        /**
         * 设置请求广告位的广告数量
         *
         * @param adCount 广告数量
         * @return 广告创建器
         */
        public Builder adCount(int adCount) {
            putParam(KEY_AD_COUNT, adCount);
            return this;
        }

        /**
         * 广告宽度
         *
         * @param width 像素为单位的宽度，若比例，请填写比例
         * @return 广告创建器
         */
        public Builder adWidth(int width) {
            putParam(KEY_AD_WIDTH, width);
            return this;
        }

        /**
         * 广告宽度
         *
         * @param height 像素为单位的高度，若比例，请填写比例
         * @return 广告创建器
         */
        public Builder adHeight(int height) {
            putParam(KEY_AD_HEIGHT, height);
            return this;
        }

        /**
         * 广告关键词，广告商可根据此优化广告结果的返回
         *
         * @param keyWords 由于广告商对关键词数量规定不同，
         *                 多出部分将丢弃。
         *                 所以请将优先级高的靠前排列。
         *                 请参见各个{@code SdkWrapper}
         * @return 广告创建器
         */
        public Builder adKeyWords(List<String> keyWords) {
            putParam(KEY_AD_KEY_WORDS, keyWords);
            return this;
        }

        /**
         * 填写额外信息
         *
         * @param key   额外信息key值
         * @param value 取值
         * @return 广告创建器
         */
        public Builder adExtra(String key, Object value) {
            putParam(key, value);
            return this;
        }

        /**
         * 创建AdRequest实例
         *
         * @return {@link AdRequest}实例
         */
        public AdRequest create() {
            AdRequest adRequest = new AdRequest();
            adRequest.mAdParams = mAdParams;
            return adRequest;
        }

        private void putParam(String key, Object value) {
            if (!TextUtils.isEmpty(key) && value != null) {
                mAdParams.put(key, value);
            }
        }
    }
}

package com.fighter.wrapper;

import android.util.ArrayMap;

import java.util.List;
import java.util.Map;

public class AdRequest {
    // ----------------------------------------------------
    // 必填
    // ----------------------------------------------------
    /**
     * 申请时广告商下发的App ID
     */
    private String mAppId;
    /**
     * 广告位对应的广告位ID
     */
    private String mAdPositionId;
    /**
     * 广告类型
     * {@link AdType}
     */
    private int mAdType;
    /**
     * 广告位请求的广告数量<br></br>
     * <b>注意：</b>返回数量不一定就是设置的数量
     */
    private int mAdCount;
    /**
     * 广告宽
     * 若为比例请填写到extras
     */
    private int mAdWidth;
    /**
     * 广告高
     * 若为比例请填写到extras
     */
    private int mAdHeight;
    // ----------------------------------------------------
    // 选填 extras中可能有必填项，请参考各个SDKWrapper
    // ----------------------------------------------------
    /**
     * 广告关键词，广告商可能根据关键词优化广告返回结果
     */
    private List<String> mAdKeyWords;
    /**
     * 部分广告SDK需要的额外字段
     * 具体可参考各个{@code SDKWrapper}介绍。
     *
     * @see TencentSDKWrapper
     */
    private Map<String, Object> mAdExtras;

    // ----------------------------------------------------

    private AdRequest() {

    }

    // ----------------------------------------------------

    public String getAppId() {
        return mAppId;
    }

    public String getAdPositionId() {
        return mAdPositionId;
    }

    public int getAdType() {
        return mAdType;
    }

    public int getAdCount() {
        return mAdCount;
    }

    public int getAdWidth() {
        return mAdWidth;
    }

    public int getAdHeight() {
        return mAdHeight;
    }

    public List<String> getAdKeyWords() {
        return mAdKeyWords;
    }

    public Map<String, Object> getAdExtras() {
        return mAdExtras;
    }

    @Override
    public String toString() {
        return "AdRequest{" +
                "mAppId='" + mAppId + '\'' +
                ", mAdPositionId='" + mAdPositionId + '\'' +
                ", mAdType=" + mAdType +
                ", mAdCount=" + mAdCount +
                ", mAdWidth=" + mAdWidth +
                ", mAdHeight=" + mAdHeight +
                ", mAdKeyWords=" + mAdKeyWords +
                ", mAdExtras=" + mAdExtras +
                '}';
    }

    // ----------------------------------------------------
    public static class Builder {
        private String mAppId;
        private String mAdPositionId;
        private int mAdType;
        private int mAdCount;
        private int mAdWidth;
        private int mAdHeight;
        private List<String> mAdKeyWords;
        private Map<String, Object> mAdExtras;

        public Builder() {
            mAdExtras = new ArrayMap<>();
        }

        /**
         * 填写应用向各个广告商申请的app id<br></br>
         * <b>注意：各个广告商申请到的ID不同</b>
         *
         * @param appId 应用ID
         * @return 广告创建器
         */
        public Builder appId(String appId) {
            mAppId = appId;
            return this;
        }

        /**
         * 广告商处设置的广告位置ID
         *
         * @param positionId 广告位置ID
         * @return 广告创建器
         */
        public Builder adPositionId(String positionId) {
            mAdPositionId = positionId;
            return this;
        }

        /**
         * 广告类型
         *
         * @param type 广告类型{@link AdType}
         * @return 广告创建器
         */
        public Builder adType(int type) {
            mAdType = type;
            return this;
        }

        /**
         * 设置请求广告位的广告数量
         *
         * @param adCount 广告数量
         * @return 广告创建器
         */
        public Builder adCount(int adCount) {
            mAdCount = adCount;
            return this;
        }

        /**
         * 广告宽度
         *
         * @param width 像素为单位的宽度，若比例，请填写比例
         * @return 广告创建器
         */
        public Builder adWidth(int width) {
            mAdWidth = width;
            return this;
        }

        /**
         * 广告宽度
         *
         * @param height 像素为单位的高度，若比例，请填写比例
         * @return 广告创建器
         */
        public Builder adHeight(int height) {
            mAdHeight = height;
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
            mAdKeyWords = keyWords;
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
            mAdExtras.put(key, value);
            return this;
        }

        /**
         * 创建AdRequest实例
         *
         * @return {@link AdRequest}实例
         */
        public AdRequest create() {
            AdRequest adRequest = new AdRequest();
            adRequest.mAppId = mAppId;
            adRequest.mAdPositionId = mAdPositionId;
            adRequest.mAdType = mAdType;
            adRequest.mAdCount = mAdCount;
            adRequest.mAdWidth = mAdWidth;
            adRequest.mAdHeight = mAdHeight;
            adRequest.mAdKeyWords = mAdKeyWords;
            adRequest.mAdExtras = mAdExtras;
            return adRequest;
        }
    }
}

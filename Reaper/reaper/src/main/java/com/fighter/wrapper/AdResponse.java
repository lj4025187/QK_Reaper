package com.fighter.wrapper;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.ad.AdInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdResponse {
    /**
     * 广告请求是否成功
     */
    private static final String KEY_IS_SUCCEED = "isSucceed";
    /**
     * 广告请求出错信息，Json格式
     */
    private static final String KEY_ERR_MSG = "errMsg";
    /**
     * 360OS广告平台位置ID
     */
    private static final String KEY_AD_POS_ID = "adPosId";
    /**
     * 广告来源名称
     */
    private static final String KEY_AD_NAME = "adName";
    /**
     * 广告类型 {@link com.fighter.ad.AdType}
     */
    private static final String KEY_AD_TYPE = "adType";
    /**
     * 广告在各个广告商平台注册的ID
     */
    private static final String KEY_AD_LOCAL_APP_ID = "adLocalAppId";
    /**
     * 广告位在广告商处的真实ID
     */
    private static final String KEY_AD_LOCAL_POSITION_ID = "adLocalPositionId";
    /**
     * 原始响应字符串
     */
    private static final String KEY_ORI_RESPONSE = "oriResponse";
    /**
     * 返回的广告
     */
    private static final String KEY_AD_INFOS = "adInfos";
    /**
     * 返回的广告 map形式
     */
    private static final String KEY_AD_INFOS_MAP = "adInfosMap";
    /**
     * 是否支持缓存
     */
    private static final String KEY_CAN_CACHE = "canCache";

    private Map<String, Object> mAdParams;

    // ----------------------------------------------------

    private AdResponse() {
        mAdParams = new ArrayMap<>();
    }

    // ----------------------------------------------------

    public boolean isSucceed() {
        Object o = mAdParams.get(KEY_IS_SUCCEED);
        return o != null && (boolean) o;
    }

    public String getErrMsg() {
        return (String) mAdParams.get(KEY_ERR_MSG);
    }

    public String getAdPosId() {
        return (String) mAdParams.get(KEY_AD_POS_ID);
    }

    public String getAdName() {
        return (String) mAdParams.get(KEY_AD_NAME);
    }

    public String getAdType() {
        return (String) mAdParams.get(KEY_AD_TYPE);
    }

    public String getAdLocalAppId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_APP_ID);
    }

    public String getAdLocalPositionId() {
        return (String) mAdParams.get(KEY_AD_LOCAL_POSITION_ID);
    }

    public String getOriResponse() {
        return (String) mAdParams.get(KEY_ORI_RESPONSE);
    }

    @SuppressWarnings("unchecked")
    public List<AdInfo> getAdInfos() {
        return (List<AdInfo>) mAdParams.get(KEY_AD_INFOS);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAdInfosMap() {
        return (List<Map<String, Object>>) mAdParams.get(KEY_AD_INFOS_MAP);
    }

    public boolean canCache() {
        Object o = mAdParams.get(KEY_CAN_CACHE);
        return o != null && (boolean) o;
    }

    public Object getAdExtra(String key) {
        return mAdParams.get(key);
    }

    public Map<String, Object> getAdAllParams() {
        return mAdParams;
    }

    public void deleteImgFiles() {
        List<AdInfo> adInfos = getAdInfos();
        if (adInfos == null) {
            return;
        }

        for (AdInfo adInfo : adInfos) {
            adInfo.deleteImgFile();
        }
    }

    @Override
    public String toString() {
        return "AdResponse{" +
                "mIsSucceed=" + isSucceed() +
                ", mErrMsg='" + getErrMsg() + '\'' +
                ", mAdPosId='" + getAdPosId() + '\'' +
                ", mAdName='" + getAdName() + '\'' +
                ", mAdType='" + getAdType() + '\'' +
                ", mADLocalAppId='" + getAdLocalAppId() + '\'' +
                ", mAdLocalPositionId='" + getAdLocalPositionId() + '\'' +
                ", mOriResponse='" + getOriResponse() + '\'' +
                ", mAdInfos=" + getAdInfos() +
                '}';
    }

    // ----------------------------------------------------

    static class Builder {
        private Map<String, Object> mAdParams;

        Builder() {
            mAdParams = new ArrayMap<>();
        }

        Builder isSucceed(boolean isSucceed) {
            putParam(KEY_IS_SUCCEED, isSucceed);
            return this;
        }

        Builder errMsg(String errMsg) {
            putParam(KEY_ERR_MSG, errMsg);
            return this;
        }

        Builder adPosId(String adPosId) {
            putParam(KEY_AD_POS_ID, adPosId);
            return this;
        }

        Builder adName(String adName) {
            putParam(KEY_AD_NAME, adName);
            return this;
        }

        Builder adType(String adType) {
            putParam(KEY_AD_TYPE, adType);
            return this;
        }

        Builder adLocalAppId(String adLocalAppId) {
            putParam(KEY_AD_LOCAL_APP_ID, adLocalAppId);
            return this;
        }

        Builder adLocalPositionAd(String adLocalPositionId) {
            putParam(KEY_AD_LOCAL_POSITION_ID, adLocalPositionId);
            return this;
        }

        Builder oriResponse(String oriResponse) {
            putParam(KEY_ORI_RESPONSE, oriResponse);
            return this;
        }

        Builder adInfos(List<AdInfo> adInfos) {
            putParam(KEY_AD_INFOS, adInfos);
            if (adInfos != null) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (AdInfo adInfo : adInfos) {
                    list.add(adInfo.getAdAllParams());
                }
                putParam(KEY_AD_INFOS_MAP, list);
            }
            return this;
        }

        Builder canCache(boolean canCache) {
            putParam(KEY_CAN_CACHE, canCache);
            return this;
        }

        Builder adExtra(String key, Object value) {
            putParam(key, value);
            return this;
        }

        AdResponse create() {
            AdResponse adResponse = new AdResponse();
            adResponse.mAdParams = mAdParams;
            return adResponse;
        }

        private void putParam(String key, Object value) {
            if (!TextUtils.isEmpty(key) && value != null) {
                mAdParams.put(key, value);
            }
        }
    }
}

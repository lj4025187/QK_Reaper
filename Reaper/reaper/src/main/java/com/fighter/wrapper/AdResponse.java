package com.fighter.wrapper;

import android.text.TextUtils;
import android.util.ArrayMap;

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
     * 广告在各个广告商平台注册的ID
     */
    private static final String KEY_APP_ID = "appId";
    /**
     * 广告位在广告商处的真实ID
     */
    private static final String KEY_AD_POSITION_ID = "adPositionId";
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
     * 广告获取来源，各个SDK Wrapper
     */
    private static final String KEY_AD_FROM = "adFrom";
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

    public String getAppId() {
        return (String) mAdParams.get(KEY_APP_ID);
    }

    public String getAdPositionId() {
        return (String) mAdParams.get(KEY_AD_POSITION_ID);
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

    public int getAdFrom() {
        Object o = mAdParams.get(KEY_AD_FROM);
        return o == null ? 0 : (int) o;
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

    @Override
    public String toString() {
        return "AdResponse{" +
                "mIsSucceed=" + isSucceed() +
                ", mErrMsg='" + getErrMsg() + '\'' +
                ", mAppId='" + getAppId() + '\'' +
                ", mAdPositionId='" + getAdPositionId() + '\'' +
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

        Builder appId(String appId) {
            putParam(KEY_APP_ID, appId);
            return this;
        }

        Builder adPositionAd(String adPositionId) {
            putParam(KEY_AD_POSITION_ID, adPositionId);
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

        Builder adFrom(int adFrom) {
            putParam(KEY_AD_FROM, adFrom);
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

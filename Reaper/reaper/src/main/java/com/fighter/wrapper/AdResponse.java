package com.fighter.wrapper;

import java.util.List;
import java.util.Map;

public class AdResponse {
    /**
     * 广告请求是否成功
     */
    private boolean mIsSucceed;
    /**
     * 广告请求出错信息，Json格式
     */
    private String mErrMsg;
    /**
     * 广告位对应的广告位ID
     */
    private String mAdPositionId;
    /**
     * 原始响应字符串
     */
    private String mOriResponse;
    /**
     * 返回的广告
     */
    private List<AdInfo> mAdInfos;
    /**
     * 额外的广告响应信息，用于SDK返回结果差异化和后续扩展
     */
    private Map<String, Object> mExtras;

    // ----------------------------------------------------

    private AdResponse() {

    }

    // ----------------------------------------------------


    public boolean isSucceed() {
        return mIsSucceed;
    }

    public String getErrMsg() {
        return mErrMsg;
    }

    public String getAdPositionId() {
        return mAdPositionId;
    }

    public String getOriResponse() {
        return mOriResponse;
    }

    public List<AdInfo> getAdInfos() {
        return mAdInfos;
    }

    public Map<String, Object> getExtras() {
        return mExtras;
    }

    @Override
    public String toString() {
        return "AdResponse{" +
                "mIsSucceed=" + mIsSucceed +
                ", mErrMsg='" + mErrMsg + '\'' +
                ", mAdPositionId='" + mAdPositionId + '\'' +
                ", mOriResponse='" + mOriResponse + '\'' +
                ", mAdInfos=" + mAdInfos +
                ", mExtras=" + mExtras +
                '}';
    }

    // ----------------------------------------------------

    static class Builder {
        private boolean mIsSucceed;
        private String mErrMsg;
        private String mAdPositionAd;
        private String mOriResponse;
        private List<AdInfo> mAdInfos;
        private Map<String, Object> mExtras;

        Builder() {

        }

        Builder isSucceed(boolean isSucceed) {
            mIsSucceed = isSucceed;
            return this;
        }

        Builder errMsg(String errMsg) {
            mErrMsg = errMsg;
            return this;
        }

        Builder adPositionId(String positionId) {
            mAdPositionAd = positionId;
            return this;
        }

        Builder oriResponse(String oriResponse) {
            mOriResponse = oriResponse;
            return this;
        }

        Builder adInfos(List<AdInfo> adInfos) {
            mAdInfos = adInfos;
            return this;
        }

        Builder adExtras(Map<String, Object> extras) {
            mExtras = extras;
            return this;
        }

        AdResponse create() {
            AdResponse adResponse = new AdResponse();
            adResponse.mIsSucceed = mIsSucceed;
            adResponse.mErrMsg = mErrMsg;
            adResponse.mAdPositionId = mAdPositionAd;
            adResponse.mOriResponse = mOriResponse;
            adResponse.mAdInfos = mAdInfos;
            adResponse.mExtras = mExtras;
            return adResponse;
        }
    }
}

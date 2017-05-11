package com.fighter.wrapper;

import java.util.List;

public class AdResponse {
    /**
     * Http返回码
     */
    private int mHttpResponseCode;
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
     * 请求时的参数信息
     */
    private AdRequest mAdRequest;
    /**
     * 原始响应字符串
     */
    private String mOriResponse;
    /**
     * 返回的广告
     */
    private List<AdInfo> mAdInfos;

    // ----------------------------------------------------

    private AdResponse() {

    }

    // ----------------------------------------------------

    public int getHttpResponseCode() {
        return mHttpResponseCode;
    }

    public boolean isSucceed() {
        return mIsSucceed;
    }

    public String getErrMsg() {
        return mErrMsg;
    }

    public String getmAdPositionId() {
        return mAdPositionId;
    }

    public AdRequest getAdRequest() {
        return mAdRequest;
    }

    public String getOriResponse() {
        return mOriResponse;
    }

    public List<AdInfo> getAdInfos() {
        return mAdInfos;
    }

    @Override
    public String toString() {
        return "AdResponse{" +
                "mHttpResponseCode=" + mHttpResponseCode +
                ", mIsSucceed=" + mIsSucceed +
                ", mErrMsg='" + mErrMsg + '\'' +
                ", mAdPositionId='" + mAdPositionId + '\'' +
                ", mAdRequest=" + mAdRequest +
                ", mOriResponse='" + mOriResponse + '\'' +
                ", mAdInfos=" + mAdInfos +
                '}';
    }

    // ----------------------------------------------------

    static class Builder {
        private int mHttpResponseCode;
        private boolean mIsSucceed;
        private String mErrMsg;
        private String mAdPositionAd;
        private AdRequest mAdRequest;
        private String mOriResponse;
        private List<AdInfo> mAdInfos;

        Builder() {

        }

        Builder httpResponseCode(int httpResponseCode) {
            mHttpResponseCode = httpResponseCode;
            return this;
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

        Builder adRequest(AdRequest adRequest) {
            mAdRequest = adRequest;
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

        AdResponse create() {
            AdResponse adResponse = new AdResponse();
            adResponse.mHttpResponseCode = mHttpResponseCode;
            adResponse.mIsSucceed = mIsSucceed;
            adResponse.mErrMsg = mErrMsg;
            adResponse.mAdPositionId = mAdPositionAd;
            adResponse.mAdRequest = mAdRequest;
            adResponse.mOriResponse = mOriResponse;
            adResponse.mAdInfos = mAdInfos;
            return adResponse;
        }
    }
}

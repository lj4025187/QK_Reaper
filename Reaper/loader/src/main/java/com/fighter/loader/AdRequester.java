package com.fighter.loader;

import java.util.Map;

/**
 * 广告请求类，一个AdRequester对应一个360OS广告位ID，可通过此类请求对应广告。
 */

public class AdRequester {
    private ReaperApi mReaperApi;
    Map<String, Object> mParams;

    AdRequester(ReaperApi reaperApi) {
        mReaperApi = reaperApi;
    }

    /**
     * 开始请求广告
     * @param adCount 请求广告的条数
     */
    public void requestAd(int adCount) {
        mParams.put("adCount", adCount);
        mReaperApi.invokeReaperApi("requestAd", mParams);
    }

    // ----------------------------------------------------

    /**
     * 广告请求回调
     */
    public interface AdRequestCallback {
        /**
         * 广告请求成功
         *
         * @param adInfo 广告
         */
        void onSuccess(AdInfo adInfo);

        /**
         * 广告请求失败
         *
         * @param errMsg 失败原因
         */
        void onFailed(String errMsg);
    }
}

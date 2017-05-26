package com.fighter.loader;

import java.util.List;
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
     */
    public void requestAd() {
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
         * @param ads 广告，可能返回多个
         */
        void onSuccess(List<AdInfo> ads);

        /**
         * 广告请求失败
         *
         * @param errMsg 失败原因
         */
        void onFailed(String errMsg);
    }
}

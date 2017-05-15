package com.fighter.wrapper;

/**
 * 广告请求回调
 */
public interface AdResponseListener {
    /**
     * 请求广告回调接口
     *
     * @param adResponse 广告响应
     */
    void onAdResponse(AdResponse adResponse);
}

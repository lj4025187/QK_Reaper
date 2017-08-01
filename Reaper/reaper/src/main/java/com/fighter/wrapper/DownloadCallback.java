package com.fighter.wrapper;

import com.fighter.ad.AdInfo;

/**
 * Apk 下载回调，供{@link ISDKWrapper#setDownloadCallback(DownloadCallback)}使用
 */

public interface DownloadCallback {

    /**
     * Apk 下载回调
     *
     * @param adInfo           广告信息
     * @param apkDownloadEvent 下载事件
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_START_DOWNLOAD}
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_DOWNLOAD_COMPLETE}
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_DOWNLOAD_FAILED}
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_DOWNLOAD_CANCELED}
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_INSTALL}
     *                         {@link com.fighter.ad.AdEvent#EVENT_APP_ACTIVE}
     */
    void onDownloadEvent(AdInfo adInfo, int apkDownloadEvent);

    void onDownloadComplete(long reference, String fileName);

    void onDownloadFailed(long reference, int reason);
}

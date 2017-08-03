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

    /**
     * 静默安装失败后，调起安装器来安装，针对聚效处理
     *
     * @param key         由聚效返回的下载应用的key，需要传回去进行激活计算
     * @param apkPath     由聚效返回的apk下载的路径
     * @param packageName apk对应的包名
     */
    void onSilentInstallFailed(String key, String apkPath, String packageName);
}

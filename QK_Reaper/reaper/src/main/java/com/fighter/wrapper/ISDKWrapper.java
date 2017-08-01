package com.fighter.wrapper;

import android.app.Application;
import android.content.Context;

import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.common.utils.ReaperLog;

import java.util.Map;

public abstract class ISDKWrapper {
    private static final String TAG = ISDKWrapper.class.getSimpleName();

    // ----------------------------------------------------

    /**
     * 返回SDK版本号
     *
     * @return 版本号，如"1.0"
     */
    public abstract String getSdkVersion();

    /**
     * 返回Sdk真实名称
     *
     * @return SDK名称，参见{@link com.fighter.ad.SdkName}
     */
    public abstract String getSdkName();

    /**
     * 初始化SDK Wrapper
     *
     * @param appContext 应用Application的Context，建议在{@link Application#onCreate()}中调用
     * @param extras     附加参数，参考各个SDK Wrapper实现的类说明
     */
    public abstract void init(Context appContext, Map<String, Object> extras);

    /**
     * 销毁wrapper
     */
    public void uninit() {

    }

    /**
     * 是否支持同步请求广告
     * 不支持时，仅支持通过{@link #requestAdAsync(AdRequest, AdResponseListener)}异步获取广告
     *
     * @return true 支持  false 不支持
     */
    public abstract boolean isRequestAdSupportSync();

    /**
     * 请求广告，同步
     *
     * @param adRequest 请求参数
     * @return 请求广告响应结果
     */
    public AdResponse requestAdSync(AdRequest adRequest) {
        ReaperLog.e(TAG, getSdkName() + " requestAdSync not implement");
        return null;
    }

    /**
     * 请求广告，异步
     *
     * @param adRequest          请求参数
     * @param adResponseListener 请求广告回调接口
     */
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        ReaperLog.e(TAG, getSdkName() + " requestAdAsync not implement");
    }

    /**
     * 是否自己处理打开浏览器展示广告操作。
     * 若不支持自打开浏览器，需要通过{@link #requestWebUrl(AdInfo)}获取链接并用浏览器打开展示
     *
     * @return
     */
    public abstract boolean isOpenWebOwn();

    /**
     * 请求浏览器展示广告的链接
     *
     * @return
     */
    public String requestWebUrl(AdInfo adInfo) {
        ReaperLog.e(TAG, "requestWebUrl not implement");
        return null;
    }

    /**
     * 是否自己处理下载事件
     * 若SDK自己处理下载事件，需通过{@link #setDownloadCallback(DownloadCallback)}监听事件。
     * 若SDK不能自处理下载事件，需通过{@link #requestDownloadUrl(AdInfo)}获取下载链接
     *
     * @return
     */
    public abstract boolean isDownloadOwn();

    /**
     * 请求下载链接。(将修改AdInfo的额外信息，后续上报需要)
     *
     * @return
     */
    public String requestDownloadUrl(AdInfo adInfo) {
        ReaperLog.e(TAG, getSdkName() + " requestDownloadUrl not implement");
        return null;
    }

    /**
     * 自下载类型的SDK，可注册下载回调监听
     *
     * @param downloadCallback
     */
    public void setDownloadCallback(DownloadCallback downloadCallback) {
        ReaperLog.e(TAG, getSdkName() + " setDownloadCallback not implement");
    }

    /**
     * 上报对应的广告跟踪事件。
     * 事件类型为{@link AdEvent}中的一种。
     *
     * @param adEvent 事件类型
     * @param adInfo  广告信息
     */
    public abstract void onEvent(int adEvent, AdInfo adInfo);
}

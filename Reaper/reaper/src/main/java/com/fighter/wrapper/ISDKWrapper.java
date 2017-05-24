package com.fighter.wrapper;

import android.app.Application;
import android.content.Context;

import java.util.Map;

public interface ISDKWrapper {

    /**
     * 返回SDK版本号
     *
     * @return 版本号，如"1.0"
     */
    public String getSdkVersion();

    /**
     * 初始化SDK Wrapper
     *
     * @param appContext 应用Application的Context，建议在{@link Application#onCreate()}中调用
     * @param extras     附加参数，参考各个SDK Wrapper实现的类说明
     */
    public void init(Context appContext, Map<String, Object> extras);

    /**
     * 销毁wrapper
     */
    public void uninit();

    /**
     * 是否支持同步请求广告
     *
     * @return true 支持 false 不支持
     */
    public boolean isSupportSync();

    public AdResponse requestAdSync(AdRequest adRequest);

    /**
     * 请求广告。
     * 方法将异步请求广告。
     *
     * @param adRequest          请求参数
     * @param adResponseListener 请求广告回调接口
     */
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener);

    /**
     * 上报对应的广告跟踪事件。
     * <p>
     * 事件类型为{@link AdEvent}中的一种。
     * 各事件所需参数如下表
     * </p>
     * <table>
     * <tr>
     * <th>类型</th>
     * <th>所需参数</th>
     * </tr>
     * <tr>
     * <td>{@code EVENT_VIEW}</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_CLICK}</td>
     * <td>
     * "down_x" | int | 用户按下时x坐标<br></br>
     * "down_y" | int | 用户按下时y坐标<br></br>
     * "up_x" | int | 用户抬起时x坐标<br></br>
     * "up_y" | int | 用户抬起时y坐标
     * </td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_CLOSE}</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_APP_START_DOWNLOAD}</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_APP_DOWNLOAD_COMPLETE}</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_APP_INSTALL}</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>{@code EVENT_APP_ACTIVE}</td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param adEvent     事件类型
     * @param adInfo      广告信息
     * @param eventParams 事件携带的参数
     */
    public void onEvent(int adEvent, AdInfo adInfo, Map<String, Object> eventParams);
}

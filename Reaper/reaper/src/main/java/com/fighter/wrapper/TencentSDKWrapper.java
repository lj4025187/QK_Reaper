package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONObject;
import com.fighter.common.Device;
import com.fighter.common.utils.EncryptUtils;

import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Version;

/**
 * 腾讯广点通Wrapper。<br></br>
 * <p>
 * <b>请求广告所需额外参数</b>
 * <table>
 * <tr>
 * <th>
 * 参数名
 * </th>
 * <th>
 * 类型
 * </th>
 * <th>
 * 说明
 * </th>
 * <th>
 * 备注
 * </th>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_AD_TYPE}</td>
 * <td>{@code int}</td>
 * <td>腾讯广点通的广告类型</td>
 * <td><b>必填</b></td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_AD_COUNT}</td>
 * <td>{@code int}</td>
 * <td>本次请求的广告数量<br></br>经测试发现仅能返回一条广告</td>
 * <td><b>必填</b></td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_REMOTE_IP}</td>
 * <td>{@code String}</td>
 * <td>公网IPV4地址</td>
 * <td><b>必填</b></td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_LAT}</td>
 * <td>{@code int}</td>
 * <td>经度</td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_LNG}</td>
 * <td>{@code int}</td>
 * <td>纬度</td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_COORDTIME}</td>
 * <td>{@code int}</td>
 * <td>获取经纬度(lat/lng)的时间，毫秒值</td>
 * </tr>
 * <tr>
 * <td>{@code EXTRA_ADVERTISING_ID}</td>
 * <td>{@code String}</td>
 * <td>Android Advertising ID</td>
 * </tr>
 * </table>
 * </p>
 */
public class TencentSDKWrapper implements ISDKWrapper {
    /**
     * 腾讯广点通的广告类型
     */
    public static final String EXTRA_AD_TYPE = "postype";
    /**
     * 本次请求的广告数量 必填
     */
    public static final String EXTRA_AD_COUNT = "count";
    /**
     * 公网IP 必填
     */
    public static final String EXTRA_REMOTE_IP = "remoteip";
    /**
     * 经度
     */
    public static final String EXTRA_LAT = "lat";
    /**
     * 维度
     */
    public static final String EXTRA_LNG = "lng";
    /**
     * 获取经纬度时的时间戳，ms
     */
    public static final String EXTRA_COORDTIME = "coordtime";
    /**
     * 若有Google Play,可传入Android Advertising ID，保留原始值，大陆大部分设备无法获取
     */
    public static final String EXTRA_ADVERTISING_ID = "aaid";

    // ----------------------------------------------------
    private static final String URL_REQUEST_AD_SCHEME = "http";
    private static final String URL_REQUEST_AD_HOST = "mi.gdt.qq.com";
    private static final String URL_REQUEST_AD_PATH = "gdt_mview.fcg";

    /**
     * 腾讯广点通API版本号
     */
    private static final String TENCENT_AD_API_VER = "2.1";

    /**
     * 腾讯广点通原生广告，不填宽高
     */
    private static final int AD_TYPE_NATIVE = 8;

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();

    // ----------------------------------------------------

    public TencentSDKWrapper(Context context) {
        mContext = context;
    }

    @Override
    public String getSdkVersion() {
        return TENCENT_AD_API_VER;
    }

    @Override
    public AdResponse requestAd(AdRequest adRequest) {
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(spliceRequestAdUrl(adRequest))
                .build();

        AdResponse adResponse = null;
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null) {
                adResponse = convertResponse(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adResponse == null ? new AdResponse.Builder().httpResponseCode(0).create() :
                adResponse;
    }

    @Override
    public void trackEvent(int adEvent, AdResponse adResponse, Map<String, Object> eventParams) {

    }

    // ----------------------------------------------------

    private HttpUrl spliceRequestAdUrl(AdRequest adRequest) {
        int adCounts = 1;
        if (adRequest.getAdExtras() != null &&
                adRequest.getAdExtras().containsKey(EXTRA_AD_COUNT)) {
            adCounts = (int) adRequest.getAdExtras().get(EXTRA_AD_COUNT);
        }

        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_REQUEST_AD_SCHEME)
                .host(URL_REQUEST_AD_HOST)
                .addPathSegments(URL_REQUEST_AD_PATH)
                .addQueryParameter("adposcount", "1")           // 本次请求的广告位个数。只能填 1
                .addQueryParameter("count",
                        String.valueOf(adCounts))               // 广告位广告个数
                .addQueryParameter("posid",
                        adRequest.getAdPositionId())            // 广告位 id，由腾讯广告联盟平台生成

                .addQueryParameter("charset", "utf8")           // 广告内容的数据编码。只能填 utf8
                .addQueryParameter("datafmt", "json")           // html或json
                .addQueryParameter("ext",
                        spliceAdRequestExt(adRequest));         // 广告请求扩展内容,内容为 json 串

        if (adRequest.getAdExtras() != null) {
            Map<String, Object> extras = adRequest.getAdExtras();
            if (extras.containsKey(EXTRA_AD_TYPE) &&
                    (int) (extras.get(EXTRA_AD_TYPE)) != AD_TYPE_NATIVE) {
                builder.addQueryParameter("posw", String.valueOf(
                        adRequest.getAdWidth()))                // 广告位宽
                        .addQueryParameter("posh", String.valueOf(
                                adRequest.getAdHeight()));      // 广告位高
            }
        }

        return builder.build();
    }

    private String spliceAdRequestExt(AdRequest adRequest) {
        /*
        json 示例
        {
            "req": {
                "c_device": "iPad",
                "c_ori": 0,
                "c_w": 768,
                "c_h": 1024,
                "c_pkgname": "com.gdt.qq.com.MyMusic",
                "muid": "a4ffbb171718e578a1a42969da59d010",
                "muidtype": 2,
                "conn": 1,
                "carrier": 0,
                "lat": 0,
                "lng": 0,
                "c_os": "ios",
                "apiver": "1.1",
                "postype": 3
            }
        }
         */

        Map<String, Object> extras = adRequest.getAdExtras();
        if (extras == null) {
            extras = new ArrayMap<>();
        }
        String strImei = Device.getM1(mContext);
        String strMac = Device.getMac(mContext);
        String strImeiMd5 = null;
        String strMacMd5;
        if (!TextUtils.isEmpty(strImei)) {
            // 需先转为小写，取MD5后转小写
            strImeiMd5 = EncryptUtils.encryptMD5ToString(strImei.toLowerCase()).toLowerCase();
        }
        if (TextUtils.isEmpty(strMac)) {
            strMac = "02:00:00:00:00:00";
        }
        // 需去除分隔符后转为大写，取MD5后转小写
        strMacMd5 = EncryptUtils.encryptMD5ToString(strMac.replaceAll(":", "").toUpperCase())
                .toLowerCase();

        JSONObject jsonReq = new JSONObject();
        jsonReq.put("apiver", TENCENT_AD_API_VER);          // api 版本
        jsonReq.put("appid", adRequest.getAppId());         // 广点通分配的 appid
        jsonReq.put("c_os", "android");                     // ios android
        jsonReq.put("muidtype", !TextUtils.isEmpty(strImeiMd5) ? 1 : 3); // 1:imei 2:ifa 3:mac
        String strMuid = !TextUtils.isEmpty(strImeiMd5) ? strImeiMd5 : strMacMd5;
        jsonReq.put("muid", strMuid);         // 移动终端标识
        jsonReq.put("c_device", Device.getBuildModel());               // 设备品牌和型号
        jsonReq.put("c_pkgname", mContext.getPackageName()); // app 包名
        int type = 8;

        jsonReq.put("postype", type);   // 广告位类型
        // 1:banner 2:插屏 3:应用墙 4:开屏 5:feed 8:原生

        Device.NetworkType networkType = Device.getNetworkType(mContext);
        int iNetType = 0;
        switch (networkType) {
            case NETWORK_WIFI: {
                iNetType = 1;
                break;
            }
            case NETWORK_2G: {
                iNetType = 2;
                break;
            }
            case NETWORK_3G: {
                iNetType = 3;
                break;
            }
            case NETWORK_4G: {
                iNetType = 4;
                break;
            }
        }
        jsonReq.put("conn", iNetType);        // 联网方式
        Device.SimOperator simOperator = Device.getSimOperatorByMnc(mContext);
        int iSimOperator = 0;
        switch (simOperator) {
            case SIM_OPERATOR_CHINA_MOBILE: {
                iSimOperator = 1;
                break;
            }
            case SIM_OPERATOR_CHINA_UNICOM: {
                iSimOperator = 2;
                break;
            }
            case SIM_OPERATOR_CHINA_TELCOM: {
                iSimOperator = 3;
                break;
            }
        }
        jsonReq.put("carrier", iSimOperator);     // 运营商
        int screenWidth = Device.getScreenWidth(mContext);
        int screenHeight = Device.getScreenHeight(mContext);
        // 屏幕宽高，取设备物理像素。高度始终大于宽度
        if (screenHeight >= screenWidth) {
            jsonReq.put("c_w", screenWidth);
            jsonReq.put("c_h", screenHeight);
        } else {
            jsonReq.put("c_w", screenHeight);
            jsonReq.put("c_h", screenWidth);
        }
        jsonReq.put("inline_full_screen", false); // 这个字段仅用于请求插屏大规格广告，请求其他类型广告时不填
        jsonReq.put("c_ori", Device.isPortrait(mContext) ? 0 : 90); // 设备横竖屏 0 90 180 270
        if (extras.containsKey(EXTRA_REMOTE_IP)) {
            jsonReq.put("remoteip", extras.get(EXTRA_REMOTE_IP));  // 用户设备的公网 IPv4 地址
        }
        // 经纬度及时间戳
        if (extras.containsKey(EXTRA_LAT)) {
            jsonReq.put("lat", extras.get(EXTRA_LAT));
        }
        if (extras.containsKey(EXTRA_LNG)) {
            jsonReq.put("lng", extras.get(EXTRA_LNG));
        }
        if (extras.containsKey(EXTRA_COORDTIME)) {
            jsonReq.put("coordtime", extras.get(EXTRA_COORDTIME));
        }
        String userAgent = Version.userAgent();
        // 终端用户 HTTP 请求头中的User-Agent 字段
        // 这里因为没有给OkHttp更改User-Agent信息，所以直接填充
        if (!TextUtils.isEmpty(userAgent)) {
            jsonReq.put("useragent", userAgent);
        }
        jsonReq.put("referer", "");     // 终端用户 HTTP 请求头中的 referer字段 (直接请求，没有来源页)
        jsonReq.put("c_osver", Device.getBuildRelease());   // os 版本
        jsonReq.put("screen_density", String.valueOf(Device.getScreenDensity())); // 屏幕密度
        jsonReq.put("imei", strImeiMd5);    // 设备 imei 的md5sum 摘要,摘要小写
        jsonReq.put("mac", strMacMd5);      // 用户设备的 MAC，去除分隔符":"后转为大写,并取 md5sum 摘要
        // android 用户终端的 AndroidID,取md5sum 摘要
        String androidId = Device.getAndroidID(mContext);
        if (!TextUtils.isEmpty(androidId)) {
            jsonReq.put("aid", EncryptUtils.encryptMD5ToString(androidId));
        }
        // Android Advertising ID,保留原始值,大陆大部分设备无法获取
        if (extras.containsKey(EXTRA_ADVERTISING_ID)) {
            jsonReq.put("aaid", extras.get(EXTRA_ADVERTISING_ID));
        }

        JSONObject jsonExt = new JSONObject();
        jsonExt.put("req", jsonReq);

        return jsonExt.toString();
    }

    private AdResponse convertResponse(Response response) {
        AdResponse.Builder builder = new AdResponse.Builder();
        builder.httpResponseCode(response.code());
        if (response.isSuccessful()) {
            try {
                builder.oriResponse(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.create();
    }
}

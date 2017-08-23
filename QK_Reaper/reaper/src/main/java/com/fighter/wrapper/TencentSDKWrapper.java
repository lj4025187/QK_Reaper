package com.fighter.wrapper;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.GlobalThreadPool;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.EncryptUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Version;

/**
 * 腾讯广点通Wrapper。
 */
public class TencentSDKWrapper extends ISDKWrapper {
    private static final String TAG = "TencentSDKWrapper";

    /**
     * 经度
     */
    public static final String EXTRA_LNG = "lng";
    /**
     * 纬度
     */
    public static final String EXTRA_LAT = "lat";
    /**
     * 获取经纬度时的时间戳，ms
     */
    public static final String EXTRA_COORDTIME = "coordtime";
    /**
     * 若有Google Play,可传入Android Advertising ID，保留原始值，大陆大部分设备无法获取
     */
    public static final String EXTRA_ADVERTISING_ID = "aaid";

    // ----------------------------------------------------

    private static final String EXTRA_EVENT_VIEW_VIEW_ID = "tencent_event_view_view_id";
    private static final String EXTRA_EVENT_APP_TARGET_ID = "tencent_event_app_target_id";
    private static final String EXTRA_EVENT_CLICK_ID = "tencent_event_app_clickid";
    private static final String EXTRA_EVENT_CLICK_URL = "tencent_event_click_url";
    private static final String EXTRA_EVENT_CLICK_ACTION_URL = "tencent_event_click_action_url";

    private static final String EXTRA_EVENT_DOWN_X = "downX";
    private static final String EXTRA_EVENT_DOWN_Y = "downY";
    private static final String EXTRA_EVENT_UP_X = "upX";
    private static final String EXTRA_EVENT_UP_Y = "upY";

    // ----------------------------------------------------
    private static final String URL_HTTP = "http";

    private static final String URL_REQUEST_AD_HOST = "mi.gdt.qq.com";
    private static final String URL_REQUEST_AD_PATH = "gdt_mview.fcg";
    private static final String URL_EVENT_VIEW_HOST = "v.gdt.qq.com";
    private static final String URL_EVENT_VIEW_PATH = "gdt_stats.fcg";
    private static final String URL_EVENT_APP_HOST = "c.gdt.qq.com";
    private static final String URL_EVENT_APP_PATH = "gdt_trace_a.fcg";

    /**
     * 腾讯广点通API版本号
     */
    private static final String TENCENT_AD_API_VER = "2.1";

    /**
     * 广告类型对应表。<br></br>
     * 腾讯广点通原生广告，不填宽高
     */
    private static final Map<String, Integer> TYPE_REF_MAP = new ArrayMap<>();

    // 1:banner 2:插屏 3:应用墙 4:开屏 5:feed 8:原生
    static {
        TYPE_REF_MAP.put(AdType.TYPE_BANNER, 1);
        TYPE_REF_MAP.put(AdType.TYPE_PLUG_IN, 2);
        TYPE_REF_MAP.put(AdType.TYPE_APP_WALL, 3);
        TYPE_REF_MAP.put(AdType.TYPE_FULL_SCREEN, 4);
        TYPE_REF_MAP.put(AdType.TYPE_FEED, 5);
        TYPE_REF_MAP.put(AdType.TYPE_NATIVE, 8);
    }

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ExecutorService mThreadPoolUtils = GlobalThreadPool.getSingleThreadPool();

    // ----------------------------------------------------

    @Override
    public String getSdkVersion() {
        return TENCENT_AD_API_VER;
    }

    @Override
    public String getSdkName() {
        return SdkName.GUANG_DIAN_TONG;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        ReaperLog.i(TAG, "[init]");

        mContext = appContext;
    }

    @Override
    public void uninit() {
        ReaperLog.i(TAG, "[uninit]");
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return true;
    }

    @Override
    public AdResponse requestAdSync(AdRequest adRequest) {
        ReaperLog.i(TAG, "[requestAdSync] params : " + adRequest);

        String errMsg = checkParams(adRequest);
        if (!TextUtils.isEmpty(errMsg)) {
            return new AdResponse
                    .Builder()
                    .adName(SdkName.GUANG_DIAN_TONG)
                    .adPosId(adRequest.getAdPosId())
                    .adLocalPositionAd(adRequest.getAdLocalPositionId())
                    .adType(adRequest.getAdType())
                    .errMsg(errMsg)
                    .create();
        }

        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(spliceRequestAdUrl(adRequest))
                .build();

        AdResponse adResponse = null;
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    adResponse = convertResponse(
                            adRequest.getAdPosId(),
                            adRequest.getExpireTime(),
                            adRequest.getSilentInstall(),
                            adRequest.getAdType(),
                            adRequest.getAdLocalAppId(),
                            adRequest.getAdLocalPositionId(),
                            response.body().string());
                } else {
                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", response.code());
                    ReaperLog.e(TAG, "ad request failed, errCode: " + response.code() + ", errMsg: " + errJson.toString());
                    return new AdResponse
                            .Builder()
                            .adName(SdkName.GUANG_DIAN_TONG)
                            .adPosId(adRequest.getAdPosId())
                            .adLocalPositionAd(adRequest.getAdLocalPositionId())
                            .adType(adRequest.getAdType())
                            .errMsg(errJson.toJSONString())
                            .create();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
        return adResponse == null ?
                new AdResponse
                        .Builder()
                        .adName(SdkName.GUANG_DIAN_TONG)
                        .adPosId(adRequest.getAdPosId())
                        .adLocalPositionAd(adRequest.getAdLocalPositionId())
                        .adType(adRequest.getAdType())
                        .errMsg("Request has no response.")
                        .create() :
                adResponse;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        mThreadPoolUtils.execute(new AdRequestRunnable(adRequest, adResponseListener));
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public String requestWebUrl(AdInfo adInfo) {
        return requestUrlInner(adInfo);
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public String requestDownloadUrl(AdInfo adInfo) {
        return requestUrlInner(adInfo);
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_SUCCESS: {
                eventView(adInfo);
                break;
            }
        }
    }

    // ----------------------------------------------------

    private String checkParams(AdRequest adRequest) {
        // 是否有对应支持的广告类型
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return "Can not find match tencent ad type with ad type " +
                    adRequest.getAdType();
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalAppId())) {
            return "Tencent app id is null";
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalPositionId())) {
            return "Tencent ad position id is null";
        }

        return null;
    }

    private HttpUrl spliceRequestAdUrl(AdRequest adRequest) {
        int adCounts = adRequest.getAdCount();

        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_HTTP)
                .host(URL_REQUEST_AD_HOST)
                .addPathSegments(URL_REQUEST_AD_PATH)
                .addQueryParameter("adposcount", "1")           // 本次请求的广告位个数。只能填 1
                .addQueryParameter("count",
                        String.valueOf(adCounts))               // 广告位广告个数
                .addQueryParameter("posid",
                        adRequest.getAdLocalPositionId())       // 广告位 id，由腾讯广告联盟平台生成

                .addQueryParameter("charset", "utf8")           // 广告内容的数据编码。只能填 utf8
                .addQueryParameter("datafmt", "json")           // html或json
                .addQueryParameter("ext",
                        spliceAdRequestExt(adRequest));         // 广告请求扩展内容,内容为 json 串

        int localAdType = 0;
        if (TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            localAdType = TYPE_REF_MAP.get(adRequest.getAdType());
        }

        // 原生广告不填写宽高
        if (localAdType != TYPE_REF_MAP.get(AdType.TYPE_NATIVE)) {
            builder.addQueryParameter("posw", String.valueOf(
                    adRequest.getAdWidth()))                    // 广告位宽
                    .addQueryParameter("posh", String.valueOf(
                            adRequest.getAdHeight()));          // 广告位高
        }

        return builder.build();
    }

    private String spliceAdRequestExt(AdRequest adRequest) {
        Map<String, Object> allParams = adRequest.getAdAllParams();
        if (allParams == null) {
            allParams = new ArrayMap<>();
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
        jsonReq.put("apiver", TENCENT_AD_API_VER);                              // api 版本
        jsonReq.put("appid", adRequest.getAdLocalAppId());                      // 广点通分配的 appid
        jsonReq.put("c_os", "android");                                         // ios android
        jsonReq.put("muidtype", !TextUtils.isEmpty(strImeiMd5) ? 1 : 3);        // 1:imei 2:ifa 3:mac
        String strMuid = !TextUtils.isEmpty(strImeiMd5) ? strImeiMd5 : strMacMd5;
        jsonReq.put("muid", strMuid);                                           // 移动终端标识
        jsonReq.put("c_device", Device.getBuildModel());                        // 设备品牌和型号
        jsonReq.put("c_pkgname", mContext.getPackageName());                    // app 包名

        int localAdType = 0;
        if (TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            localAdType = TYPE_REF_MAP.get(adRequest.getAdType());
        }
        jsonReq.put("postype", localAdType);                                    // 广告位类型

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
        jsonReq.put("conn", iNetType);                                         // 联网方式
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
        jsonReq.put("carrier", iSimOperator);                                  // 运营商
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
        jsonReq.put("inline_full_screen", false);                              // 这个字段仅用于请求插屏大规格广告，请求其他类型广告时不填
        jsonReq.put("c_ori", Device.isPortrait(mContext) ? 0 : 90);            // 设备横竖屏 0 90 180 270
        // 公网IP无法获取，不填写
        // 经纬度及时间戳
        if (allParams.containsKey(EXTRA_LAT)) {
            jsonReq.put("lat", allParams.get(EXTRA_LAT));
        }
        if (allParams.containsKey(EXTRA_LNG)) {
            jsonReq.put("lng", allParams.get(EXTRA_LNG));
        }
        if (allParams.containsKey(EXTRA_COORDTIME)) {
            jsonReq.put("coordtime", allParams.get(EXTRA_COORDTIME));
        }
        String userAgent = Version.userAgent();
        // 终端用户 HTTP 请求头中的User-Agent 字段
        // 这里因为没有给OkHttp更改User-Agent信息，所以直接填充
        if (!TextUtils.isEmpty(userAgent)) {
            jsonReq.put("useragent", userAgent);
        }
        // 终端用户 HTTP 请求头中的 referer字段 (直接请求，没有来源页 不填写)
        jsonReq.put("c_osver", Device.getBuildRelease());                           // os 版本
        jsonReq.put("screen_density", String.valueOf(Device.getScreenDensity()));   // 屏幕密度
        jsonReq.put("imei", strImeiMd5);                                            // 设备 imei 的md5sum 摘要,摘要小写
        jsonReq.put("mac", strMacMd5);                                              // 用户设备的 MAC，去除分隔符":"后转为大写,并取 md5sum 摘要
        // android 用户终端的 AndroidID,取md5sum 摘要
        String androidId = Device.getAndroidID(mContext);
        if (!TextUtils.isEmpty(androidId)) {
            jsonReq.put("aid", EncryptUtils.encryptMD5ToString(androidId));
        }
        // Android Advertising ID,保留原始值,大陆大部分设备无法获取
        if (allParams.containsKey(EXTRA_ADVERTISING_ID)) {
            jsonReq.put("aaid", allParams.get(EXTRA_ADVERTISING_ID));
        }

        JSONObject jsonExt = new JSONObject();
        jsonExt.put("req", jsonReq);

        return jsonExt.toString();
    }

    private AdResponse convertResponse(String adPosId,
                                       long expireTime,
                                       boolean silentInstall,
                                       String adType,
                                       String adLocalAppId,
                                       String adLocalPositionId,
                                       String oriResponse) {
        AdResponse.Builder builder = new AdResponse.Builder();
        builder.adPosId(adPosId)
                .adName(SdkName.GUANG_DIAN_TONG)
                .adType(adType)
                .adLocalAppId(adLocalAppId)
                .adLocalPositionAd(adLocalPositionId);
        JSONObject errJson = new JSONObject();

        JSONObject resJson = null;
        try {
            resJson = JSONObject.parseObject(oriResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject dataJson = null;
        JSONObject adPosJson = null;
        JSONArray adListJson = null;
        if (resJson != null) {
            int retCode = resJson.getIntValue("ret");
            int rptCode = resJson.getIntValue("rpt");
            String msg = resJson.getString("msg");
            errJson.put("tencentRetCode", retCode);
            errJson.put("tencentRptCode", rptCode);
            errJson.put("tencentMsg", msg);

            dataJson = resJson.getJSONObject("data");
        }
        if (dataJson != null) {
            adPosJson = dataJson.getJSONObject(adLocalPositionId);
        }
        if (adPosJson != null) {
            int dataRetCode = adPosJson.getIntValue("ret");
            String dataMsg = adPosJson.getString("msg");
            errJson.put("tencentDataRetCode", dataRetCode);
            errJson.put("tencentDataMsg", dataMsg);

            if (dataRetCode == 0) {
                adListJson = adPosJson.getJSONArray("list");
            }
        }

        AdInfo adInfo = null;
        if (adListJson != null && adListJson.size() > 0) {
            int size = adListJson.size();
            for (int i = 0; i < size; i++) {
                JSONObject adInfoJson = adListJson.getJSONObject(i);
                if (adInfoJson == null) {
                    continue;
                }
                adInfo = new AdInfo();
                adInfo.generateUUID();
                adInfo.setExpireTime(expireTime);
                adInfo.setSilentInstall(silentInstall);
                adInfo.setCanCache(true);
                adInfo.setAdName(SdkName.GUANG_DIAN_TONG);
                adInfo.setAdPosId(adPosId);
                adInfo.setAdType(adType);
                adInfo.setAdLocalAppId(adLocalAppId);
                adInfo.setAdLocalPosId(adLocalPositionId);

                int contentType = AdInfo.ContentType.PICTURE;
                int tCrtType = adInfoJson.getIntValue("crt_type");
                switch (tCrtType) {
                    case 1: {
                        contentType = AdInfo.ContentType.TEXT;
                        break;
                    }
                    case 2: {
                        contentType = AdInfo.ContentType.PICTURE;
                        break;
                    }
                    case 3:
                    case 7: {
                        contentType = AdInfo.ContentType.PICTURE_WITH_TEXT;
                        break;
                    }
                }
                adInfo.setContentType(contentType);

                String rl = adInfoJson.getString("rl");
                if (!TextUtils.isEmpty(rl)) {
                    adInfo.setExtra(EXTRA_EVENT_CLICK_URL, rl);
                }

                int actionType = AdInfo.ActionType.BROWSER;
                int actType = adInfoJson.getIntValue("acttype");
                if (actType == 1) { // Android app
                    actionType = AdInfo.ActionType.APP_DOWNLOAD;
                }
                adInfo.setActionType(actionType);

                adInfo.setImgUrl(adInfoJson.getString("img"));
                adInfo.setTitle(adInfoJson.getString("txt"));
                adInfo.setDesc(adInfoJson.getString("desc"));

                adInfo.setExtra(EXTRA_EVENT_VIEW_VIEW_ID, adInfoJson.getString("viewid"));
                adInfo.setExtra(EXTRA_EVENT_APP_TARGET_ID, adInfoJson.getString("targetid"));

                JSONObject adExtJson = adInfoJson.getJSONObject("ext");
                if (adExtJson != null) {
                    adInfo.setAppIconUrl(adExtJson.getString("iconurl"));
                    String downAppName = adExtJson.getString("appname");
                    adInfo.setDownAppName(downAppName);
                    adInfo.setBrandName(downAppName);
                    int appScore = adExtJson.getIntValue("appscore");
                    int usersNum = adExtJson.getIntValue("num_app_users");
                }

                break;
            }
            if (adInfo != null) {
                builder.isSucceed(true);
                builder.adInfo(adInfo);
            }
        }

        builder.errMsg(errJson.toJSONString());
        return builder.create();
    }

    private void eventView(AdInfo adInfo) {
        String viewId = (String) adInfo.getExtra(EXTRA_EVENT_VIEW_VIEW_ID);
        if (TextUtils.isEmpty(viewId)) {
            return;
        }

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(URL_HTTP)
                .host(URL_EVENT_VIEW_HOST)
                .addPathSegments(URL_EVENT_VIEW_PATH)
                .addQueryParameter("count", "1")
                .addQueryParameter("viewid0", viewId)
                .build();

        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(httpUrl)
                .build();

        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.code() == 204) {
                ReaperLog.i(TAG, "tencent event view report succeed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String requestUrlInner(AdInfo adInfo) {
        String url = (String) adInfo.getExtra(EXTRA_EVENT_CLICK_ACTION_URL);
        if (!TextUtils.isEmpty(url)) {
            return url;
        }

        String rl = (String) adInfo.getExtra(EXTRA_EVENT_CLICK_URL);
        if (TextUtils.isEmpty(rl)) {
            ReaperLog.e(TAG, "[requestUrlInner] rl is null");
            return null;
        }

        if (adInfo.getActionType() == AdInfo.ActionType.APP_DOWNLOAD) {
            rl += "&acttype=1";    // app下载
        } else {
            rl += "&acttype=18";    // 普通跳转
        }

        int downX = -999;
        int downY = -999;
        int upX = -999;
        int upY = -999;

        Map<String, Object> eventParams = adInfo.getAdAllParams();

        if (eventParams != null) {
            if (eventParams.containsKey(EXTRA_EVENT_DOWN_X)) {
                downX = (int) eventParams.get(EXTRA_EVENT_DOWN_X);
            }
            if (eventParams.containsKey(EXTRA_EVENT_DOWN_Y)) {
                downY = (int) eventParams.get(EXTRA_EVENT_DOWN_Y);
            }
            if (eventParams.containsKey(EXTRA_EVENT_UP_X)) {
                upX = (int) eventParams.get(EXTRA_EVENT_UP_X);
            }
            if (eventParams.containsKey(EXTRA_EVENT_UP_Y)) {
                upY = (int) eventParams.get(EXTRA_EVENT_UP_Y);
            }
        }

        JSONObject s = new JSONObject();
        s.put("down_x", downX);
        s.put("down_y", downY);
        s.put("up_x", upX);
        s.put("up_y", upY);
        rl += "&s=" + s.toJSONString();

        if (adInfo.getActionType() == AdInfo.ActionType.BROWSER) {
            url = rl;
        } else {
            url = requestAppUrl(rl, adInfo);
        }
        adInfo.setExtra(EXTRA_EVENT_CLICK_ACTION_URL, url);

        return url;
    }

    private String requestAppUrl(String rl, AdInfo adInfo) {
        String url = null;

        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(rl)
                .build();

        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                String body = response.body().string();

                JSONObject resJson = JSONObject.parseObject(body);
                int ret = resJson.getInteger("ret");
                if (ret == 0) {
                    JSONObject dataJson = resJson.getJSONObject("data");
                    if (dataJson != null) {
                        String dstlink = dataJson.getString("dstlink");
                        String clickid = dataJson.getString("clickid");

                        if (!TextUtils.isEmpty(dstlink)) {
                            url = dstlink;
                        }

                        if (!TextUtils.isEmpty(clickid)) {
                            adInfo.setExtra(EXTRA_EVENT_CLICK_ID, clickid);
                        }
                    } else {
                        ReaperLog.e(TAG,
                                "Request apk download url failed due to null data");
                    }
                } else {
                    ReaperLog.e(TAG, "Request apk download url failed");
                }
            } else {
                ReaperLog.e(TAG, "Request http error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }

        return url;
    }

    // ----------------------------------------------------
    private class AdRequestRunnable implements Runnable {
        private AdRequest mAdRequest;
        private AdResponseListener mAdResponseListener;

        AdRequestRunnable(AdRequest adRequest, AdResponseListener adResponseListener) {
            mAdRequest = adRequest;
            mAdResponseListener = adResponseListener;
        }

        @Override
        public void run() {
            AdResponse adResponse = requestAdSync(mAdRequest);
            if (mAdResponseListener != null) {
                mAdResponseListener.onAdResponse(adResponse);
            }
        }
    }
}

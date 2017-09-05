package com.fighter.wrapper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.common.Device;
import com.fighter.common.GlobalThreadPool;
import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EmptyUtils;
import com.fighter.common.utils.EncryptUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 靶心广告 wrapper
 * <p>
 * Created by jia on 7/14/17.
 */
public class BullsEyeSDKWrapper extends ISDKWrapper {

    private static final String TAG = "BullsEyeSDKWrapper";
    public static boolean BETA_SERVER = false;

    public static final String EXTRA_GPS_SPEED = "bx_gps_speed";
    public static final String EXTRA_GPS_ACCURACY = "bx_gps_accuracy";
    public static final String EXTRA_GPS_LAT = "bx_gps_lat";
    public static final String EXTRA_GPS_LON = "bx_gps_lon";
    public static final String EXTRA_CURRENT_MILLIS = "bx_cur_millis";

    private static String sAppId, sAppKey, sVersionName, sVersionCode, sPackageName, sAppName;
    private static SimpleDateFormat sDateFormat;
    private static IRC4 sIrc4, sTrackIrc4;

    private Context mContext;
    private OkHttpClient mClient = AdOkHttpClient.INSTANCE.getOkHttpClient();
    private ExecutorService mThreadPool = GlobalThreadPool.getSingleThreadPool();

    private static final String URL_AIM_HOST = "10.139.232.146";
    private static final int AD_PORT = 5001;
    private static final int TRACK_PORT = 5002;
    private static final String URL_REQUEST_AIM_HOST = "bxe.comp.360os.com";
    private static final String URL_TRACK_AIM_HOST = "bxt.comp.360os.com";
    private static final String SSP_MEDIA_TYPE = "application/json; charset=utf-8";

    private static final String URL_REQUEST_AD_SCHEME = "http";
    private static final String URL_REQUEST_AD_PATH = "aim/e/v1/q";
    private static final String VERSION_CODE = "1.0";

    private static final String URL_TRACK_REQUEST_AD_SCHEME = "http";
    private static final String URL_TRACK_REQUEST_AD_PATH = "aim/b/v1/t";

    private static final String KEY_TRACK_ID = "bx_track_id";

    //ad info detail type 1:应用下载 2:电影 3:美食
    private static final String KEY_DETAIL_TYPE = "bx_detail_type";
    private static final String TYPE_APP_DL = "1";
    private static final String TYPE_MOVIE_DATA = "2";
    private static final String TYPE_CATE_DATA = "3";

    private static final String KEY_URL_STRING = "bx_ad_info_url";

    //app ad info
    private static final String KEY_APP_DL_IMP = "bx_app_imp";
    private static final String KEY_APP_DL_CLK = "bx_app_clk";
    private static final String KEY_APP_DL_INS = "bx_app_ins";
    private static final String KEY_APP_DL_ACT = "bx_app_act";
    private static final String KEY_APP_DL_CATE = "bx_app_category";

    //movie ad info
    private static final String KEY_MOVIE_ID = "bx_movie_id";
    private static final String KEY_MOVIE_NAME = "bx_movie_name";

    //cate ad info
    private static final String KEY_CATE_SHOP_ID = "bx_cate_shop_id";
    private static final String KEY_CATE_SHOP_NAME = "bx_cate_shop_name";
    private static final String KEY_CATE_CLASS_NAME = "bx_cate_class_name";

    //track url type 0:download 1:wap页面 2:deeplink链接
    private static final String KEY_TRACK_URL_TYPE = "bx_track_url_type";

    private static final String REQUEST_ADV_NUM = "1";

    /**
     * 支持的广告类型
     */
    private static final Map<String, Integer> TYPE_REF_MAP = new HashMap<>();

    static {
        TYPE_REF_MAP.put(AdType.TYPE_NATIVE, 8);
    }

    @Override
    public String getSdkVersion() {
        return VERSION_CODE;
    }

    @Override
    public String getSdkName() {
        return SdkName.BA_XIN;
    }

    @Override
    public void init(Context appContext, Map<String, Object> extras) {
        if (appContext == null || extras == null) return;
        BETA_SERVER |= Device.checkSDKMode(SdkName.BA_XIN);
        mContext = appContext;
        init(mContext);
        Object app_id = extras.get("app_id");
        if (app_id != null && app_id instanceof String)
            sAppId = (String) app_id;
        Object app_key = extras.get("app_key");
        if (app_key != null && app_key instanceof String)
            sAppKey = (String) app_key;
        sIrc4 = RC4Factory.create(sAppKey);
        //打点Kye值固定不变，测试环境(12345-qwert)正式环境(cda4bd24af9551ae)
        sTrackIrc4 = RC4Factory.create(BETA_SERVER ? "12345-qwert" : "cda4bd24af9551ae");
        sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    }

    private void init(Context context) {
        if (context == null) return;
        PackageInfo packageInfo = null;
        PackageManager packageManager = mContext.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null) return;
        sVersionName = packageInfo.versionName;
        sVersionCode = String.valueOf(packageInfo.versionCode);
        sPackageName = mContext.getPackageName();
        sAppName = (String) mContext.getApplicationInfo().loadLabel(packageManager);
    }

    @Override
    public boolean isRequestAdSupportSync() {
        return true;
    }

    @Override
    public void requestAdAsync(AdRequest adRequest, AdResponseListener adResponseListener) {
        if (adRequest == null) {
            ReaperLog.e(TAG, "requestAdAsync , adRequest == null");
            return;
        }

        if (adResponseListener == null) {
            ReaperLog.e(TAG, "requestAdAsync , adResponseListener == null");
            return;
        }

        mThreadPool.execute(new AdRequestRunnable(adRequest, adResponseListener));
    }

    @Override
    public AdResponse requestAdSync(AdRequest adRequest) {
        String errMsg = checkParams(adRequest);
        if (!TextUtils.isEmpty(errMsg)) {
            return new AdResponse.Builder()
                    .adName(SdkName.BA_XIN)
                    .adPosId(adRequest.getAdPosId())
                    .adLocalPositionAd(adRequest.getAdLocalPositionId())
                    .adType(adRequest.getAdType())
                    .errMsg(errMsg)
                    .create();
        }
        Request request = new Request.Builder()
                .addHeader("content-type", SSP_MEDIA_TYPE)
                .url(spliceRequestAdUrl(adRequest))
                .post(spliceRequestAdBody(adRequest))
                .build();
        ReaperLog.e(TAG, "requestAdSync url " + request.url().toString() +
                " start " + new Date(System.currentTimeMillis()));
        AdResponse adResponse = null;
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                ReaperLog.e(TAG, "get response " + new Date(System.currentTimeMillis()));
                if (response.isSuccessful()) {
                    adResponse = convertResponse(adRequest, response);
                } else {
                    int code = response.code();
                    String message = response.message();
                    String result = "ad request failed, errCode: " + code +
                            ", errMsg: " + (TextUtils.isEmpty(message) ? "not define" : message);
                    boolean reportSuccess = reportFailEvent(AdEvent.EVENT_AD_DOWN_FAIL, result);
                    ReaperLog.e(TAG, result + " report to server " + reportSuccess);

                    JSONObject errJson = new JSONObject();
                    errJson.put("httpResponseCode", code);
                    errJson.put("errMsg", TextUtils.isEmpty(message) ? "not define" : message);
                    return new AdResponse.Builder()
                            .adName(SdkName.BA_XIN)
                            .adPosId(adRequest.getAdPosId())
                            .adLocalPositionAd(adRequest.getAdLocalPositionId())
                            .adType(adRequest.getAdType())
                            .errMsg(errJson.toJSONString())
                            .create();
                }
            }
        } catch (IOException e) {
            ReaperLog.e(TAG, e.toString() + " fail " + new Date(System.currentTimeMillis()));
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
        return adResponse == null ?
                new AdResponse.Builder()
                        .adName(SdkName.BA_XIN)
                        .adPosId(adRequest.getAdPosId())
                        .adLocalPositionAd(adRequest.getAdLocalPositionId())
                        .adType(adRequest.getAdType())
                        .errMsg("Request has no response.")
                        .create() :
                adResponse;
    }

    private String checkParams(AdRequest adRequest) {
        // 是否有对应支持的广告类型
        if (!TYPE_REF_MAP.containsKey(adRequest.getAdType())) {
            return "Can not find match mix adx ad type with ad type " +
                    adRequest.getAdType();
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalAppId())) {
            return "BullsEye app id is null";
        }
        if (EmptyUtils.isEmpty(adRequest.getAdLocalPositionId())) {
            return "BullsEye ad position id is null";
        }
        return null;
    }

    private HttpUrl spliceRequestAdUrl(AdRequest adRequest) {
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_REQUEST_AD_SCHEME);
        if (BETA_SERVER) {
            builder.host(URL_AIM_HOST)
                    .port(AD_PORT);
        } else {
            builder.host(URL_REQUEST_AIM_HOST);
        }
        builder.addPathSegments(URL_REQUEST_AD_PATH)
                .addQueryParameter("aid", sAppId)
                .addQueryParameter("pid", adRequest.getAdLocalPositionId())
                .addQueryParameter("sv", VERSION_CODE);
        return builder.build();
    }

    private RequestBody spliceRequestAdBody(AdRequest adRequest) {
        JSONObject params = generateAdPostParams(adRequest);
        ReaperLog.e(TAG, "spliceRequestAdBody " + params.toString());
        MediaType mediaType = MediaType.parse(SSP_MEDIA_TYPE);
        byte[] result = sIrc4.encrypt(params.toString().getBytes());
        return RequestBody.create(mediaType, result);
    }

    private JSONObject generateAdPostParams(AdRequest adRequest) {
        JSONObject params = new JSONObject();
        String m1 = Device.getM1(mContext);                         //m1
        if (!TextUtils.isEmpty(m1))
            params.put("m1", EncryptUtils.encryptMD5ToString(m1).toLowerCase());
        params.put("m2", Device.getQDASM2(mContext));               //m2
        params.put("serialno", Device.getSerialNo());               //serial
        params.put("md", Device.getBuildModel());                   //机型
        params.put("br", Device.getBuildBrand());                   //手机品牌
        params.put("os", Device.getBuildSDKVersion());              //SDK version
        params.put("osv", Device.getBuildRelease());                //系统版本
        int screenWidth = Device.getScreenWidth(mContext);          //屏幕宽度
        int screenHeight = Device.getScreenHeight(mContext);        //屏幕高度
        float screenDensity = Device.getScreenDensity();            //屏幕密度
        params.put("sw", String.valueOf(screenWidth));
        params.put("sh", String.valueOf(screenHeight));
        params.put("dip", String.valueOf(screenDensity));
        params.put("adv_type", adRequest.getAdType());              //请求广告类型
        params.put("adv_num", REQUEST_ADV_NUM);                     //一次请求广告的数量
        params.put("net", Device.getNetworkTypeString(mContext));   //网络类型
        params.put("appv", sVersionName);                           //versionName;
        params.put("appvint", sVersionCode);                        //versionCode;
        params.put("brand", Device.getBuildBrand());                //手机品牌
        params.put("solution", Device.getBuildManufacturer());      //手机制造商
        params.put("d_model", Device.getBuildModel());              //手机设备型号
        params.put("channel", Device.getDeviceChannel());           //系统渠道
        params.put("carrier", Device.getSimOperator(mContext));     //运营商代码
        params.put("apppkg", TextUtils.isEmpty(sPackageName) ? "" : sPackageName);        //应用包名
        params.put("appname", TextUtils.isEmpty(sAppName) ? "" : sAppName);               //应用名称
        params.put("so", Device.isPortrait(mContext) ? "1" : "2");                        //横竖屏
        params.put("searchword", "");                                                     //关键字
        JSONObject lbs = generateLbsParams(adRequest);
        if (lbs != null)
            params.put("lbs", lbs);                    //POI业务需求adv_native_movie,adv_native_cate
        return params;
    }

    /**
     * 美食{@value TYPE_CATE_DATA、电影类{@value TYPE_MOVIE_DATA}的广告必须传入经纬度数据
     *
     * @return
     */
    private JSONObject generateLbsParams(AdRequest adRequest) {
        Map<String, Object> adAllParams = adRequest.getAdAllParams();
        //lbs
        JSONObject libs = new JSONObject();
        try {
            if (adAllParams.containsKey(EXTRA_GPS_SPEED)) {
                libs.put("gps_s", adAllParams.get(EXTRA_GPS_SPEED));          //gps速度
            }
            if (adAllParams.containsKey(EXTRA_GPS_ACCURACY)) {
                libs.put("gps_r", adAllParams.get(EXTRA_GPS_ACCURACY));       //gps定位半径
            }
            if (adAllParams.containsKey(EXTRA_GPS_LAT)) {
                libs.put("lat", adAllParams.get(EXTRA_GPS_LAT));              //纬度39.9811016777
            }
            if (adAllParams.containsKey(EXTRA_GPS_LON)) {
                libs.put("lon", adAllParams.get(EXTRA_GPS_LON));              //经度116.4883012203
            }
            if (adAllParams.containsKey(EXTRA_CURRENT_MILLIS)) {
                libs.put("gps_t", adAllParams.get(EXTRA_CURRENT_MILLIS));              //gps时间戳
            }

            libs.put("wm", Device.getBullsEyeCurrentWifi(mContext));                    //当前wifi（bssid+rssi）
            libs.put("wf", Device.getBullsEyeWifiList(mContext));                       //扫描到的wifi列表
            libs.put("wf_t", String.valueOf(System.currentTimeMillis()));               //wifi采集事件戳
            libs.put("bt", Device.getBullsEyeStationInfo(mContext));                    //基站信息
            libs.put("bt_t", String.valueOf(System.currentTimeMillis()));               //基站时间戳
            ReaperLog.i(TAG, "generateLibsParams " + libs.toString());
        } catch (JSONException e) {
            ReaperLog.e(TAG, "generateLibsParams exception " + e.toString());
            e.printStackTrace();
        }
        return libs;
    }

    private AdResponse convertResponse(AdRequest adRequest, Response response) throws IOException {
        String adPosId = adRequest.getAdPosId();
        long expireTime = adRequest.getExpireTime();
        String adType = adRequest.getAdType();
        String adLocalAppId = adRequest.getAdLocalAppId();
        String adLocalPositionId = adRequest.getAdLocalPositionId();
        AdResponse.Builder builder = new AdResponse.Builder();
        builder.adPosId(adPosId)
                .adName(SdkName.BA_XIN)
                .adType(adType)
                .adLocalAppId(adLocalAppId)
                .adLocalPositionAd(adLocalPositionId);
        ResponseBody resBody = response.body();
        if (resBody == null) {
            return builder.create();
        }
        byte[] resBodyData = resBody.bytes();
        String decrypt = new String(sIrc4.decrypt(resBodyData));
        ReaperLog.e(TAG, "convert response decrypt " + decrypt);
        JSONObject resJson = null;
        try {
            resJson = JSONObject.parseObject(decrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AdInfo adInfo;
        if (resJson != null) {
            String errCode = resJson.getString("errno");                          //错误码,非0为错误
            String errMsg = resJson.getString("errmsg");                          //错误信息
            if (!TextUtils.equals(errCode, "0")) {
                StringBuilder result = new StringBuilder();
                result.append("err code ")
                        .append(TextUtils.isEmpty(errCode) ? "no define" : errCode)
                        .append("err msg ")
                        .append(TextUtils.isEmpty(errMsg) ? " no define" : errMsg);
                boolean reportSuccess = reportFailEvent(AdEvent.EVENT_AD_DOWN_FAIL, result.toString());
                result.append("report to server ").append(reportSuccess);
                return new AdResponse.Builder()
                        .adName(SdkName.BA_XIN)
                        .adPosId(adRequest.getAdPosId())
                        .adLocalPositionAd(adRequest.getAdLocalPositionId())
                        .adType(adRequest.getAdType())
                        .errMsg(result.toString())
                        .create();
            }
            String pos_id = resJson.getString("pos_id");                        //广告位id
            String track_id = resJson.getString("trackid");                     //请求拿到的id，用来请求打点
            JSONArray app_dl_datas = resJson.getJSONArray("app_dl_datas");      //应用下载列表
            JSONArray movie_datas = resJson.getJSONArray("movie_datas");        //电影广告列表
            JSONArray cate_datas = resJson.getJSONArray("cate_datas");          //美食广告列表

            adInfo = generateAdInfo(TYPE_APP_DL, app_dl_datas, adRequest);
            if (adInfo == null)
                adInfo = generateAdInfo(TYPE_MOVIE_DATA, movie_datas, adRequest);
            if (adInfo == null)
                adInfo = generateAdInfo(TYPE_CATE_DATA, cate_datas, adRequest);

            if (adInfo != null) {
                adInfo.generateUUID();
                adInfo.setExpireTime(expireTime);
                adInfo.setCanCache(true);
                adInfo.setAdName(SdkName.BA_XIN);
                adInfo.setAdPosId(adPosId);
                adInfo.setAdType(adType);
                adInfo.setAdLocalAppId(adLocalAppId);
                adInfo.setAdLocalPosId(adLocalPositionId);
                adInfo.setExtra(KEY_TRACK_ID, track_id);
                builder.isSucceed(true);
                builder.adInfo(adInfo);
            }
        }
        return builder.create();
    }

    /**
     * generate AdInfo from the result json
     *
     * @param flag
     * @param jsonArray
     * @return
     */
    private AdInfo generateAdInfo(String flag, JSONArray jsonArray, AdRequest adRequest) {
        if (jsonArray == null || jsonArray.size() == 0) return null;
        int size = jsonArray.size();

        AdInfo adInfo = null;
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            if (jsonObject == null) continue;
            adInfo = new AdInfo();
            switch (flag) {
                case TYPE_APP_DL:
//                    String id = jsonObject.getString("id");                         //应用唯一id
//                    String apkid = jsonObject.getString("apkid");                   //应用包名
//                    String apk_md5 = jsonObject.getString("apk_md5");               //应用包md5
//                    String name = jsonObject.getString("name");                     //应用名称
//                    String version_name = jsonObject.getString("version_name");     //version_name
//                    String version_code = jsonObject.getString("version_code");     //version_code
//                    String os_version = jsonObject.getString("os_version");         //系统最低版本要求
//                    String pkg_size = jsonObject.getString("size");                 //包大小
//                    String signature_md5 = jsonObject.getString("signature_md5");   //包签名
//                    String download_times = jsonObject.getString("download_times"); //下载次数
//                    String category = jsonObject.getString("category");             //应用分类
//                    String type = jsonObject.getString("type");                     //soft-软件;game-游戏
//                    String soft_corp_name = jsonObject.getString("soft_corp_name"); //出品公司
//                    String logo_url = jsonObject.getString("logo_url");             //应用图片url地址
//                    String download_url = jsonObject.getString("download_url");     //应用下载地址
//                    String rating = jsonObject.getString("rating");                 //应用评分
//                    String adv_source = jsonObject.getString("adv_source");         //广告源
//                    putJSONValue(jsonObject, KEY_ACTUAL_SOURCE, adv_source);
//                    JSONArray track = jsonObject.getJSONArray("track");             //代理广告需要打点
//                    if (track != null && track.size() > 0) {
//                        int trackSize = track.size();
//                        for (int j = 0; j < trackSize; j++) {
//                            JSONObject trackObj = track.getJSONObject(i);
//                            JSONArray tk_imp = trackObj.getJSONArray("tk_imp");
//                            if (tk_imp != null && !tk_imp.isEmpty()) {
//                                ArrayList<String> urls = new ArrayList<>();
//                                for (int m = 0; m < tk_imp.size(); m++) {
//                                    String url = (String) tk_imp.get(i);
//                                    urls.add(url);
//                                }
//                                adInfo.setExtra(KEY_APP_DL_IMP, urls);
//                            }
//
//                            JSONArray tk_clk = trackObj.getJSONArray("tk_clk");
//                            if (tk_clk != null && !tk_clk.isEmpty()) {
//                                ArrayList<String> urls = new ArrayList<>();
//                                for (int m = 0; m < tk_clk.size(); m++) {
//                                    String url = (String) tk_clk.get(i);
//                                    urls.add(url);
//                                }
//                                adInfo.setExtra(KEY_APP_DL_CLK, urls);
//                            }
//
//                            JSONArray tk_ins = trackObj.getJSONArray("tk_ins");
//                            if (tk_ins != null && !tk_ins.isEmpty()) {
//                                ArrayList<String> urls = new ArrayList<>();
//                                for (int m = 0; m < tk_ins.size(); m++) {
//                                    String url = (String) tk_ins.get(i);
//                                    urls.add(url);
//                                }
//                                adInfo.setExtra(KEY_APP_DL_INS, urls);
//                            }
//
//                            JSONArray tk_act = trackObj.getJSONArray("tk_act");
//                            if (tk_act != null && !tk_act.isEmpty()) {
//                                ArrayList<String> urls = new ArrayList<>();
//                                for (int m = 0; m < tk_act.size(); m++) {
//                                    String url = (String) tk_act.get(i);
//                                    urls.add(url);
//                                }
//                                adInfo.setExtra(KEY_APP_DL_ACT, urls);
//                            }
//                        }
//                    }
//
//                    adInfo.setContentType(AdInfo.ContentType.PICTURE_WITH_TEXT);
//                    adInfo.setActionType(AdInfo.ActionType.APP_DOWNLOAD);
//
//                    adInfo.setAdName(SdkName.BA_XIN);
//                    if (!TextUtils.isEmpty(name)) adInfo.setDownAppName(name);
//                    if (!TextUtils.isEmpty(apkid)) adInfo.setDownPkgName(apkid);
//                    if (!TextUtils.isEmpty(download_url)) {
//                        adInfo.setDownAppUrl(download_url);
//                        adInfo.setExtra(KEY_TRACK_URL_TYPE, "0");
//                    }
//                    if (!TextUtils.isEmpty(logo_url)) {
//                        adInfo.setAppIconUrl(logo_url);
//                        adInfo.setImgUrl(logo_url);
//                    }
//
//                    if (!TextUtils.isEmpty(category)) adInfo.setExtra(KEY_APP_DL_CATE, category);
//                    adInfo.setExtra(KEY_DETAIL_TYPE, TYPE_APP_DL);
//                    putExtraValue(adInfo, BullsEyeKey.ADV_SOURCE, adv_source);
                    break;
                case TYPE_MOVIE_DATA:
                    String movie_id = jsonObject.getString("id");                   //电影唯一id
                    if (!TextUtils.isEmpty(movie_id)) adInfo.setExtra(KEY_MOVIE_ID, movie_id);
                    String movie_name = jsonObject.getString("name");               //电影名称
                    if (!TextUtils.isEmpty(movie_name)) {
                        adInfo.setExtra(KEY_MOVIE_NAME, movie_name);
                        adInfo.setTitle(movie_name);
                    }
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_NAME, movie_name);
                    String sc = jsonObject.getString("sc");                         //电影评分
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_RATE, sc);
                    String rt = jsonObject.getString("rt");                         //上映时间
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_SHOW, rt);
                    String star = jsonObject.getString("star");                     //演员
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_STAR, star);
                    String dur = jsonObject.getString("dur");                       //电影时长
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_DURATION, dur);
                    String showst = jsonObject.getString("showst");                 //上映情况
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_STATE, showst);
                    String wap_url = jsonObject.getString("wap_url");               //电影h5
                    if (!TextUtils.isEmpty(wap_url)) {
                        adInfo.setExtra(KEY_TRACK_URL_TYPE, "1");
                        adInfo.setExtra(KEY_URL_STRING, wap_url);
                    }
                    String movie_adv_source = jsonObject.getString("adv_source");   //广告源
                    putExtraValue(adInfo, BullsEyeKey.ADV_SOURCE, movie_adv_source);
                    String img = jsonObject.getString("img");                       //电影剧照
                    //http://p1.meituan.net/w.h/movie/9d0b4122fccde29eecb66dc2d2422bba444652.jpg
                    if (!TextUtils.isEmpty(img)) {
                        String replace = img.replace("w.h", adRequest.getAdWidth() + "." + adRequest.getAdHeight());
                        adInfo.setImgUrl(replace);
                    }
                    String dir = jsonObject.getString("dir");                       //导演
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_DIRECTOR, dir);
                    String ver = jsonObject.getString("ver");                       //版本（3D/中国巨幕）
                    putExtraValue(adInfo, BullsEyeKey.MOVIE_VERSION, ver);

                    adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    adInfo.setContentType(AdInfo.ContentType.PICTURE_WITH_TEXT);

                    adInfo.setAdName(SdkName.BA_XIN);
                    adInfo.setExtra(KEY_DETAIL_TYPE, flag);
                    putExtraValue(adInfo, BullsEyeKey.ADV_TYPE, flag);
                    break;

                case TYPE_CATE_DATA:
                    String cate_adv_source = jsonObject.getString("adv_source");    //广告源
                    putExtraValue(adInfo, BullsEyeKey.ADV_SOURCE, cate_adv_source);
                    String shopid = jsonObject.getString("shopid");                 //门店唯一id
                    if (!TextUtils.isEmpty(shopid)) adInfo.setExtra(KEY_CATE_SHOP_ID, shopid);
                    String shop_name = jsonObject.getString("shop_name");           //门店名称
                    if (!TextUtils.isEmpty(shop_name))
                        adInfo.setExtra(KEY_CATE_SHOP_NAME, shop_name);
                    putExtraValue(adInfo, BullsEyeKey.CATE_SHOP, shop_name);
                    String class_name = jsonObject.getString("class_name");         //一级品类名称
                    if (!TextUtils.isEmpty(class_name))
                        adInfo.setExtra(KEY_CATE_CLASS_NAME, class_name);
                    putExtraValue(adInfo, BullsEyeKey.CATE_CLASS_NAME, class_name);
                    String type_name = jsonObject.getString("type_name");           //二级品类名称
                    putExtraValue(adInfo, BullsEyeKey.CATE_TYPE_NAME, type_name);
                    String cate_name = jsonObject.getString("cate_name");           //三级品类名称
                    putExtraValue(adInfo, BullsEyeKey.CATE_TYPE, cate_name);
                    String area_name = jsonObject.getString("area_name");           //商圈
                    putExtraValue(adInfo, BullsEyeKey.CATE_AREA, area_name);
                    String latitude = jsonObject.getString("latitude");             //纬度
                    putExtraValue(adInfo, BullsEyeKey.CATE_LATITUDE, latitude);
                    String longitude = jsonObject.getString("longitude");           //经度
                    putExtraValue(adInfo, BullsEyeKey.CATE_LONGITUDE, longitude);
                    String distance = jsonObject.getString("distance");             //距离
                    putExtraValue(adInfo, BullsEyeKey.CATE_DISTANCE, distance);
                    String cate_rating = jsonObject.getString("rating");            //评分
                    putExtraValue(adInfo, BullsEyeKey.CATE_RATE, cate_rating);
                    String shop_wap_url = jsonObject.getString("shop_wap_url");     //门店h5页面
                    if (!TextUtils.isEmpty(shop_wap_url)) {
                        adInfo.setExtra(KEY_TRACK_URL_TYPE, "1");
                        adInfo.setExtra(KEY_URL_STRING, shop_wap_url);
                    }
                    String district_name = jsonObject.getString("district_name");   //行政区名称
                    putExtraValue(adInfo, BullsEyeKey.CATE_DISTRICT, district_name);
                    String address = jsonObject.getString("address");               //门店地址
                    putExtraValue(adInfo, BullsEyeKey.CATE_ADDRESS, address);
                    String phone = jsonObject.getString("phone");                   //门店电话
                    putExtraValue(adInfo, BullsEyeKey.CATE_PHONE, phone);
                    String city_name = jsonObject.getString("city_name");           //所在城市
                    putExtraValue(adInfo, BullsEyeKey.CATE_CITY, city_name);
                    String deeplink = jsonObject.getString("deeplink");             //应用的deeplink（目前是美团)

                    adInfo.setActionType(AdInfo.ActionType.BROWSER);
                    adInfo.setContentType(AdInfo.ContentType.TEXT);

                    adInfo.setAdName(SdkName.BA_XIN);
                    adInfo.setExtra(KEY_DETAIL_TYPE, flag);
                    putExtraValue(adInfo, BullsEyeKey.ADV_TYPE, flag);
                    if (!TextUtils.isEmpty(deeplink)) {
                        Intent intent = null;
                        try {
                            intent = Intent.parseUri(deeplink, Intent.URI_INTENT_SCHEME);
                            if (mContext.getPackageManager().queryIntentActivities(intent,
                                    PackageManager.GET_META_DATA).size() > 0) {
                                adInfo.setExtra(KEY_TRACK_URL_TYPE, "2");
                                adInfo.setExtra(KEY_URL_STRING, deeplink);
                            }
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return adInfo;
    }

    private void putExtraValue(AdInfo adInfo, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            adInfo.setExtra(key, value);
        } else {
            ReaperLog.i(TAG, "set value to ad info key " + key + " value is null");
        }
    }

    private void putJSONValue(JSONObject json, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            json.put(key, value.trim());
        } else {
            ReaperLog.i(TAG, "put json value key " + key + " value is null");
            json.put(key, "");
        }
    }

    @Override
    public boolean isOpenWebOwn() {
        return false;
    }

    @Override
    public String requestWebUrl(AdInfo adInfo) {
        return (String) adInfo.getExtra(KEY_URL_STRING);
    }

    @Override
    public boolean isDownloadOwn() {
        return false;
    }

    @Override
    public String requestDownloadUrl(AdInfo adInfo) {
        return adInfo.getDownAppUrl();
    }

    @Override
    public void onEvent(int adEvent, AdInfo adInfo) {
        reportEvent(adEvent, adInfo);
        reportTrackInfoEvent(adEvent, adInfo);
    }

    /**
     * Track event
     *
     * @param adEvent
     * @param adInfo
     */
    private void reportEvent(int adEvent, AdInfo adInfo) {
        if (adInfo == null) return;
        String track_id = (String) adInfo.getExtra(KEY_TRACK_ID);
        if (TextUtils.isEmpty(track_id)) {
            ReaperLog.e(TAG, "adInfo uuid " + adInfo.getUUID() + " track id is null");
            return;
        }
        Request request = new Request.Builder()
                .addHeader("content-type", SSP_MEDIA_TYPE)
                .url(spliceTrackRequestAdUrl(adEvent))
                .post(spliceTrackRequestAdBody(adInfo, adEvent, ""))
                .build();
        Response response = null;
        ReaperLog.e(TAG, " reportEvent url " + request.url().toString());
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    ReaperLog.i(TAG, "uuid: " + adInfo.getUUID() + " event report succeed : " +
                            adEvent);
                } else {
                    int code = response.code();
                    String message = response.message();
                    ReaperLog.e(TAG, "Event report failed : code : " + code
                            + "error message : " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
    }

    /**
     * 代理类广告主需要打点（应用类广告打点）
     *
     * @param adEvent 时间类型
     * @param adInfo  广告对象
     */
    @SuppressWarnings("unchecked")
    private void reportTrackInfoEvent(int adEvent, AdInfo adInfo) {
        if (adInfo == null) return;
        String detail_type = (String) adInfo.getExtra(KEY_DETAIL_TYPE);
        if (TextUtils.equals(detail_type, TYPE_APP_DL)) return;
        List<String> urls = null;
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_FAIL:
                break;
            case AdEvent.EVENT_VIEW_SUCCESS:
                urls = (List) adInfo.getExtra(KEY_APP_DL_IMP);
                break;
            case AdEvent.EVENT_CLICK:
                urls = (List) adInfo.getExtra(KEY_APP_DL_CLK);
                break;
            case AdEvent.EVENT_CLOSE:
                break;
            case AdEvent.EVENT_AD_DOWN_FAIL:
                break;
            case AdEvent.EVENT_APP_START_DOWNLOAD:
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
            case AdEvent.EVENT_APP_DOWNLOAD_FAILED:
            case AdEvent.EVENT_APP_DOWNLOAD_CANCELED:
                break;
            case AdEvent.EVENT_APP_INSTALL:
                urls = (List) adInfo.getExtra(KEY_APP_DL_INS);
                break;
            case AdEvent.EVENT_APP_ACTIVE:
                urls = (List) adInfo.getExtra(KEY_APP_DL_ACT);
                break;
            default:
                break;
        }

        if (urls == null || urls.size() == 0) {
            ReaperLog.i(TAG, "ignore event type " + adEvent);
            return;
        }

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            ReaperLog.i(TAG, "event report with url " + url);

            Request request = new Request.Builder()
                    .addHeader("content-type", "application/json;charset:utf-8")
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = mClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    ReaperLog.i(TAG, "event report succeed : " + adEvent);
                } else {
                    ReaperLog.e(TAG, "Event report failed : " + adEvent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(response);
            }
        }
    }

    /**
     * report down load ad failed event
     *
     * @param adEvent
     * @param errMsg
     */
    private boolean reportFailEvent(int adEvent, String errMsg) {
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(spliceTrackRequestAdUrl(adEvent))
                .post(spliceTrackRequestAdBody(null, adEvent, errMsg))
                .build();
        boolean report = false;
        Response response = null;
        ReaperLog.e(TAG, "reportFailEvent url " + request.url().toString());
        try {
            response = mClient.newCall(request).execute();
            if (response != null) {
                int code = response.code();
                String message = response.message();
                if (response.isSuccessful()) {
                    ReaperLog.i(TAG, "report down ad fail success");
                    report = true;
                } else {
                    ReaperLog.e(TAG, "report down ad fail code : " + code
                            + " msg : " + (TextUtils.isEmpty(message) ? "null" : message));
                    report = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            report = false;
        } finally {
            CloseUtils.closeIOQuietly(response);
        }
        return report;
    }

    /**
     * get bulls eye track http url
     *
     * @param adEvent
     * @return
     */
    private HttpUrl spliceTrackRequestAdUrl(int adEvent) {
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme(URL_TRACK_REQUEST_AD_SCHEME);
        if (BETA_SERVER) {
            builder.host(URL_AIM_HOST)
                    .port(TRACK_PORT);
        } else {
            builder.host(URL_TRACK_AIM_HOST);
        }
        builder.addPathSegments(URL_TRACK_REQUEST_AD_PATH)
                .addQueryParameter("aid", sAppId)
                .addQueryParameter("sv", VERSION_CODE)
                .addQueryParameter("at", getTrackPathSegments(adEvent));
        return builder.build();
    }

    /**
     * 广告曝光事件bx_imp
     * 广告点击事件bx_click
     * 广告转化事件(应用下载)bx_action
     * 广告获取失败事件bx_failed
     *
     * @param adEvent
     * @return
     */
    private String getTrackPathSegments(int adEvent) {
        String at_param;
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_SUCCESS:
                at_param = "1";
                break;
            case AdEvent.EVENT_CLICK:
                at_param = "2";
                break;
            case AdEvent.EVENT_APP_START_DOWNLOAD:
            case AdEvent.EVENT_APP_DOWNLOAD_CANCELED:
            case AdEvent.EVENT_APP_DOWNLOAD_FAILED:
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
            case AdEvent.EVENT_APP_INSTALL:
            case AdEvent.EVENT_APP_ACTIVE:
                at_param = "3";
                break;
            case AdEvent.EVENT_AD_DOWN_FAIL:
                at_param = "100";
                break;
            default:
                at_param = "";
                break;
        }
        return at_param;
    }

    /**
     * get RequestBody for the Track
     *
     * @param adInfo
     * @param adEvent
     * @param errMsg
     * @return
     */
    private RequestBody spliceTrackRequestAdBody(AdInfo adInfo, int adEvent, String errMsg) {
        JSONObject params = generateTrackPostParam(adInfo, adEvent, errMsg);
        ReaperLog.e(TAG, "spliceTrackRequestAdBody " + params.toString());
        MediaType mediaType = MediaType.parse(SSP_MEDIA_TYPE);
        byte[] result = sTrackIrc4.encrypt(params.toString().getBytes());
        return RequestBody.create(mediaType, result);
    }

    /**
     * set the post data in hash map
     *
     * @param adInfo
     * @param adEvent
     * @param errMsg
     * @return
     */
    private JSONObject generateTrackPostParam(AdInfo adInfo, int adEvent, String errMsg) {
        JSONObject params = new JSONObject();
        if (adInfo != null) {
            String track_id = (String) adInfo.getExtra(KEY_TRACK_ID);
            params.put("trackid", track_id);                                        //trackid
        }
        params.put("mac", Device.getFormatMac(mContext));                           //mac
        String m1 = Device.getM1(mContext);                                         //m1
        if (!TextUtils.isEmpty(m1))
            params.put("m1", EncryptUtils.encryptMD5ToString(m1).toLowerCase());
        params.put("m2", Device.getQDASM2(mContext));                               //m2
        params.put("brand", Device.getBuildManufacturer());                         //手机厂商
        params.put("solution", Device.getBuildProduct());                           //手机制造商
        params.put("d_model", Device.getBuildModel());                              //设备型号
        params.put("app_pkg", sPackageName);                                        //应用包名
        if (adInfo != null) {
            String adLocalPosId = adInfo.getAdLocalPosId();
            params.put("posid", adLocalPosId);                                      //靶心的广告位
        }
        String adType = null;
        if (adInfo != null) {
            adType = adInfo.getAdType();
        }
        if (adInfo != null) {
            Object type_obj = adInfo.getExtra(BullsEyeKey.ADV_TYPE);
            String factual_type = type_obj == null ? "" : (String) type_obj;
            params.put("adv_type", TextUtils.isEmpty(factual_type) ? "adv_native" : factual_type);  //靶心支持的广告类型，有效值adv_native
        }
        params.put("adv_num", REQUEST_ADV_NUM);                                     //获取的广告数量
        int width = Device.getScreenWidth(mContext);                                //屏幕宽度
        int height = Device.getScreenHeight(mContext);                              //屏幕高度
        params.put("screen", width + "*" + height);                                 //分辨率
        params.put("net", Device.getNetworkTypeString(mContext));                   //网络类型
        params.put("ad_sdk_v", VERSION_CODE);                                       //广告sdk版本号
        params.put("c_time", sDateFormat.format(new Date()));                       //时间
        params.put("channel", Device.getDeviceChannel());                           //系统渠道
        params.put("lang", Device.getLocalLanguage());                              //语言
        String mcc = Device.getMcc(mContext);
        putJSONValue(params, "mcc", mcc);                                           //国家运营商码
        JSONObject jsonObject = generateTrackBxEvent(adInfo, adEvent, errMsg);
        if (jsonObject != null)
            params.put("bx_event", jsonObject);
        return params;
    }

    /**
     * generate Track bx event param
     *
     * @param adInfo
     * @return
     */
    private JSONObject generateTrackBxEvent(AdInfo adInfo, int adEvent, String errMsg) {
        JSONObject advEventData = null;
        switch (adEvent) {
            case AdEvent.EVENT_VIEW_FAIL:
                break;
            case AdEvent.EVENT_VIEW_SUCCESS:
                advEventData = getTrackImpEventData(adInfo);
                break;
            case AdEvent.EVENT_CLICK:
                advEventData = getTrackClickEventData(adInfo);
                break;
            case AdEvent.EVENT_CLOSE:
                break;
            case AdEvent.EVENT_AD_DOWN_FAIL:
                advEventData = getTrackAdFailData(errMsg);
                break;
            case AdEvent.EVENT_APP_START_DOWNLOAD:
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
            case AdEvent.EVENT_APP_DOWNLOAD_FAILED:
            case AdEvent.EVENT_APP_DOWNLOAD_CANCELED:
            case AdEvent.EVENT_APP_INSTALL:
            case AdEvent.EVENT_APP_ACTIVE:
                advEventData = getTrackAppActionData(adInfo, adEvent, errMsg);
                break;
            default:
                break;
        }
        return advEventData;
    }

    /**
     * 广告曝光事件
     *
     * @param adInfo
     * @return
     */
    private JSONObject getTrackImpEventData(AdInfo adInfo) {
        if (adInfo == null) return null;
        String advDetailType = (String) adInfo.getExtra(KEY_DETAIL_TYPE);
        String adv_source = adInfo.getAdName();
        Object advSource = adInfo.getExtra(BullsEyeKey.ADV_SOURCE);
        if (advSource != null && advSource instanceof String) {
            adv_source = (String) advSource;
        }
        JSONObject jsonObject = new JSONObject();
        switch (advDetailType) {
            case TYPE_APP_DL:
                jsonObject.put("adv_sub_type", "adv_native_apk_dl");
                putJSONValue(jsonObject, "adv_source", adv_source);
                JSONArray array = new JSONArray();
                JSONObject adApp = new JSONObject();
                String appName = adInfo.getDownAppName();
                String pkgName = adInfo.getDownPkgName();
                String category = (String) adInfo.getExtra(KEY_APP_DL_CATE);
                putJSONValue(jsonObject, "ad_app_name", appName);
                putJSONValue(jsonObject, "ad_app_pkg", pkgName);
                putJSONValue(jsonObject, "ad_app_category", category);
                array.add(adApp);
                jsonObject.put("ad_apps", array);
                break;
            case TYPE_MOVIE_DATA:
                jsonObject.put("adv_sub_type", "adv_native_movie");
                putJSONValue(jsonObject, "adv_source", adv_source);
                String movieId = (String) adInfo.getExtra(KEY_MOVIE_ID);
                String movieName = (String) adInfo.getExtra(KEY_MOVIE_NAME);
                putJSONValue(jsonObject, "ad_movie_id", movieId);
                putJSONValue(jsonObject, "ad_movie_name", movieName);
                break;
            case TYPE_CATE_DATA:
                jsonObject.put("adv_sub_type", "adv_native_cate");
                putJSONValue(jsonObject, "adv_source", adv_source);
                String shopId = (String) adInfo.getExtra(KEY_CATE_SHOP_ID);
                putJSONValue(jsonObject, "ad_shop_id", shopId);
                String shopName = (String) adInfo.getExtra(KEY_CATE_SHOP_NAME);
                putJSONValue(jsonObject, "ad_shop_name", shopName);
                String className = (String) adInfo.getExtra(KEY_CATE_CLASS_NAME);
                putJSONValue(jsonObject, "class_name", className);
                break;
        }
        return jsonObject;
    }

    /**
     * 广告点击打点跟踪事件
     *
     * @param adInfo
     * @return
     */
    private JSONObject getTrackClickEventData(AdInfo adInfo) {
        if (adInfo == null) return null;
        String advDetailType = (String) adInfo.getExtra(KEY_DETAIL_TYPE);
        String adv_source = adInfo.getAdName();
        Object advSource = adInfo.getExtra(BullsEyeKey.ADV_SOURCE);
        if (advSource != null && advSource instanceof String) {
            adv_source = (String) advSource;
        }
        String url_type = (String) adInfo.getExtra(KEY_TRACK_URL_TYPE);
        JSONObject jsonObject = new JSONObject();
        switch (advDetailType) {
            case TYPE_APP_DL:
                jsonObject.put("adv_sub_type", "adv_native_apk_dl");
                putJSONValue(jsonObject, "url_type", url_type);
                String downAppUrl = adInfo.getDownAppUrl();
                putJSONValue(jsonObject, "url", downAppUrl);
                putJSONValue(jsonObject, "adv_source", adv_source);
                String appName = adInfo.getDownAppName();
                String pkgName = adInfo.getDownPkgName();
                String category = (String) adInfo.getExtra(KEY_APP_DL_CATE);
                putJSONValue(jsonObject, "ad_app_name", appName);
                putJSONValue(jsonObject, "ad_app_pkg", pkgName);
                putJSONValue(jsonObject, "ad_app_category", category);
                break;
            case TYPE_MOVIE_DATA:
                jsonObject.put("adv_sub_type", "adv_native_movie");
                putJSONValue(jsonObject, "url_type", url_type);
                String movieUrl = (String) adInfo.getExtra(KEY_URL_STRING);
                putJSONValue(jsonObject, "url", movieUrl);
                putJSONValue(jsonObject, "adv_source", adv_source);
                String movieId = (String) adInfo.getExtra(KEY_MOVIE_ID);
                String movieName = (String) adInfo.getExtra(KEY_MOVIE_NAME);
                putJSONValue(jsonObject, "ad_movie_id", movieId);
                putJSONValue(jsonObject, "ad_movie_name", movieName);
                break;
            case TYPE_CATE_DATA:
                jsonObject.put("adv_sub_type", "adv_native_cate");
                putJSONValue(jsonObject, "url_type", url_type);
                String cateUrl = (String) adInfo.getExtra(KEY_URL_STRING);
                putJSONValue(jsonObject, "url", cateUrl);
                putJSONValue(jsonObject, "adv_source", adv_source);
                String shopId = (String) adInfo.getExtra(KEY_CATE_SHOP_ID);
                putJSONValue(jsonObject, "ad_shop_id", shopId);
                String shopName = (String) adInfo.getExtra(KEY_CATE_SHOP_NAME);
                putJSONValue(jsonObject, "ad_shop_name", shopName);
                String className = (String) adInfo.getExtra(KEY_CATE_CLASS_NAME);
                putJSONValue(jsonObject, "class_name", className);
                break;
        }
        return jsonObject;
    }

    /**
     * 广告应用下载打点跟踪事件
     *
     * @param adInfo
     * @param adEvent
     * @param reason
     * @return
     */
    private JSONObject getTrackAppActionData(AdInfo adInfo, int adEvent, String reason) {
        if (adInfo == null) return null;
        JSONObject jsonObject = new JSONObject();
        String advDetailType = (String) adInfo.getExtra(KEY_DETAIL_TYPE);
        if (!TextUtils.equals(advDetailType, TYPE_APP_DL)) return jsonObject;
        String adv_source = adInfo.getAdName();
        Object advSource = adInfo.getExtra(BullsEyeKey.ADV_SOURCE);
        if (advSource != null && advSource instanceof String) {
            adv_source = (String) advSource;
        }
        putJSONValue(jsonObject, "adv_source", adv_source);
        String act_type = null;
        switch (adEvent) {
            case AdEvent.EVENT_APP_START_DOWNLOAD:
                act_type = "down_begin";
                break;
            case AdEvent.EVENT_APP_DOWNLOAD_COMPLETE:
                act_type = "down_end";
                break;
            case AdEvent.EVENT_APP_INSTALL:
                act_type = "install";
                break;
            case AdEvent.EVENT_AD_DOWN_FAIL:
                act_type = "down_failed";
                break;
        }
        putJSONValue(jsonObject, "act_type", act_type);
        putJSONValue(jsonObject, "reason", reason);
        String appPkg = adInfo.getDownPkgName();
        putJSONValue(jsonObject, "download_app_pkg", appPkg);
        String downUrl = adInfo.getDownAppUrl();
        putJSONValue(jsonObject, "download_url", downUrl);
        String appName = adInfo.getDownAppName();
        putJSONValue(jsonObject, "download_app_name", appName);
        return jsonObject;
    }

    /**
     * 广告获取失败事件
     *
     * @param errMsg
     * @return
     */
    private JSONObject getTrackAdFailData(String errMsg) {
        JSONObject jsonObject = new JSONObject();
        putJSONValue(jsonObject, "reason", TextUtils.isEmpty(errMsg) ? "no reason" : errMsg);
        jsonObject.put("reserved1", "");
        jsonObject.put("reserved2", "");
        return jsonObject;
    }

    /**
     * Get ad request
     */
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

    /**
     * 封装AdInfo用到的KEY,不能修改
     */
    private class BullsEyeKey {
        /**
         * 广告对应的真实广告源
         */
        public static final String ADV_SOURCE = "bx_adv_source";//猫眼，手助，美团
        /**
         * 广告的真实类型
         */
        public static final String ADV_TYPE = "bx_adv_type";//下载，电影、美食
        /**
         * 电影名称
         */
        public static final String MOVIE_NAME = "bx_movie_name";
        /**
         * 电影评分
         */
        public static final String MOVIE_RATE = "bx_movie_rate";
        /**
         * 电影上映时间
         */
        public static final String MOVIE_SHOW = "bx_movie_show";
        /**
         * 电影导演
         */
        public static final String MOVIE_DIRECTOR = "bx_movie_dir";
        /**
         * 电影演员
         */
        public static final String MOVIE_STAR = "bx_movie_star";
        /**
         * 电影时长
         */
        public static final String MOVIE_DURATION = "bx_movie_dur";
        /**
         * 电影版本
         */
        public static final String MOVIE_VERSION = "bx_movie_ver";
        /**
         * 电影上映情况
         */
        public static final String MOVIE_STATE = "bx_movie_state";

        /**
         * 美食所在城市
         */
        public static final String CATE_CITY = "bx_cate_city";
        /**
         * 美食商铺名称
         */
        public static final String CATE_SHOP = "bx_cate_shop";
        /**
         * 美食一级品类
         */
        public static final String CATE_CLASS_NAME = "bx_cate_class_name";
        /**
         * 美食二级品类
         */
        public static final String CATE_TYPE_NAME = "bx_cate_type_name";
        /**
         * 美食三级品类
         */
        public static final String CATE_TYPE = "bx_cate_cate_type";
        /**
         * 美食商圈
         */
        public static final String CATE_AREA = "bx_cate_area";
        /**
         * 美食行政区名称
         */
        public static final String CATE_DISTRICT = "bx_cate_dis";
        /**
         * 美食纬度
         */
        public static final String CATE_LATITUDE = "bx_cat_lat";
        /**
         * 美食经度
         */
        public static final String CATE_LONGITUDE = "bx_cate_lon";
        /**
         * 美食距离
         */
        public static final String CATE_DISTANCE = "bx_cate_distance";
        /**
         * 美食地址
         */
        public static final String CATE_ADDRESS = "bx_cate_adr";
        /**
         * 美食电话
         */
        public static final String CATE_PHONE = "bx_cate_phone";
        /**
         * 美食评分
         */
        public static final String CATE_RATE = "bx_cate_rate";
    }
}

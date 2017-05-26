package com.fighter.cache;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.ad.AdEvent;
import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.cache.downloader.AdCacheFileDownloadManager;
import com.fighter.common.PriorityTaskDaemon;
import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;
import com.fighter.reaper.BumpVersion;
import com.fighter.wrapper.AdRequest;
import com.fighter.wrapper.AdResponse;
import com.fighter.wrapper.AdResponseListener;
import com.fighter.wrapper.ICacheConvert;
import com.fighter.wrapper.ISDKWrapper;
import com.fighter.wrapper.MixAdxSDKWrapper;
import com.fighter.wrapper.TencentSDKWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * the class to manager the ad cache.
 * init() fill the all the posId cache.
 * requestAdCache() firstly request ad from cache, if cache is null,
 * request two ads, one for return to client, another for cache.
 * <p>
 * Created by lichen on 17-5-17.
 */

public class AdCacheManager implements AdCacheFileDownloadManager.DownloadCallback{
    private static final String TAG = AdCacheManager.class.getSimpleName();

    private static final String SALT = "salt_not_define";
    private static final String METHOD_ON_RESPONSE = "onResponse";

    private static final int CACHE_MAX = 5;

    private static AdCacheManager mAdCacheManager = new AdCacheManager();
    private File mCacheDir;

    private Map<String, ISDKWrapper> mSdkWrappers;
    private Map<String, String> mAdTypeMap;
    private Map<String, Method> mMethodMap;

    /**
     * the memory cache object
     */
    private static HashMap<String, List<Object>> mAdCache = new HashMap<>();

    private PriorityTaskDaemon mWorkThread;
    private Context mContext;

    private String mCacheId;
    private String mAppId;
    private String mAppKey;
    private Object mCallBack;

    private AdCacheFileDownloadManager mAdFileManager;

    private class RequestAdRunner implements PriorityTaskDaemon.TaskRunnable {
        private String mPosId;
        private Object mCallBack;
        private boolean mCached;

        public RequestAdRunner(String posId, Object callBack, boolean isCached) {
            this.mPosId = posId;
            this.mCallBack = callBack;
            this.mCached = isCached;
        }

        @Override
        public Object doSomething() {
            String errMsg = "";
            if (!requestWrapperAd(mPosId, mCallBack, mCached, errMsg)) {
                onRequestAdError(mCallBack, errMsg);
                return false;
            }
            return true;
        }
    }

    public static synchronized AdCacheManager getInstance() {
        if (mAdCacheManager == null)
            mAdCacheManager = new AdCacheManager();
        return mAdCacheManager;
    }

    private AdCacheManager() {
    }

    private void initCache(Context context) {
        String []posIds = getAllPosId(context);
        String cacheId = null;
        List<Object> adCacheObjects = new ArrayList<>();
        File cacheDir = context.getCacheDir();
        File adCacheDir = new File(cacheDir, "ac");
        if (!adCacheDir.exists())
            adCacheDir.mkdir();
        mCacheDir = adCacheDir;
        File[] cacheDirs = mCacheDir.listFiles();
        for (File dir : cacheDirs) {
            if (dir.isDirectory()) {
                cacheId = dir.getName();
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            Object adInfo = getAdCacheFromFile(file);
                            AdCacheInfo adCacheInfo = (AdCacheInfo)adInfo;
                            if(adCacheInfo.isCacheDisPlayed() || isAdCacheTimeout(adInfo)) {
                                cleanBeforeCache(adCacheInfo);
                            } else {
                                adCacheObjects.add(adInfo);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (adCacheObjects.size() > 0) {
            Collections.sort(adCacheObjects, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return (int) (((AdCacheInfo) o1).getCacheTime() -
                            ((AdCacheInfo) o2).getCacheTime());
                }
            });
            mAdCache.put(cacheId, adCacheObjects);
        }
        fillAdCachePool(posIds);
    }

    private void initReaperConfig(Context context) {
//        updateConfig(context);
        postConfigUpdate();
    }

    private void updateWrapper(Context context) {
        if (context == null)
            return;
        mSdkWrappers = new ArrayMap<>();

//        ISDKWrapper akAdWrapper = new AKAdSDKWrapper();
        ISDKWrapper tencentWrapper = new TencentSDKWrapper();
        ISDKWrapper mixAdxWrapper = new MixAdxSDKWrapper();
//        akAdWrapper.init(context, null);
        tencentWrapper.init(context, null);
        mixAdxWrapper.init(context, null);
//        mSdkWrappers.put(SdkName.AKAD, akAdWrapper);
        mSdkWrappers.put(SdkName.GUANG_DIAN_TONG, tencentWrapper);
        mSdkWrappers.put(SdkName.MIX_ADX, mixAdxWrapper);

        mAdTypeMap = new ArrayMap<>();
        mAdTypeMap.put("banner", AdType.TYPE_BANNER);
        mAdTypeMap.put("plugin", AdType.TYPE_PLUG_IN);
        mAdTypeMap.put("app_wall", AdType.TYPE_APP_WALL);
        mAdTypeMap.put("full_screen", AdType.TYPE_FULL_SCREEN);
        mAdTypeMap.put("feed", AdType.TYPE_FEED);
        mAdTypeMap.put("native", AdType.TYPE_NATIVE);
        mAdTypeMap.put("native_video", AdType.TYPE_NATIVE_VIDEO);

        mMethodMap = new ArrayMap<>();
    }

    /**
     * fill ad cache pool when cache init fill it.
     */
    private void fillAdCachePool(String[] posIds) {
        for (String posId : posIds) {
            postAdRequestTask(posId, null, true);
        }
    }

    /**
     * init the ad cache manager
     *
     * @param context
     * @param appId
     * @param appKey
     */
    public void init(Context context, String appId, String appKey) {
        mContext = context;
        mAppId = appId;
        mAppKey = appKey;

        mWorkThread = new PriorityTaskDaemon();
        mWorkThread.start();
        initReaperConfig(mContext);
        updateWrapper(mContext);
        initCache(mContext);
        mAdFileManager = AdCacheFileDownloadManager.getInstance(context);
        mAdFileManager.setDownloadCallback(this);
    }

    private String generateCacheId(AdCacheInfo info) {
        long cacheTime = info.getCacheTime();
        return String.valueOf(cacheTime);
    }

    private String[] getAllPosId(Context context) {
        List<ReaperAdvPos> reaperAdvPoses = ReaperConfigManager.getAllReaperAdvPos(context);
        String[] posIds = new String[reaperAdvPoses.size()];
        for (int i = 0; i < reaperAdvPoses.size(); i++) {
            posIds[i] = reaperAdvPoses.get(i).pos_id;
        }
        return posIds;
    }

    private void setCacheUsed(AdCacheInfo adCacheInfo) {
        if (adCacheInfo == null)
            return;
        adCacheInfo.setCacheState(AdCacheInfo.CACHE_BACK_TO_USER);
        File cacheDir = new File(mCacheDir, adCacheInfo.getAdCacheId());
        if (cacheDir.isDirectory() && cacheDir.exists()) {
            File cacheFile = new File(cacheDir, String.valueOf(adCacheInfo.getCacheTime()));
            if (cacheFile.isFile() && cacheFile.exists()) {
                FileOutputStream fileOutputStream = null;
                ObjectOutputStream objectOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(cacheFile);
                    objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(adCacheInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (objectOutputStream != null) {
                        try {
                            objectOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * request the ad cache to get the Ad information.
     */
    public void requestAdCache(String cacheId, Object callBack) {
        mCacheId = cacheId;
        mCallBack = callBack;
        Object cache = getCacheAdInfo(mCacheId);
        if (cache != null) {
            AdCacheInfo info = (AdCacheInfo) cache;
            String adSource = info.getAdSource();
            ISDKWrapper sdkWrapper = mSdkWrappers.get(adSource);
            onRequestAd(callBack, sdkWrapper.convertFromString(info.getCache()).getAdAllParams());
            setCacheUsed((AdCacheInfo) cache);
            requestCacheAdInternal();
        } else {
            onRequestAdError(callBack, "the request ad form all source is null");
        }
    }


    /**
     * the method is used to cache ad information to sdcard.
     *
     * @param cacheId the ad unique id
     * @param adInfo  the object of ad information
     * @throws IOException it maybe throw IOException
     */
    private void cacheAdInfo(String cacheId, Object adInfo) throws IOException {
        if (cacheId == null || adInfo == null)
            return;
        List<Object> adInfoObjects = new ArrayList<>();
        AdCacheInfo adCacheInfo = (AdCacheInfo) adInfo;
        String cacheFileId = generateCacheId(adCacheInfo);
        if (mAdCache.containsKey(cacheId)) {
            adInfoObjects = mAdCache.get(cacheId);
        }
        File cacheIdDir = new File(mCacheDir, cacheId);
        if (!cacheIdDir.exists()) {
            cacheIdDir.mkdir();
        }
        File adInfoFile = new File(cacheIdDir, cacheFileId);
        if (!adInfoFile.exists()) {
            adInfoFile.createNewFile();
        }
        adCacheInfo.setCachePath(adInfoFile.getAbsolutePath());
        // TODO test all the adc cache is available
        adCacheInfo.setCacheState(AdCacheInfo.CACHE_DISPLAY_BY_USER);
        adInfoObjects.add(adInfo);
        mAdCache.put(cacheId, adInfoObjects);
        FileOutputStream fileOutputStream = new FileOutputStream(adInfoFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(adInfo);
        objectOutputStream.close();
        fileOutputStream.close();
    }

    private Object getAdCacheFromFile(File file) throws IOException, ClassNotFoundException {
        Object adInfo = null;
        if (file == null)
            return null;
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            adInfo = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        }
        return adInfo;
    }

    private Object getAdCacheFromDisk(String cacheId) throws IOException, ClassNotFoundException {
        AdCacheInfo adInfo = null;
        File adCacheDir = new File(mCacheDir, cacheId);
        if (adCacheDir.exists() && adCacheDir.isDirectory()) {
            File[] cacheFiles = adCacheDir.listFiles();
            List<File> cacheFileList = Arrays.asList(cacheFiles);
            Collections.sort(cacheFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    String n1 = o1.getName();
                    String n2 = o2.getName();
                    return (int) (Long.parseLong(n1) - Long.parseLong(n2));
                }
            });
            for (File file : cacheFileList) {
                adInfo = (AdCacheInfo) getAdCacheFromFile(file);
                if (adInfo != null && !adInfo.isCacheBackToUser()) {
                    break;
                }
            }
        }
        return adInfo;
    }

    /**
     * cache ad file
     * @param imageUrl
     * @return cache file instance
     */
    private File cacheAdFile(String imageUrl){
        return mAdFileManager.cacheAdFile(imageUrl);
    }

    @Override
    public void onDownloadComplete(long reference, String fileName) {
        //download app success

    }

    /**
     * the method is used to get the ad cache information from sdcard.
     *
     * @param cacheId the ad unique id..
     * @return the object about ad information
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object getCacheAdInfo(String cacheId) {
        if (cacheId == null)
            return null;
        /* 1. find ad cache in memory cache */
        Object adInfo = null;
        List<Object> adInfoObjects = mAdCache.get(cacheId);
        if (adInfoObjects != null) {
            for (int i = 0; i < adInfoObjects.size(); i++) {
                adInfo = adInfoObjects.get(i);
                if (adInfo != null && adInfo instanceof AdCacheInfo && !((AdCacheInfo) adInfo).isCacheBackToUser())
                    break;
            }
        }
        /* 2. find ad cache in disk cache */
        if (adInfo == null) {
            try {
                adInfo = getAdCacheFromDisk(cacheId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        /* 3. check ad cache is available */
        if (isAdCacheTimeout(adInfo)) {
            return adInfo;
        }
        /* 4. request two ads from sdk, one is return, another is cache */
        requestDoubleAds();
        return adInfo;
    }

    public void collateAdCache(String cacheId) {
        if (cacheId == null)
            return;
        List<Object> cacheObjects = mAdCache.get(cacheId);
        AdCacheInfo cacheInfoInBottom = null;
        if (cacheObjects != null) {
            cacheInfoInBottom = (AdCacheInfo) cacheObjects.get(0);
        }
        if (cacheObjects != null && cacheObjects.size() > CACHE_MAX) {
            if (cacheInfoInBottom != null && cacheInfoInBottom.isCacheBackToUser()) {
                File cacheFile = new File(cacheInfoInBottom.getCachePath());
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                cacheObjects.remove(0);
            }
        }
    }

    public void onRequestAdError(Object receiver, String errMsg) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("isSucceed", false);
        params.put("errMsg", errMsg);
        onRequestAd(receiver, params);
    }

    public void onEvent(int adEvent, AdInfo adInfo) {
        // TODO set cache is unavailable
        ISDKWrapper wrapper = null;
        switch (adInfo.getAdName()) {
            case SdkName.GUANG_DIAN_TONG: {
                wrapper = mSdkWrappers.get("guangdiantong");
                break;
            }
            case SdkName.MIX_ADX: {
                wrapper = mSdkWrappers.get("baidu");
                break;
            }
            case SdkName.AKAD: {
                wrapper = mSdkWrappers.get("juxiao");
                break;
            }
        }

        if (wrapper != null) {
            wrapper.onEvent(adEvent, adInfo);
            handleTouchEvent(adEvent, adInfo);
        }
    }

    /**
     * handle touch event such as click
     *
     * @param adEvent
     * @param adInfo
     */
    private void handleTouchEvent(int adEvent, AdInfo adInfo){
        switch (adEvent) {
            case AdEvent.EVENT_CLICK:
                handleAction(adInfo);
                break;
        }
    }

    private void handleAction(AdInfo adInfo){
        int actionType = adInfo.getActionType();
        String adName = adInfo.getAdName();
        ISDKWrapper isdkWrapper = mSdkWrappers.get(adName);
        String actionUrl = null;
        switch (actionType) {
            case AdInfo.ActionType.APP_DOWNLOAD:
                actionUrl = isdkWrapper.requestDownloadUrl(adInfo);
                break;
            case AdInfo.ActionType.BROWSER:
                actionUrl = isdkWrapper.requestWebUrl(adInfo);
                break;
            default:
                ReaperLog.i(TAG, " click action type is undefine");
                break;
        }
        if(TextUtils.isEmpty(actionUrl))
                return;
        ReaperLog.i(TAG, actionType + " url " + actionUrl);
        mAdFileManager.requestDownload(actionUrl, null, null);
    }

    /**
     * check the ad cache is available.
     *
     * @param info the ad cache info
     * @return if available return true
     */
    private boolean isAdCacheTimeout(Object info) {
        if (info == null)
            return false;
        AdCacheInfo cacheInfo = (AdCacheInfo) info;
        long current = System.currentTimeMillis();
        return (current - cacheInfo.getCacheTime()) > Long.parseLong(cacheInfo.getExpireTime());
    }


    /**
     * if the cache is null, request two ads,
     * one is return to client, another cache if.
     */
    private void requestDoubleAds() {
        postAdRequestTask(mCacheId, mCallBack, false);
        postAdRequestTask(mCacheId, null, true);
    }

    private void cleanBeforeCache(AdCacheInfo info) {
        if (info == null)
            return;
        File cacheFile = new File(info.getCachePath());
        if (cacheFile.exists() && cacheFile.isFile()) {
            cacheFile.delete();
        }
    }

    private void postConfigUpdate() {
        PriorityTaskDaemon.NotifyPriorityTask mUpdateConfig = new PriorityTaskDaemon.NotifyPriorityTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                new PriorityTaskDaemon.TaskRunnable() {
                    @Override
                    public Object doSomething() {
                        return ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                                mContext.getPackageName(), SALT, mAppKey, mAppId);
                    }
                },
                new PriorityTaskDaemon.TaskNotify() {
                    @Override
                    public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
                        if ((boolean) result) {
                            updateWrapper(mContext);
                        }
                    }
                });
        mWorkThread.postTask(mUpdateConfig);
    }

    private void postAdRequestTask(String posId, Object callBack, boolean isCache) {
        RequestAdRunner adRunner = new RequestAdRunner(posId, callBack, isCache);
        PriorityTaskDaemon.NotifyPriorityTask requestAdTask = new PriorityTaskDaemon.NotifyPriorityTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST, adRunner, new PriorityTaskDaemon.TaskNotify() {
            @Override
            public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
                if ((boolean) result) {

                }
            }
        });
        mWorkThread.postTaskInFront(requestAdTask);
    }

    private void requestCacheAdInternal() {
        postAdRequestTask(mCacheId, null, true);
    }

    private void onRequestAd(Object receiver, Map<String, Object> params) {
        if (receiver == null) {
            return;
        }
        Method methodOnResponse = mMethodMap.get(METHOD_ON_RESPONSE);
        if (methodOnResponse == null) {
            try {
                methodOnResponse = receiver.getClass().getDeclaredMethod(
                        METHOD_ON_RESPONSE, Map.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (methodOnResponse != null) {
                mMethodMap.put(METHOD_ON_RESPONSE, methodOnResponse);
            }
        }

        if (methodOnResponse == null) {
            return;
        }

        try {
            methodOnResponse.invoke(receiver, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean requestWrapperAd(String adPosition, Object callBack, boolean isCached, String errMsg) {
        // 首先查找缓存是否存在

        // 获取配置信息
        boolean fetchSucceed = true;
        //TODO : update fetch config update wrapper
//        ReaperConfigManager.fetchReaperConfigFromServer(mContext,
//                            mContext.getPackageName(), SALT, mAppKey, mAppId);

//        postConfigUpdate();

        if (!fetchSucceed) {
//            onRequestAdError(mCallback, "Can not fetch reaper config from server");
            errMsg = "Can not fetch reaper config from server";
            return false;
        }

        ReaperAdvPos reaperAdvPos =
                ReaperConfigManager.getReaperAdvPos(mContext, adPosition);
        if (reaperAdvPos == null) {
            errMsg = "Can not find config info with ad position id [" + adPosition + "]";
            return false;
        }
        List<ReaperAdSense> reaperAdSenses = reaperAdvPos.getAdSenseList();
        if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
            ReaperAdSense adSense = ReaperConfigManager.getReaperAdSens(mContext, adPosition);
            List<ReaperAdSense> adSenses = new ArrayList<>();
            adSenses.add(adSense);
            reaperAdSenses = adSenses;
        }

        if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
            errMsg = "Config get 0 ad sense with ad position id [" + adPosition + "]";
            return false;
        }

        Collections.sort(reaperAdSenses);
        return requestWrapperAdInner(reaperAdSenses, reaperAdvPos.adv_type, isCached, callBack, errMsg);
    }

    private boolean requestWrapperAdInner(List<ReaperAdSense> reaperAdSenses, String advType, boolean isCache,
                                          Object callBack, String errMsg) {
        Iterator<ReaperAdSense> it = reaperAdSenses.iterator();
        ReaperAdSense reaperAdSense = it.next();
        it.remove();
        String adsName = reaperAdSense.ads_name;
        if (mSdkWrappers == null)
            return false;
        ISDKWrapper sdkWrapper = mSdkWrappers.get(adsName);
        if (sdkWrapper == null) {
            errMsg = "Can not find " + adsName + "'s sdk implements, may need " +
                    "upgrade reaper jar, current version " + BumpVersion.value();
            return false;
        }

        String adType = null;
        if (mAdTypeMap.containsKey(advType)) {
            adType = mAdTypeMap.get(advType);
        } else {
            errMsg = "Can not find match ad type with type name " +
                    advType;
            return false;
        }

        AdRequest.Builder builder = new AdRequest.Builder()
                .adLocalAppId(reaperAdSense.ads_appid)
                .adLocalPositionId(reaperAdSense.ads_posid)
                .adType(adType)
                .adCount(1);

        if ("pixel".equalsIgnoreCase(reaperAdSense.adv_size_type)) {
            String realSize = reaperAdSense.adv_real_size;
            String[] size = realSize.split("x");
            int width = 0;
            int height = 0;

            if (size.length == 2) {
                try {
                    width = Integer.valueOf(size[0]);
                    height = Integer.valueOf(size[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (width > 0 || height > 0) {
                builder.adWidth(width);
                builder.adHeight(height);
            }
        } else if ("ratio".equalsIgnoreCase(reaperAdSense.adv_size_type)) {
            errMsg = "[TEST] Not support adv size type " +
                    reaperAdSense.adv_size_type;
            return false;
        } else {
            errMsg = "Not support adv size type " +
                    reaperAdSense.adv_size_type;
            return false;
        }

        sdkWrapper.requestAdAsync(builder.create(), new AdResponseCallback(reaperAdSenses, advType,
                callBack, reaperAdSense, isCache, errMsg));
        return true;
    }


    /**
     * This callback is for ISDKWrapper
     */
    private class AdResponseCallback implements AdResponseListener {

        private List<ReaperAdSense> mReaperAdSenses;
        private String mAdvType;
        private Object mCallback;
        private ReaperAdSense curAdSense;
        private boolean mCached;
        private String mErrMsg;

        public AdResponseCallback(List<ReaperAdSense> mReaperAdSenses, String mAdvType, Object mCallback,
                                  ReaperAdSense curAdSense, boolean mCached, String mErrMsg) {
            this.mReaperAdSenses = mReaperAdSenses;
            this.mAdvType = mAdvType;
            this.mCallback = mCallback;
            this.curAdSense = curAdSense;
            this.mCached = mCached;
            this.mErrMsg = mErrMsg;
        }

        @Override
        public void onAdResponse(AdResponse adResponse) {
            if (adResponse == null) {
                if (!nextRequest()) {
                    onRequestAdError(mCallback, "Response with no result");
                }
                return;
            }

            if (adResponse.isSucceed() || !nextRequest()) {
                if (!mCached) {
                    onAdResponseCacheAdFile(adResponse);
                    onRequestAd(mCallback, adResponse.getAdAllParams());
                } else {
                    onAdResponseCacheAdInfo(adResponse);
                    onAdResponseCacheAdFile(adResponse);
                }
            }

        }

        private void onAdResponseCacheAdInfo(AdResponse adResponse) {
            AdCacheInfo info = new AdCacheInfo();
            ISDKWrapper sdkWrapper = mSdkWrappers.get(curAdSense.ads_name);
            info.setAdSource(curAdSense.ads_name);
            info.setCache(sdkWrapper.convertToString(adResponse));
            info.setExpireTime(curAdSense.expire_time);
            info.setAdCacheId(curAdSense.getPosId());
            try {
                cacheAdInfo(curAdSense.getPosId(), info);
                collateAdCache(curAdSense.getPosId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * The method for caching file into disk storage
         *
         * @param adResponse
         * @return
         */
        private void onAdResponseCacheAdFile(AdResponse adResponse) {
            List<AdInfo> ads = adResponse.getAdInfos();
            if(ads == null || ads.isEmpty()){
                ReaperLog.e(TAG, "adResponse get ads is null");
                return;
            }
            for(AdInfo adInfo : ads) {
                String imageUrl = adInfo.getImgUrl();
                if(TextUtils.isEmpty(imageUrl))
                    continue;
                File imageFile = cacheAdFile(imageUrl);
                if(imageFile == null || !imageFile.exists()){
                    continue;
                }
                adInfo.setImgFile(imageFile);
            }
        }

        private boolean nextRequest() {
            if (mReaperAdSenses != null &&
                    mReaperAdSenses.size() > 0) {
                requestWrapperAdInner(mReaperAdSenses, mAdvType, mCached, mCallback, mErrMsg);
                return true;
            } else {
                return false;
            }
        }
    }
}

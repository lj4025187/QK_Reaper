package com.fighter.cache;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.common.Device;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;
import com.fighter.reaper.BumpVersion;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.fighter.wrapper.AdRequest;
import com.fighter.wrapper.AdResponse;
import com.fighter.wrapper.AdResponseListener;
import com.fighter.wrapper.AdType;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * the class to manager the ad cache.
 * init() fill the all the posId cache.
 * requestAdCache() firstly request ad from cache, if cache is null,
 * request two ads, one for return to client, another for cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheManager {
    private static final String TAG = AdCacheManager.class.getSimpleName();

    private static final String SALT = "salt_not_define";
    private static final String METHOD_ON_RESPONSE = "onResponse";

    private static AdCacheManager mAdCacheManager = new AdCacheManager();
    private File mCacheDir;

    private Map<String, ISDKWrapper> mSdkWrappers;
    private Map<String, Integer> mAdTypeMap;
    private Map<String, Method> mMethodMap;

    /** the disk cache path */
    private final static HashMap<String, List<String>> mAdCacheFilePath = new HashMap<>();
    /** the memory cache object */
    private static HashMap<String, List<Object>> mAdCache = new HashMap<>();

    private ThreadPoolUtils mThreadPoolUtils;

    private Context mContext;

    private String mCacheId;
    private String mAppId;
    private String mAppKey;
    private Object mCallBack;

    public static synchronized AdCacheManager getInstance() {
        if (mAdCacheManager == null)
            mAdCacheManager = new AdCacheManager();
        return mAdCacheManager;
    }

    private AdCacheManager() {
    }

    private void initCache(Context context) {
        String cacheId = null;
        String cachePath;
        List<String> adCacheFilePaths = new ArrayList<>();
        List<Object> adCacheObjects = new ArrayList<>();
        File cacheDir = context.getCacheDir();
        File adCacheDir = new File(cacheDir, "ac");
        if (!adCacheDir.exists())
            adCacheDir.mkdir();
        mCacheDir = adCacheDir;
        File [] cacheDirs = mCacheDir.listFiles();
        for (File dir : cacheDirs) {
            if (dir.isDirectory()) {
                cacheId = dir.getName();
                File []files = dir.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        cachePath = file.getAbsolutePath();
                        adCacheFilePaths.add(cachePath);
                        try {
                            Object adInfo = getAdCacheFromDisk(file);
                            adCacheObjects.add(adInfo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        Collections.sort(adCacheFilePaths, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String path1 = o1.substring(o1.lastIndexOf("/") + 1);
                String path2 = o2.substring(o2.lastIndexOf("/") + 1);
                return (int)(Long.parseLong(path1) - Long.parseLong(path2));
            }
        });
        mAdCacheFilePath.put(cacheId, adCacheFilePaths);
        Collections.sort(adCacheObjects, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return (int) (((AdCacheInfo)o1).getCacheTime() -
                        ((AdCacheInfo)o2).getCacheTime());
            }
        });
        mAdCache.put(cacheId, adCacheObjects);
        String []posIds = getAllPosId(context);
        fillAdCachePool(posIds);
    }

    private void initReaperConfig(Context context) {
        updateConfig(context);
    }

    private void initReaperWrapper(Context context) {
        mSdkWrappers = new ArrayMap<>();
        ISDKWrapper akAdWrapper = new AKAdSDKWrapper();
        ISDKWrapper tencentWrapper = new TencentSDKWrapper();
        ISDKWrapper mixAdxWrapper = new MixAdxSDKWrapper();
        akAdWrapper.init(context, null);
        tencentWrapper.init(context, null);
        mixAdxWrapper.init(context, null);
        mSdkWrappers.put("juxiao", akAdWrapper);
        mSdkWrappers.put("guangdiantong", tencentWrapper);
        mSdkWrappers.put("baidu", mixAdxWrapper);

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

    private void updateConfig(Context context) {
        /// TODO care thread sync
        Device.NetworkType networkType = Device.getNetworkType(context);
        if (networkType != Device.NetworkType.NETWORK_NO) {
            mThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                            mContext.getPackageName(), SALT, mAppKey, mAppId);
                }
            });
        }
    }

    /**
     *
     * fill ad cache pool when cache init fill it.
     */
    private void fillAdCachePool(String[] posIds) {
        for (String posId : posIds) {
            File file = new File(mCacheDir, posId);
            if (!file.exists()) {
                mThreadPoolUtils.execute(new RequestAdRunner(posId, null, true));
            }
        }
    }

    /**
     * init the ad cache manager
     *
     * @param params
     */
    public void init(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        mContext = (Context) params.get("appContext");
        mAppId = (String) params.get("appId");
        mAppKey = (String) params.get("appKey");
        if (mContext == null || TextUtils.isEmpty(mAppId) ||
                TextUtils.isEmpty(mAppKey)) {
            return;
        }

        mThreadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.CachedThread, 1);
        initReaperConfig(mContext);
        initReaperWrapper(mContext);
        initCache(mContext);
    }

    private String generateCacheId(AdCacheInfo info) {
        long cacheTime = info.getCacheTime();
        return String.valueOf(cacheTime);
    }

    private String[] getAllPosId(Context context) {
        List<ReaperAdvPos> reaperAdvPoses = ReaperConfigManager.getAllReaperAdvPos(context);
        String [] posIds = new String [reaperAdvPoses.size()];
        for(int i = 0; i < reaperAdvPoses.size(); i++) {
            posIds[i] = reaperAdvPoses.get(i).pos_id;
        }
        return posIds;
    }

    /**
     * request the ad cache to get the Ad information.
     *
     */
    public void requestAdCache(String cacheId, Object callBack) {
        mCacheId = cacheId;
        mCallBack = callBack;
        Object cache = getCacheAdInfo(mCacheId);
        if (cache != null) {
            AdCacheInfo info = (AdCacheInfo) cache;
            String adSource = info.getAdSource();
            ISDKWrapper sdkWrapper = mSdkWrappers.get(adSource);
            ICacheConvert convert = (ICacheConvert)sdkWrapper;
            onRequestAd(callBack, convert.convertFromString(info.getCache()).getAdAllParams());
            //consumeAdCache(info);
            requestCacheAdInternal();
        }
    }


    /**
     * the method is used to cache ad information to sdcard.
     *
     * @param cacheId the ad unique id
     * @param adInfo the object of ad information
     * @throws IOException it maybe throw IOException
     */
    private void cacheAdInfo(String cacheId, Object adInfo) throws IOException{
        if (cacheId == null || adInfo == null)
            return;
        List<Object> adInfoObjects = new ArrayList<>();
        List<String> adInfoFilePaths = new ArrayList<>();
        AdCacheInfo adCacheInfo = (AdCacheInfo)adInfo;
        String cacheFileId = generateCacheId(adCacheInfo);
        if (mAdCache.containsKey(cacheId)) {
            adInfoObjects = mAdCache.get(cacheId);
        }
        adInfoObjects.add(adInfo);
        mAdCache.put(cacheId, adInfoObjects);
        if (mAdCacheFilePath.containsKey(cacheId)) {
            adInfoFilePaths = mAdCacheFilePath.get(cacheId);
        }
        File cacheIdDir = new File(mCacheDir, cacheId);
        if (!cacheIdDir.exists()) {
            cacheIdDir.mkdir();
        }
        File adInfoFile = new File(cacheIdDir, cacheFileId);
        if (!adInfoFile.exists()) {
            adInfoFile.createNewFile();
        }
        adInfoFilePaths.add(adInfoFile.getAbsolutePath());
        mAdCacheFilePath.put(cacheId, adInfoFilePaths);
        FileOutputStream fileOutputStream = new FileOutputStream(adInfoFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(adInfo);
        objectOutputStream.close();
        fileOutputStream.close();
    }
    private Object getAdCacheFromDisk(File file) throws IOException, ClassNotFoundException {
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
        Object adInfo = null;
        List<String> cacheAdInfoPaths = mAdCacheFilePath.get(cacheId);
        File adInfoFile;
        if (cacheAdInfoPaths != null && cacheAdInfoPaths.size() > 0) {
            adInfoFile = new File(cacheAdInfoPaths.get(0));
            if (adInfoFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(adInfoFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                adInfo = objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            }
        }
        return adInfo;
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
        if (adInfoObjects != null && adInfoObjects.size() > 0) {
            adInfo = adInfoObjects.get(0);
            if (adInfo != null && adInfo instanceof AdCacheInfo)
                return adInfo;
        }
        /* 2. find ad cache in disk cache */
        try {
            adInfo = getAdCacheFromDisk(cacheId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /* 3. check ad cache is available */
        if (isAdCacheAvailable(adInfo)) {
            return adInfo;
        }
        /* 4. request two ads from sdk, one is return, another is cache */
        requestDoubleAds();
        return adInfo;
    }

    /**
     * The method cache file into disk storage
     * @param cacheFileId
     * @return
     */
    public boolean cacheFileInDisk(String cacheFileId) {

        return true;
    }

    public void consumeAdCache(AdCacheInfo info) {
        if (info == null)
            return;
        String cacheId = info.getAdCacheId();
        List<Object> cacheObjects = mAdCache.get(cacheId);
        if (cacheObjects != null && cacheObjects.size() > 0) {
            cacheObjects.remove(0);
        }
        List<String> cachePaths = mAdCacheFilePath.get(cacheId);
        if (cachePaths != null && cachePaths.size() > 0) {
            File cacheFile = new File(cachePaths.get(0));
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
        }
        File cacheDir = new File(mCacheDir, cacheId);
        if (cacheDir.exists()) {
            cacheDir.delete();
        }
    }

    /**
     * check the ad cache is available.
     *
     * @param info the ad cache info
     * @return if available return true
     */
    private boolean isAdCacheAvailable(Object info) {
        if (info == null)
            return false;
        AdCacheInfo cacheInfo = (AdCacheInfo)info;
        long current = System.currentTimeMillis();
        return (current - cacheInfo.getCacheTime()) > Long.parseLong(cacheInfo.getExpireTime());
    }


    /**
     * if the cache is null, request two ads,
     * one is return to client, another cache if.
     *
     */
    private void requestDoubleAds() {
        mThreadPoolUtils.execute(new RequestAdRunner(mCacheId, mCallBack));
        mThreadPoolUtils.execute(new RequestAdRunner(mCacheId, null, true));
    }

    private void requestCacheAdInternal() {
        mThreadPoolUtils.execute(new RequestAdRunner(mCacheId, null, true));
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

    private void onRequestAdError(Object receiver, String errMsg) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("isSucceed", false);
        params.put("errMsg", errMsg);
        onRequestAd(receiver, params);
    }

    private class RequestAdRunner implements Runnable {
        private String mAdPosition;
        private Object mCallback;
        private boolean mCached;

        public RequestAdRunner(String adPosition, Object adRequestCallback) {
            mAdPosition = adPosition;
            mCallback = adRequestCallback;
        }

        public RequestAdRunner(String adPosition, Object adRequestCallback, boolean isCache) {
            mAdPosition = adPosition;
            mCallback = adRequestCallback;
            mCached = isCache;
        }


        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // 首先查找缓存是否存在

            // 获取配置信息
            boolean fetchSucceed = true;
                    /*ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                            mContext.getPackageName(), SALT, mAppKey, mAppId);*/

            if (!fetchSucceed) {
                onRequestAdError(mCallback, "Can not fetch reaper config from server");
                return;
            }

            ReaperAdvPos reaperAdvPos =
                    ReaperConfigManager.getReaperAdvPos(mContext, mAdPosition);
            if (reaperAdvPos == null) {
                /*onRequestAdError(mCallback,
                        "Can not find config info with ad position id [" + mAdPosition + "]");
                return;*/
                Random random = new Random(System.currentTimeMillis());
                reaperAdvPos = new ReaperAdvPos();
                reaperAdvPos.pos_id = mAdPosition;
                reaperAdvPos.adv_type = "banner";
                ReaperAdSense tencentSense = new ReaperAdSense();
                tencentSense.ads_name = "guangdiantong";
                tencentSense.ads_appid = "1104241296";
                tencentSense.ads_posid = "5060504124524896";
                tencentSense.adv_size_type = "pixel";
                tencentSense.adv_real_size = "640x100";
                tencentSense.max_adv_num = "10";
                tencentSense.priority = String.valueOf(random.nextInt(10));
                ReaperAdSense baiduSense = new ReaperAdSense();
                baiduSense.ads_name = "baidu";
                baiduSense.ads_appid = "0";
                baiduSense.ads_posid = "128";
                baiduSense.adv_size_type = "pixel";
                baiduSense.adv_real_size = "600x300";
                baiduSense.max_adv_num = "10";
                baiduSense.priority = String.valueOf(random.nextInt(10));

                reaperAdvPos.addAdSense(tencentSense);
//                reaperAdvPos.addAdSense(baiduSense);
            }
            List<ReaperAdSense> reaperAdSenses = reaperAdvPos.getAdSenseList();
            if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
                ReaperAdSense adSense = ReaperConfigManager.getReaperAdSens(mContext, mAdPosition);
                List<ReaperAdSense> adSenses = new ArrayList<>();
                adSenses.add(adSense);
                reaperAdSenses = adSenses;
            }
            if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
                onRequestAdError(mCallback,
                        "Config get 0 ad sense with ad position id [" + mAdPosition + "]");
                return;
            }
            Collections.sort(reaperAdSenses);

            // 依据配置信息获取广告
            mThreadPoolUtils.execute(new RequestSingleAdRunner(
                    reaperAdSenses,
                    reaperAdvPos.adv_type,
                    mCallback,
                    mCached
            ));
        }
    }

    private class RequestSingleAdRunner implements Runnable {
        private Object mCallback;
        private String mAdvType;
        private List<ReaperAdSense> mReaperAdSenses;
        private boolean mCached;

        public RequestSingleAdRunner(List<ReaperAdSense> reaperAdSenses,
                                     String advType,
                                     Object adRequestCallback) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            mCallback = adRequestCallback;
        }

        public RequestSingleAdRunner(List<ReaperAdSense> reaperAdSenses,
                                     String advType,
                                     Object adRequestCallback,
                                     boolean isCache) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            mCallback = adRequestCallback;
            mCached = isCache;
        }

        @Override
        public void run() {
            Iterator<ReaperAdSense> it = mReaperAdSenses.iterator();
            ReaperAdSense reaperAdSense = it.next();
            it.remove();
            String adsName = reaperAdSense.ads_name;
            if (mSdkWrappers == null)
                return;
            ISDKWrapper sdkWrapper = mSdkWrappers.get(adsName);
            if (sdkWrapper == null) {
                onRequestAdError(mCallback,
                        "Can not find " + adsName + "'s sdk implements, may need " +
                                "upgrade reaper jar, current version " + BumpVersion.value());
                return;
            }

            int adType = 0;
            if (mAdTypeMap.containsKey(mAdvType)) {
                adType = mAdTypeMap.get(mAdvType);
            } else {
                onRequestAdError(mCallback, "Can not find match ad type with type name " +
                        mAdvType);
                return;
            }

            AdRequest.Builder builder = new AdRequest.Builder()
                    .appId(reaperAdSense.ads_appid)
                    .adPositionId(reaperAdSense.ads_posid)
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
                onRequestAdError(mCallback, "[TEST] Not support adv size type " +
                        reaperAdSense.adv_size_type);
                return;
            } else {
                onRequestAdError(mCallback, "Not support adv size type " +
                        reaperAdSense.adv_size_type);
                return;
            }

            sdkWrapper.requestAdAsync(builder.create(),
                    mCached ? new AdResponseCacheCallback(mReaperAdSenses, mAdvType, reaperAdSense, mCallback) :
                            new AdResponseCallback(mReaperAdSenses, mAdvType, mCallback));
        }
    }

    private class AdResponseCallback implements AdResponseListener {

        private List<ReaperAdSense> mReaperAdSenses;
        private String mAdvType;
        private Object mCallback;

        public AdResponseCallback(List<ReaperAdSense> reaperAdSenses,
                                  String advType,
                                  Object adRequestCallback) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            mCallback = adRequestCallback;
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
                onRequestAd(mCallback, adResponse.getAdAllParams());
            }
        }

        private boolean nextRequest() {
            if (mReaperAdSenses != null &&
                    mReaperAdSenses.size() > 0) {
                mThreadPoolUtils.execute(
                        new RequestSingleAdRunner(mReaperAdSenses, mAdvType, mCallback));
                return true;
            } else {
                return false;
            }
        }
    }

    private class AdResponseCacheCallback implements AdResponseListener {

        private List<ReaperAdSense> mReaperAdSenses;
        private String mAdvType;
        private ReaperAdSense curAdSense ;
        private Object mCallback;

        public AdResponseCacheCallback(List<ReaperAdSense> reaperAdSenses,
                                       String advType,
                                       ReaperAdSense adSense,
                                       Object adRequestCallback) {
            mReaperAdSenses = reaperAdSenses;
            mAdvType = advType;
            curAdSense = adSense;
            mCallback = adRequestCallback;
        }

        @Override
        public void onAdResponse(AdResponse adResponse) {
            if (adResponse == null) {
                if (!nextRequest()) {
                    onRequestAdError(mCallback, "Response with no result");
                }
                return;
            }

            if ((adResponse.isSucceed() && adResponse.canCache()) || !nextRequest()) {
                AdCacheInfo info = new AdCacheInfo();
                ISDKWrapper sdkWrapper = mSdkWrappers.get(curAdSense.ads_name);
                ICacheConvert convert = (ICacheConvert) sdkWrapper;
                info.setAdSource(curAdSense.ads_name);
                info.setCache(convert.convertToString(adResponse));
                info.setExpireTime(curAdSense.expire_time);
                try {
                    cacheAdInfo(curAdSense.getPosId(), info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean nextRequest() {
            if (mReaperAdSenses != null &&
                    mReaperAdSenses.size() > 0) {
                mThreadPoolUtils.execute(
                        new RequestSingleAdRunner(mReaperAdSenses, mAdvType, mCallback));
                return true;
            } else {
                return false;
            }
        }
    }
}

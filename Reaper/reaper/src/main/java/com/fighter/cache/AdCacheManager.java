package com.fighter.cache;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LongSparseArray;

import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.cache.downloader.AdCacheFileDownloadManager;
import com.fighter.common.PriorityTaskDaemon;
import com.fighter.common.utils.OpenUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.common.utils.ThreadPoolUtils;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;
import com.fighter.reaper.BumpVersion;
import com.fighter.tracker.EventActionParam;
import com.fighter.tracker.EventClickParam;
import com.fighter.tracker.EventDisPlayParam;
import com.fighter.tracker.EventDownLoadParam;
import com.fighter.tracker.Tracker;
import com.fighter.tracker.TrackerEventType;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.fighter.wrapper.AdRequest;
import com.fighter.wrapper.AdResponse;
import com.fighter.wrapper.AdResponseListener;
import com.fighter.wrapper.DownloadCallback;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fighter.ad.AdEvent.EVENT_APP_ACTIVE;
import static com.fighter.ad.AdEvent.EVENT_APP_DOWNLOAD_CANCELED;
import static com.fighter.ad.AdEvent.EVENT_APP_DOWNLOAD_COMPLETE;
import static com.fighter.ad.AdEvent.EVENT_APP_DOWNLOAD_FAILED;
import static com.fighter.ad.AdEvent.EVENT_APP_INSTALL;
import static com.fighter.ad.AdEvent.EVENT_APP_START_DOWNLOAD;
import static com.fighter.ad.AdEvent.EVENT_CLICK;
import static com.fighter.ad.AdEvent.EVENT_CLOSE;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_CARD_CLICK;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_CONTINUE;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_EXIT;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_FULLSCREEN;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_PAUSE;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_PLAY_COMPLETE;
import static com.fighter.ad.AdEvent.EVENT_VIDEO_START_PLAY;
import static com.fighter.ad.AdEvent.EVENT_VIEW_FAIL;
import static com.fighter.ad.AdEvent.EVENT_VIEW_SUCCESS;

/**
 * the class to manager the ad cache.
 * init() fill the all the posId cache.
 * requestAdCache() firstly request ad from cache, if cache is null,
 * request two ads, one for return to client, another for cache.
 * <p>
 * Created by lichen on 17-5-17.
 */

public class AdCacheManager implements DownloadCallback{

    private static final String TAG = AdCacheManager.class.getSimpleName();
    private static final String EXTRA_EVENT_DOWN_X = "downX";
    private static final String EXTRA_EVENT_DOWN_Y = "downY";
    private static final String EXTRA_EVENT_UP_X = "upX";
    private static final String EXTRA_EVENT_UP_Y = "upY";

    private static final long EFFECTIVE_TIME = 3*60*1000;
    private static final String SALT = "cf447fe3adac00476ee9244fd30fba74";
    private static final String METHOD_ON_RESPONSE = "onResponse";

    private static final int CACHE_MAX = 5;
    private static AdCacheManager mAdCacheManager = new AdCacheManager();
    private static ApkInstallReceiver mInstallReceiver;
    private File mCacheDir;

    private Map<String, ISDKWrapper> mSdkWrapperSupport;
    private Map<String, String> mSdkWrapperAdTypeSupport;
    private Map<String, Method> mMethodCall;

    private ThreadPoolUtils mThreadPoolUtils = new ThreadPoolUtils(ThreadPoolUtils.SingleThread, 1);
    private LongSparseArray<AdInfo> mDownloadApps;
    private Map<String, Long> mInstallApps;
    private Map<String, AdInfo> mInstallAds;
    private ReaperAdvPos mReaperAdvPos;

    /**************************************************Init cache task start*****************************************************************/
    private boolean mInitCacheSuccess = false;

    /**
     * for init cache for all posIds
     */
    private class InitCacheTask extends PriorityTaskDaemon.NotifyPriorityTask {

        public InitCacheTask(int priority, PriorityTaskDaemon.TaskRunnable runnable, PriorityTaskDaemon.TaskNotify notify) {
            super(priority, runnable, notify);
        }
    }

    private PriorityTaskDaemon.TaskRunnable mInitRunnable = new PriorityTaskDaemon.TaskRunnable() {
        @Override
        public Object doSomething() {
            boolean initSuccess = initCache(mContext);
            ReaperLog.i(TAG, "InitCacheRunnable do something init " + initSuccess);
//            String[] posIds = getAllPosId(context);
//            for (String posId : posIds) {
//                postAdRequestWrapperTask(posId, null, true, InitCacheRunnable.this);
//            }
            return initSuccess;
        }
    };

    private PriorityTaskDaemon.TaskNotify mInitNotify = new PriorityTaskDaemon.TaskNotify() {
        @Override
        public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
            boolean init = false;
            if (result instanceof Boolean)
                init = (boolean) result;
            ReaperLog.i(TAG, " init cache task on result method is called and init " + init);
        }
    };

    private InitCacheTask mInitTask = new InitCacheTask(
            PriorityTaskDaemon.PriorityTask.PRI_FIRST, mInitRunnable, mInitNotify);

    /****************************************************Init cache Task end**************************************************************************/


    /****************************************************Tracker Task start**************************************************************************/
    /**
     * TrackerTask is used for tracking  onEvent {@link Tracker#onEvent}
     * and ISDKWrapper {@link ISDKWrapper#onEvent(int, AdInfo)}
     */
    private class TrackerTask extends PriorityTaskDaemon.NotifyPriorityTask {

        public TrackerTask(int priority, PriorityTaskDaemon.TaskRunnable runnable, PriorityTaskDaemon.TaskNotify notify) {
            super(priority, runnable, notify);
        }
    }

    private class TrackerRunnable extends PriorityTaskDaemon.TaskRunnable{

        private final String TAG = TrackerRunnable.class.getSimpleName();
//        private Context context;
//        private Tracker tracker;
        private int actionEvent;
        private AdInfo adInfo;
//        private String errMsg;
//        private ApkInstallReceiver installReceiver;

//        public void setContext(Context context) {
//            this.context = context;
//        }

//        public void setTracker(Tracker tracker) {
//            this.tracker = tracker;
//        }

        public void setActionEvent(int actionEvent) {
            this.actionEvent = actionEvent;
        }

        public void setAdInfo(AdInfo adInfo) {
            this.adInfo = adInfo;
        }

//        public void setErrMsg(String errMsg) {
//            this.errMsg = adInfo.getAdName() + " " + errMsg;
//        }

        public TrackerRunnable() {
        }

        public TrackerRunnable(int actionEvent, AdInfo adInfo) {
            this.actionEvent = actionEvent;
            this.adInfo = adInfo;
        }

        @Override
        public Object doSomething() {
//            if (context == null) {
//                ReaperLog.e(TAG, "tracker runnable init context is null");
//                return null;
//            }
            //open web url or download app
//            handleTouchEvent(actionEvent, adInfo);

            //ISdkWrapper onEvent
            String adName = adInfo.getAdName();
            if(!TextUtils.isEmpty(adName)) {
                ISDKWrapper wrapper = mSdkWrapperSupport.get(adName);
                if(wrapper != null) {
                    wrapper.onEvent(actionEvent, adInfo);
                } else {
                    ReaperLog.e("Reaper sdk can not support " + adName);
                }
            }

            //Tracker onEvent
//            trackActionEvent(actionEvent, errMsg, adInfo);
            return adInfo;
        }

    }

    private TrackerRunnable mTrackerRunner  = new TrackerRunnable();
    private PriorityTaskDaemon.TaskNotify mTrackerNotify = new PriorityTaskDaemon.TaskNotify() {
        @Override
        public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
            ReaperLog.i(TAG, "tracker task onResult method is called");
        }
    };
    private TrackerTask mTrackerTask = new TrackerTask(
            PriorityTaskDaemon.PriorityTask.PRI_FIRST, mTrackerRunner, mTrackerNotify);
    /****************************************************Tracker Task end**************************************************************************/
    /****************************************************update config Task start**************************************************************************/
    private PriorityTaskDaemon.TaskRunnable mUpdateConfigRunner = new PriorityTaskDaemon.TaskRunnable() {
        @Override
        public Object doSomething() {
            return updateConfig();
        }
    };

    private PriorityTaskDaemon.TaskNotify mUpdateConfigNotify = new PriorityTaskDaemon.TaskNotify() {
        @Override
        public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
            if (result instanceof Boolean) {
                if (!(boolean)result && !needHoldAd)
                    onRequestAdError(mCallBack, "update config failed");
            }
        }
    };

    private PriorityTaskDaemon.NotifyPriorityTask mUpdateConfigTask = new PriorityTaskDaemon.NotifyPriorityTask(
            PriorityTaskDaemon.PriorityTask.PRI_FIRST, mUpdateConfigRunner, mUpdateConfigNotify);
    /****************************************************update config Task start**************************************************************************/
    /****************************************************AdRequestWrapper Task start**************************************************************************/
    /**
     * request wrapper task for callback cache and notify user
     */
    private class AdRequestWrapperTask extends PriorityTaskDaemon.NotifyPriorityTask implements AdResponseListener {
        private String mPosId;
        private Object mCallBack;
        private boolean mCache;
        private int mLocation;
        private List<ReaperAdSense> mAdSenseList;

        public int getLocation() {
            return mLocation;
        }

        public void setLocation(int mLocation) {
            this.mLocation = mLocation;
        }

        public void setPosId(String mPosId) {
            this.mPosId = mPosId;
        }

        public void setCallBack(Object mCallBack) {
            this.mCallBack = mCallBack;
        }

        public void setCache(boolean mCache) {
            this.mCache = mCache;
        }

        public String getPosId() {
            return mPosId;
        }

        public Object getCallBack() {
            return mCallBack;
        }

        public boolean isCache() {
            return mCache;
        }

        public AdRequestWrapperTask(int priority, PriorityTaskDaemon.TaskRunnable runnable,
                                    PriorityTaskDaemon.TaskNotify notify, String mPosId,
                                    Object mCallBack, boolean mCache, List<ReaperAdSense> list) {
            super(priority, runnable, notify);
            this.mPosId = mPosId;
            this.mCallBack = mCallBack;
            this.mCache = mCache;
            this.mAdSenseList = list;
        }

        public AdRequestWrapperTask(PriorityTaskDaemon.NotifyPriorityTask task, String mPosId,
                                    Object mCallBack, boolean mCache, List<ReaperAdSense> list) {
            super(task);
            this.mPosId = mPosId;
            this.mCallBack = mCallBack;
            this.mCache = mCache;
            this.mAdSenseList = list;
        }

        @Override
        public void onAdResponse(AdResponse adResponse) {
            AdRequestWrapperAsyncRunner runner = new AdRequestWrapperAsyncRunner();
            runner.setAdResponse(adResponse);
            runner.setAdSenseList(mAdSenseList);
            runner.setLocation(mLocation);
            this.setRunnable(runner);
            mWorkThread.postTaskInFront(AdRequestWrapperTask.this);
        }
    }

    private class AdRequestWrapperAsyncRunner extends PriorityTaskDaemon.TaskRunnable {
        private AdResponse mAdResponse;
        private List<ReaperAdSense> mAdSenseList;
        private int mLocation;

        public int getLocation() {
            return mLocation;
        }

        public void setLocation(int mLocation) {
            this.mLocation = mLocation;
        }

        public void setAdResponse(AdResponse mAdResponse) {
            this.mAdResponse = mAdResponse;
        }

        public void setAdSenseList(List<ReaperAdSense> mAdSenseList) {
            this.mAdSenseList = mAdSenseList;
        }

        public AdRequestWrapperAsyncRunner() {
        }

        public AdRequestWrapperAsyncRunner(AdResponse mAdResponse) {
            this.mAdResponse = mAdResponse;
        }

        @Override
        public Object doSomething() {
            AdRequestWrapperTask task = (AdRequestWrapperTask) getTask();
            if(mAdResponse == null || mReaperAdvPos == null) return null;
            if (mAdResponse.isSucceed()) {
                return mAdResponse.getAdInfo();
            }
            while(mAdResponse != null && !mAdResponse.isSucceed()) {
                ReaperLog.i(TAG, "Async runner task: " + task);
                if (task != null) {
                    mAdResponse = requestWrapperAdInner(mAdSenseList, mLocation, mReaperAdvPos.adv_type,
                            task);
                    task.setLocation(mLocation++);
                } else {
                    break;
                }
            }
            if (mAdResponse != null && mAdResponse.isSucceed()) {
                return mAdResponse.getAdInfo();
            }
            if (mAdResponse != null) {
                if (needHoldAd) {
                    return generateHoldAd(mCacheId);
                }
            }
            return "all ads not get ad";
        }
    }

    private class AdRequestWrapperRunner extends PriorityTaskDaemon.TaskRunnable {
        private String mPosId;
        private List<ReaperAdSense> mAdSenseList;

        public void setPosId(String mPosId) {
            this.mPosId = mPosId;
        }

        public void setmAdSenseList(List<ReaperAdSense> mAdSenseList) {
            this.mAdSenseList = mAdSenseList;
        }

        public AdRequestWrapperRunner() {
        }

        public AdRequestWrapperRunner(String mPosId, List<ReaperAdSense> list) {
            this.mPosId = mPosId;
            this.mAdSenseList = list;
        }

        @Override
        public Object doSomething() {
            AdInfo adInfo = null;
            AdResponse adResponse;
            AdRequestWrapperTask task = (AdRequestWrapperTask) getTask();
            int location = task.getLocation();
            if (mAdSenseList == null || mReaperAdvPos == null) {
                return null;
            }

            ReaperLog.i(TAG, "Reaper advPos: " + mReaperAdvPos + ",Reaper adSenses:" + mAdSenseList);
            updateWrapper(mAdSenseList);
            do {
                adResponse = requestWrapperAdInner(mAdSenseList, location, mReaperAdvPos.adv_type, task);
                task.setLocation(location++);
            } while (adResponse != null && !adResponse.isSucceed());

            if (adResponse != null && adResponse.isSucceed()) {
                adInfo = adResponse.getAdInfo();
                ReaperLog.i(TAG, "wrapper runner adInfo: " + adInfo.getUUID() + ", hash: " + adInfo.hashCode());
            }
            downloadAdResourceFile(adInfo);
            if (mAdSenseList != null && location > mAdSenseList.size()) {
                  return needHoldAd ? generateHoldAd(mPosId) : "all ads not get ad";
            }
            return adInfo;
        }
    }

    private class AdRequestWrapperNotify implements PriorityTaskDaemon.TaskNotify {
        @Override
        public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
            boolean isCache = false;
            Object callBack = null;
            AdRequestWrapperTask adRequestWrapperTask;
            AdInfo adInfo;
            if (task instanceof AdRequestWrapperTask) {
                adRequestWrapperTask = (AdRequestWrapperTask) task;
                isCache = adRequestWrapperTask.isCache();
                callBack = adRequestWrapperTask.getCallBack();
            }
            if (result != null && result instanceof AdInfo) {
                adInfo = (AdInfo)result;
                ReaperLog.i(TAG, "request wrapper ad info: " + adInfo.getUUID());
//                Log.i(TAG, "request wrapper ad info: " + adInfo.getUUID());
//                Log.i(TAG, "\n");
                if (isCache) {
                    cacheAdInfo(adInfo);
                } else {
//                    Log.i(TAG, "AdRequestWrapperNotify task: " + task.hashCode());
//                    Log.i(TAG, "\n");
                    onRequestAdSucceed(callBack, adInfo);
                }
            }
            if (result != null && result instanceof String) {
                onRequestAdError(callBack, (String) result);
            }

        }
    }
    private AdRequestWrapperAsyncRunner mAdRequestWrapperAsyncRunner = new AdRequestWrapperAsyncRunner();
    private AdRequestWrapperNotify mAdRequestWrapperNotify = new AdRequestWrapperNotify();
    /****************************************************AdRequestWrapper Task end**************************************************************************/

    /****************************************************AdRequestTask Task start**************************************************************************/
    /**
     * for user ad request
     */
    private class AdRequestTask extends PriorityTaskDaemon.NotifyPriorityTask {
        private String mPosId;
        private Object mCallBack;

        public String getPosId() {
            return mPosId;
        }

        public void setPosId(String mPosId) {
            this.mPosId = mPosId;
        }

        public Object getCallBack() {
            return mCallBack;
        }

        public void setCallBack(Object mCallBack) {
            this.mCallBack = mCallBack;
        }

        public AdRequestTask(int priority, PriorityTaskDaemon.TaskRunnable runnable,
                             PriorityTaskDaemon.TaskNotify notify) {
            super(priority, runnable, notify);
        }

        public AdRequestTask(int priority, PriorityTaskDaemon.TaskRunnable runnable,
                             PriorityTaskDaemon.TaskNotify notify, String mPosId, Object mCallBack) {
            super(priority, runnable, notify);
            this.mPosId = mPosId;
            this.mCallBack = mCallBack;
        }
    }

    private class AdRequestRunner extends PriorityTaskDaemon.TaskRunnable {
        private String mPosId;
        private Object mCallBack;
        private List<ReaperAdSense> mAdSenseList;

        public void setPosId(String mPosId) {
            this.mPosId = mPosId;
        }

        public void setCallBack(Object mCallBack) {
            this.mCallBack = mCallBack;
        }

        public AdRequestRunner(){
        }

        public AdRequestRunner(String posId, Object callBack) {
            mPosId = posId;
            mCallBack = callBack;
        }

        @Override
        public Object doSomething() {
            mReaperAdvPos = ReaperConfigManager.getReaperAdvPos(mContext, mPosId);
            IAdRequestPolicy policy = AdRequestPolicyManager.getAdRequestPolicy(mContext, mPosId);
            if (policy != null) {
                mAdSenseList = policy.generateList();
            } else {
                return "AdRequestPolicyManager getAdRequestPolicy posId " + mPosId + " policy is null";
            }
            // 1.post a task to pull ad for cache
            postAdRequestWrapperTask(mPosId, null, true, mAdSenseList, AdRequestRunner.this);
            // 2. if cache is full, back cache ad info
            Object info = getCacheAdInfo(mPosId);
            AdCacheInfo adCacheInfo;
            Object cache;
            AdInfo adInfo = null;
            if (info != null && info instanceof AdCacheInfo) {
                adCacheInfo = (AdCacheInfo)info;
                ReaperLog.i(TAG, "ad cache info： " + adCacheInfo.getUuid() + "; mState: " + adCacheInfo.getCacheState() +
                        "; hash: " + adCacheInfo.hashCode());
                ReaperLog.i(TAG, "\n");
                cache = adCacheInfo.getCache();
                if (cache instanceof String) {
                    adInfo = AdInfo.convertFromString((String)cache);
                } else if (cache instanceof AdInfo) {
                    adInfo = (AdInfo) cache;
                }
                if (isAdCacheTimeout(adCacheInfo) && adInfo != null) {
                    EventDisPlayParam param = new EventDisPlayParam();
                    param.ad_info = adInfo;
                    param.ad_num = 1;
                    param.ad_appid = Integer.parseInt(mAppId);
                    param.ad_info = adInfo;
                    param.app_pkg = mContext.getPackageName();
                    param.result = "failed";
                    param.reason = "timeout";
                    mReaperTracker.trackDisplayEvent(mContext, param);
                } else {
                    setCacheUsed(adCacheInfo);
                    return adInfo;
                }
            }
            // 3. if cache is empty, post a task call wrapper get ad
            postAdRequestWrapperTask(mPosId, mCallBack, false, mAdSenseList, AdRequestRunner.this);
            return null;
        }
    }

    private class AdRequestNotify implements PriorityTaskDaemon.TaskNotify {
        @Override
        public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
            Object callBack = null;
            if (task instanceof AdRequestTask) {
                callBack = ((AdRequestTask) task).getCallBack();
            }
            if (result != null && result instanceof AdInfo) {
                onRequestAdSucceed(callBack, (AdInfo) result);
            }
            if (result != null && result instanceof String) {
                onRequestAdError(callBack, (String)result);
            }
        }
    }
    /****************************************************AdRequestTask Task end**************************************************************************/

    /**
     * the memory cache object
     */
    private static HashMap<String, ArrayMap<String, Object>> mAdCache = new HashMap<>();

    private PriorityTaskDaemon mWorkThread;
    private Context mContext;

    private String mCacheId;
    private String mAppId;
    private String mAppKey;
    private Object mCallBack;

    private boolean needHoldAd;
    private AdCacheFileDownloadManager mAdFileManager;
    private Tracker mReaperTracker;

    public static synchronized AdCacheManager getInstance() {
        if (mAdCacheManager == null)
            mAdCacheManager = new AdCacheManager();
        return mAdCacheManager;
    }

    private AdCacheManager() {
    }

    /**
     * execute init cache task
     *
     * @param context
     */
    private void postInitCacheTask(Context context) {
        mWorkThread.postTaskInFront(mInitTask);
    }

    /**
     * update config from server
     *
     */
    private void postUpdateConfigTask() {
        mWorkThread.postTaskInFront(mUpdateConfigTask);
    }

    /**
     * this method called by InitCacheRunnable
     *
     * @param context
     * @return whether cache init success
     */
    private boolean initCache(Context context) {
        if (context == null) {
            ReaperLog.e(TAG, " init cache method context is null should return early");
            return mInitCacheSuccess;
        }
        mAdFileManager = AdCacheFileDownloadManager.getInstance(context);
        mAdFileManager.setDownloadCallback(this);
        mDownloadApps = new LongSparseArray<>();
        mInstallApps = new HashMap<>();
        mInstallAds = new HashMap<>();
        String cacheId = null;
        ArrayMap<String, Object> adCacheObjects = new ArrayMap<>();
        File adCacheDir = new File(context.getCacheDir(), "ac");
        if (!adCacheDir.exists())
            adCacheDir.mkdir();
        mInitCacheSuccess = adCacheDir.exists();
        mCacheDir = adCacheDir;
        File[] cacheDirs = adCacheDir.listFiles();
        for (File dir : cacheDirs) {
            if (!dir.isDirectory())
                continue;
            cacheId = dir.getName();
            judgeCleanOrPutCache(adCacheObjects, dir);
        }

        if (adCacheObjects.size() > 0) {
            mAdCache.put(cacheId, adCacheObjects);
        }
        return mInitCacheSuccess;
    }

    /**
     * judge the ad cache should clean or put ArrayMap
     *
     * @param adCacheObjects
     * @param dir
     */
    private void judgeCleanOrPutCache(ArrayMap<String, Object> adCacheObjects, File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.isFile())
                continue;
            try {
                Object adInfo = getAdCacheFromFile(file);
                AdCacheInfo adCacheInfo = (AdCacheInfo) adInfo;
                if ((adCacheInfo.isCacheDisPlayed() || isAdCacheTimeout(adInfo)) && adCacheObjects.size() > 1) {
                    cleanBeforeCache(adCacheInfo);
                } else {
                    adCacheObjects.put(((AdCacheInfo) adInfo).getUuid(), adInfo);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

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
        mReaperTracker = Tracker.getTracker();
        mReaperTracker.init(mContext);
        postInitCacheTask(mContext);
    }

    /**
     * request the ad cache to get the Ad information.
     */
    public void requestAdCache(int adCount, String cacheId, Object callBack) {
        mCacheId = cacheId;
        mCallBack = callBack;
        postUpdateConfigTask();
        for(int i = 0; i < adCount; i++) {
            postAdRequestTask(mCacheId, mCallBack);
        }
    }

    /**
     * the flags need hold ad
     *
     * @param needHoldAd
     */
    public void setNeedHoldAd(boolean needHoldAd) {
        this.needHoldAd = needHoldAd;
    }

    private AdInfo generateHoldAd(String posId) {
        ReaperLog.i(TAG, "generateHoldAd");
        AdCacheInfo adCacheInfo;
        AdInfo info = null;
        ArrayMap<String, Object> cacheObjects = mAdCache.get(posId);
        if (cacheObjects != null && cacheObjects.size() > 0) {
            adCacheInfo = (AdCacheInfo) cacheObjects.get(cacheObjects.keyAt(0));
            if (adCacheInfo != null) {
                Object object = adCacheInfo.getCache();
                if (object instanceof String) {
                    info = AdInfo.convertFromString((String) object);
                } else if (object instanceof AdInfo) {
                    info =  (AdInfo)object;
                }
                if (info != null) {
                    info.setAdInfoAvailable(false);
                    adCacheInfo.setCacheState(AdCacheInfo.CACHE_IS_HOLD_AD);
                }
                updateDiskCache(adCacheInfo);
            }
        }

        return info;
    }

    private String[] getAllPosId(Context context) {
        List<ReaperAdvPos> reaperAdvPoses = ReaperConfigManager.getAllReaperAdvPos(context);
        String[] posIds = new String[reaperAdvPoses.size()];
        for (int i = 0; i < reaperAdvPoses.size(); i++) {
            posIds[i] = reaperAdvPoses.get(i).pos_id;
        }
        return posIds;
    }

    private void updateDiskCache(AdCacheInfo adCacheInfo) {
        File cacheDir = new File(mCacheDir, adCacheInfo.getAdCacheId());
        if (cacheDir.isDirectory() && cacheDir.exists()) {
            File cacheFile = new File(cacheDir, String.valueOf(adCacheInfo.getUuid()));
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

    private void setCacheUsed(AdCacheInfo adCacheInfo) {
//        Log.i(TAG, "setCacheUsd");
//        Log.i(TAG, "\n\n");
        if (adCacheInfo == null)
            return;
        adCacheInfo.setCacheState(AdCacheInfo.CACHE_BACK_TO_USER);
        updateDiskCache(adCacheInfo);
//        Log.i(TAG, "setCacheUsed adCacheInfo : " + adCacheInfo.getUuid() + ", mState: " + adCacheInfo.getCacheState());
//        Log.i(TAG, "\n\n");
    }

    private void setCacheDisplayed(AdInfo adInfo) {
        if (adInfo == null)
            return;
        ArrayMap<String, Object> cacheObjects = mAdCache.get(adInfo.getAdPosId());
        if (cacheObjects == null)
            return;
        Object object = cacheObjects.get(adInfo.getUUID());
        if (object instanceof AdCacheInfo) {
            AdCacheInfo adCacheInfo = (AdCacheInfo) object;
            adCacheInfo.setCacheState(AdCacheInfo.CACHE_DISPLAY_BY_USER);
            if (cacheObjects.size() > 1) {
                cacheObjects.remove(adCacheInfo.getUuid());
                cleanBeforeCache(adCacheInfo);
            }
        }
    }

    /**
     * the method is used to cache ad information to sdcard.
     *
     * @param cacheId the ad unique id
     * @param info  the object of ad information
     * @throws IOException it maybe throw IOException
     */
    private void cacheAdInfo(String cacheId, Object info) throws IOException {
        if (cacheId == null || info == null)
            return;
        ArrayMap<String, Object> adCacheInfoObjects = null;
        AdCacheInfo adCacheInfo = (AdCacheInfo) info;
        String cacheFileId = adCacheInfo.getUuid();
        if (mAdCache.containsKey(cacheId)) {
            adCacheInfoObjects = mAdCache.get(cacheId);
        } else {
            adCacheInfoObjects = new ArrayMap<>();
        }
        Object cache = adCacheInfo.getCache();
        if (cache instanceof String) {
            File cacheIdDir = new File(mCacheDir, cacheId);
            if (!cacheIdDir.exists()) {
                cacheIdDir.mkdir();
            }
            File adInfoFile = new File(cacheIdDir, cacheFileId);
            if (!adInfoFile.exists()) {
                adInfoFile.createNewFile();
            }
            adCacheInfo.setCachePath(adInfoFile.getAbsolutePath());
            adCacheInfoObjects.put(adCacheInfo.getUuid(), adCacheInfo);
            mAdCache.put(cacheId, adCacheInfoObjects);
            FileOutputStream fileOutputStream = new FileOutputStream(adInfoFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(adCacheInfo);
            objectOutputStream.close();
            fileOutputStream.close();
        } else if (cache instanceof AdInfo) {
            adCacheInfo.setCachePath(null);
            adCacheInfoObjects.put(adCacheInfo.getUuid(), adCacheInfo);
            mAdCache.put(cacheId, adCacheInfoObjects);
        }
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
            for (File file : cacheFileList) {
                adInfo = (AdCacheInfo) getAdCacheFromFile(file);
                if (adInfo != null &&
                        !adInfo.isCacheBackToUser() &&
                            !adInfo.isCacheDisPlayed() &&
                                !adInfo.isHoldAd()) {
                    break;
                } else {
                    adInfo = null;
                }
            }
        }
//        Log.i(TAG, "getCacheInfo memory: " + adInfo);
//        Log.i(TAG, "\n\n");
        return adInfo;
    }

    /**
     * cache ad file
     *
     * @param imageUrl
     * @return cache file instance
     */
    private File cacheAdFile(String imageUrl) throws Exception {
        return mAdFileManager.cacheAdFile(imageUrl);
    }

    /**
     * the method is used to get the ad cache information from sdcard.
     *
     * @param cacheId the ad unique id..
     * @return the object about ad information
     */
    private Object getCacheAdInfo(String cacheId) {
        if (cacheId == null)
            return null;
        /* 1. find ad cache in memory cache */
        Object adInfo = null;
        ArrayMap<String, Object> adInfoObjects = mAdCache.get(cacheId);
        if (adInfoObjects != null) {
            for (int i = 0; i < adInfoObjects.size(); i++) {
                adInfo = adInfoObjects.get(adInfoObjects.keyAt(i));
                if (adInfo != null &&
                        adInfo instanceof AdCacheInfo &&
                        !((AdCacheInfo) adInfo).isCacheBackToUser() &&
                            !((AdCacheInfo) adInfo).isCacheDisPlayed() &&
                            !((AdCacheInfo) adInfo).isHoldAd()) {
                    break;
                } else {
                    adInfo = null;
                }
            }
        }
//        Log.i(TAG, "getCacheInfo memory: " + adInfo);
//        Log.i(TAG, "\n\n");
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
        return adInfo;
    }

    public void collateAdCache(String cacheId) {
        if (cacheId == null)
            return;
        ArrayMap<String, Object> cacheObjects = mAdCache.get(cacheId);
        AdCacheInfo cacheInfoInBottom = null;
        if (cacheObjects != null) {
            cacheInfoInBottom = (AdCacheInfo) cacheObjects.get(cacheObjects.keyAt(0));
        }
        if (cacheObjects != null && cacheObjects.size() > CACHE_MAX) {
            if (cacheInfoInBottom != null && cacheInfoInBottom.isCacheBackToUser()) {
                String cachePath = cacheInfoInBottom.getCachePath();
                if (cachePath != null) {
                    File cacheFile = new File(cachePath);
                    if (cacheFile.exists()) {
                        cacheFile.delete();
                    }
                }
                cacheObjects.remove(cacheObjects.keyAt(0));
            }
        }
    }

    public void onRequestAdError(Object receiver, String errMsg) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("isSucceed", false);
        params.put("errMsg", errMsg);
        onRequestAd(receiver, params);
    }

    private void onRequestAdSucceed(Object receiver, AdInfo adInfo) {
        ReaperLog.i(TAG, "on success ad info: " + adInfo);
//        Log.i(TAG, "on success ad info: " + adInfo.getUUID()+ ", hash: " + adInfo.hashCode());
//        Log.i(TAG, "\n\n");
        ArrayMap<String, Object> params = new ArrayMap<>();
        if (adInfo != null) {
            params.put("isSucceed", true);
            params.put("adInfo", adInfo.getAdAllParams());
        } else {
            onRequestAdError(receiver, "request ad succeed, but with no ad response");
        }
        onRequestAd(receiver, params);
    }

    /**
     * post tracker event task in this method
     * @param adInfo
     * @param actionEvent
     */
    private void postTrackerTask(AdInfo adInfo, int actionEvent) {
        postTrackerTask(adInfo, actionEvent, null);
    }

    /**
     * post tracker event task in this method
     *
     * @param actionEvent 行为事件
     * @param adInfo      广告信息
     * @param errMsg      用来描述下载失败原因
     */
    private void postTrackerTask(final AdInfo adInfo, int actionEvent, String errMsg) {
//        TrackerRunnable trackerRunnable = new TrackerRunnable(context, tracker, actionEvent, adInfo, wrapper);
//        TrackerTask trackerTask = new TrackerTask(
//                PriorityTaskDaemon.PriorityTask.PRI_FIRST,
//                trackerRunnable,
//                new PriorityTaskDaemon.TaskNotify() {
//                    @Override
//                    public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
//                        ReaperLog.i(TAG, "tracker task onResult method is called");
//                        if (result.equals(adInfo)) {
//                            ReaperLog.i(TAG, "tracker ad info " + adInfo.getAdPosId() + "tracker event has handled");
//                        }
//                    }
//                }
//        );
        if(adInfo == null)
            return;
//        mTrackerRunner.setContext(mContext);
//        mTrackerRunner.setTracker(mReaperTracker);
        mTrackerRunner.setActionEvent(actionEvent);
        mTrackerRunner.setAdInfo(adInfo);
//        if(!TextUtils.isEmpty(errMsg))
//            mTrackerRunner.setErrMsg(errMsg);
        mWorkThread.postTask(mTrackerTask);
    }

    /**
     * This method is support for ReaperApi and use Tracker task to record event
     *
     * @param actionEvent
     * @param adInfo
     */
    public void onEvent(int actionEvent, AdInfo adInfo) {
        if (actionEvent == EVENT_VIEW_SUCCESS) {
            setCacheDisplayed(adInfo);
        }
        //jump to webView and download app here
        trackActionEvent(actionEvent, adInfo);
        postTrackerTask(adInfo, actionEvent);
    }


    /*************************************************start move from tracker to fix open webView so many times*****************************************************************/

    private void trackActionEvent(int actionEvent, AdInfo adInfo) {
        trackActionEvent(actionEvent, adInfo, null);
    }

    private void trackActionEvent(int actionEvent, AdInfo adInfo, String errMsg) {
        switch (actionEvent) {
            case EVENT_VIEW_FAIL:
            case EVENT_VIEW_SUCCESS:
                EventDisPlayParam disPlayParam = new EventDisPlayParam();
                disPlayParam.ad_info = adInfo;
                disPlayParam.ad_num = 1;
                disPlayParam.ad_appid = Integer.parseInt(mAppId);
                disPlayParam.app_pkg = mContext.getPackageName();
                disPlayParam.result = actionEvent == EVENT_VIEW_SUCCESS ? "ok" : "fail";
                disPlayParam.reason = actionEvent == EVENT_VIEW_SUCCESS ? "" : "onAdShow fail view is null";
                mReaperTracker.trackDisplayEvent(mContext, disPlayParam);
                break;
            case EVENT_CLICK:
                handleClickAction(adInfo);
                EventClickParam clickParam = new EventClickParam();
                clickParam.ad_info = adInfo;
                clickParam.ad_num = 1;
                clickParam.ad_appid = Integer.parseInt(mAppId);
                clickParam.app_pkg = mContext.getPackageName();
                clickParam.click_pos = loadCoordinate(adInfo);
                mReaperTracker.trackClickEvent(mContext, clickParam);
                break;
            case EVENT_CLOSE:
                break;
            case EVENT_APP_START_DOWNLOAD:
            case EVENT_APP_DOWNLOAD_COMPLETE:
            case EVENT_APP_DOWNLOAD_FAILED:
            case EVENT_APP_DOWNLOAD_CANCELED:
            case EVENT_APP_INSTALL:
            case EVENT_APP_ACTIVE:
                EventActionParam actionParam = new EventActionParam();
                actionParam.ad_info = adInfo;
                actionParam.ad_num = 1;
                actionParam.ad_appid = Integer.parseInt(mAppId);
                actionParam.app_pkg = mContext.getPackageName();
                actionParam.act_type = loadAppActionType(actionEvent);
                actionParam.reason = loadAppActReason(actionEvent, errMsg);
                actionParam.download_app_pkg = adInfo.getAppPackageName();
                actionParam.download_app_name = adInfo.getAppName();
                actionParam.download_url = loadAppDownloadUrl(adInfo);
                mReaperTracker.trackActionEvent(mContext, actionParam);
                break;
            case EVENT_VIDEO_CARD_CLICK:
                break;
            case EVENT_VIDEO_START_PLAY:
                break;
            case EVENT_VIDEO_PAUSE:
                break;
            case EVENT_VIDEO_CONTINUE:
                break;
            case EVENT_VIDEO_PLAY_COMPLETE:
                break;
            case EVENT_VIDEO_FULLSCREEN:
                break;
            case EVENT_VIDEO_EXIT:
                break;
            default:
                break;
        }
    }

    /**
     * handle action browse or download
     *
     * @param adInfo
     */
    private void handleClickAction(AdInfo adInfo) {
        int actionType = adInfo.getActionType();
        String adName = adInfo.getAdName();
        ISDKWrapper iSdkWrapper = mSdkWrapperSupport.get(adName);
        String actionUrl;
        switch (actionType) {
            case AdInfo.ActionType.APP_DOWNLOAD:
                if(iSdkWrapper.isDownloadOwn()) {
                    iSdkWrapper.setDownloadCallback(this);
                } else {
                    mThreadPoolUtils.execute(new RequestAppUrlTask(iSdkWrapper, adInfo));
                }
                break;
            case AdInfo.ActionType.BROWSER:
                if(!iSdkWrapper.isOpenWebOwn()) {
                    actionUrl = iSdkWrapper.requestWebUrl(adInfo);
                    if (!TextUtils.isEmpty(actionUrl)) {
                        try {
                            Class<?> reaperClass = Class.forName("com.fighter.loader.ReaperActivity");
                            Intent intent = new Intent(mContext, reaperClass);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("url", actionUrl);
                            mContext.startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            OpenUtils.openWebUrl(mContext, actionUrl);
                            e.printStackTrace();
                        }
                    }
                } else {
                }
                break;
            default:
                ReaperLog.i(TAG, " click action type is undefine");
                break;
        }
    }

    private String loadAppActionType(int actionEvent) {
        String actionType = null;
        switch (actionEvent) {
            case EVENT_APP_START_DOWNLOAD:
                actionType = TrackerEventType.APP_ACTION_TYPE_BEGIN;
                break;
            case EVENT_APP_DOWNLOAD_COMPLETE:
                actionType = TrackerEventType.APP_ACTION_TYPE_END;
                break;
            case EVENT_APP_DOWNLOAD_FAILED:
                actionType = TrackerEventType.APP_ACTION_TYPE_FAILED;
                break;
            case EVENT_APP_DOWNLOAD_CANCELED:
                actionType = TrackerEventType.APP_ACTION_TYPE_FAILED;
                break;
            case EVENT_APP_INSTALL:
                actionType = TrackerEventType.APP_ACTION_TYPE_INSTALL;
                break;
            case EVENT_APP_ACTIVE:
                actionType = TrackerEventType.APP_ACTIVE;
                break;
        }
        return actionType;
    }

    private String loadAppActReason(int actionEvent, String errMsg) {
        String actReason;
        switch (actionEvent) {
            case EVENT_APP_START_DOWNLOAD:
            case EVENT_APP_DOWNLOAD_COMPLETE:
            default:
                actReason = "";
                break;
            case EVENT_APP_DOWNLOAD_FAILED:
                actReason = errMsg;
                break;
            case EVENT_APP_DOWNLOAD_CANCELED:
                actReason = "down load app canceled by user";
                break;
        }
        return actReason;
    }

    private String loadAppDownloadUrl(AdInfo adInfo){
        String adName = adInfo.getAdName();
        String actionUrl;
        ISDKWrapper iSdkWrapper = mSdkWrapperSupport.get(adName);
        if(iSdkWrapper.isDownloadOwn()) {
            actionUrl = "no action url because this sdk download apk own";
        } else {
            actionUrl = iSdkWrapper.requestDownloadUrl(adInfo);
        }
        return actionUrl;
    }

    private String loadCoordinate(AdInfo adInfo) {
        Map<String, Object> eventParams = adInfo.getAdAllParams();
        int downX = -999;
        int downY = -999;
        int upX = -999;
        int upY = -999;
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
        return "downX:" + downX + " dowY:" + downY + " upX:" + upX + " upY:" + upY;
    }

    @Override
    public void onDownloadComplete(long reference, String fileName) {
        AdInfo adInfo = mDownloadApps.get(reference);
        if(adInfo == null)
            return;
        if(TextUtils.isEmpty(fileName)){
            trackActionEvent(EVENT_APP_DOWNLOAD_FAILED, adInfo, adInfo + " download app file name is null");
            return;
        }
        //download app success
        ReaperLog.i(TAG, reference + " on download complete " + fileName);
        File resultFile;
        File apkFile = new File(fileName);
        if(!apkFile.exists()) {
            return;
        }
        trackActionEvent(EVENT_APP_DOWNLOAD_COMPLETE, adInfo);
        registerInstallReceiver();
        String parent = apkFile.getParent();
        //handle the result file to apk file
        if(!fileName.endsWith(".apk")) {
            resultFile = new File(parent, reference+".apk");
            boolean rename = apkFile.renameTo(resultFile);
            ReaperLog.i(TAG, apkFile.getAbsolutePath() + " rename to " + resultFile.getAbsolutePath()+ " " + rename);
        } else {
            resultFile = apkFile;
        }

        //start install apk
        if(!resultFile.getName().endsWith(".apk")) return;
        PackageInfo packageInfo = getPackageInfo(resultFile);
        if(packageInfo == null) return;
        mInstallApps.put(packageInfo.packageName, System.currentTimeMillis());
        mInstallAds.put(packageInfo.packageName, adInfo);
        installApk(resultFile);
        //down complete should remove this adInfo
        mDownloadApps.remove(reference);
    }

    private void registerInstallReceiver() {
        if(mInstallReceiver == null)
            mInstallReceiver = new ApkInstallReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(mInstallReceiver, intentFilter);
    }

    /**
     * Install apk when receive download complete
     * @param apkFile
     */
    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private PackageInfo getPackageInfo(File apkFile) {
        String apkPath = apkFile.getAbsolutePath();
        if(TextUtils.isEmpty(apkPath)) return null;
        PackageManager packageManager = mContext.getPackageManager();
        return packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
    }

    @Override
    public void onDownloadFailed(long reference, int reason) {
        AdInfo adInfo = mDownloadApps.get(reference);
        if(adInfo == null) {
            ReaperLog.e(TAG, " here is a app download fail but adInfo is not in download map");
            return;
        }
        String errMsg;
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                errMsg = "DownloadManager.ERROR_CANNOT_RESUME";
                break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                errMsg = "DownloadManager.ERROR_DEVICE_NOT_FOUND";
                break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                errMsg = "DownloadManager.ERROR_FILE_ALREADY_EXISTS";
                break;
            case DownloadManager.ERROR_FILE_ERROR:
                errMsg = "DownloadManager.ERROR_FILE_ERROR";
                break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                errMsg = "DownloadManager.ERROR_HTTP_DATA_ERROR";
                break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                errMsg = "DownloadManager.ERROR_INSUFFICIENT_SPACE";
                break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                errMsg = "DownloadManager.ERROR_TOO_MANY_REDIRECTS";
                break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                errMsg = "DownloadManager.ERROR_UNHANDLED_HTTP_CODE";
                break;
            case DownloadManager.ERROR_UNKNOWN:
            default:
                errMsg = "DownloadManager.ERROR_UNKNOWN";
                break;
        }
        errMsg += " errCode:" + reason;
        trackActionEvent(EVENT_APP_DOWNLOAD_FAILED, adInfo, errMsg);
        //download fail should the adInfo
        mDownloadApps.remove(reference);
    }

    /**
     * this callback method is for ISDKWrapper handle download apk own（eg.AkAdSDKWrapper）
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
    @Override
    public void onDownloadEvent(AdInfo adInfo, int apkDownloadEvent) {
        if(apkDownloadEvent == EVENT_APP_DOWNLOAD_FAILED) {
            String errMsg = "download app own does not return fail reason";
            trackActionEvent(apkDownloadEvent, adInfo, errMsg);
        } else {
            trackActionEvent(apkDownloadEvent, adInfo, null);
        }
    }

    class ApkInstallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null)
                return;
            String action = intent.getAction();
            if(!TextUtils.equals(action, Intent.ACTION_PACKAGE_ADDED)) return;
            String packageName = intent.getData().getSchemeSpecificPart();
            if(TextUtils.isEmpty(packageName)
                    || !mInstallApps.containsKey(packageName)
                        || !mInstallAds.containsKey(packageName)) return;
            long start = mInstallApps.get(packageName);
            if(System.currentTimeMillis() - start < EFFECTIVE_TIME) {
                AdInfo adInfo = mInstallAds.get(packageName);
                trackActionEvent(EVENT_APP_INSTALL, adInfo);
                PackageManager packageManager = context.getPackageManager();
                Intent appIntent = packageManager.getLaunchIntentForPackage(packageName);
                context.startActivity(appIntent);
                trackActionEvent(EVENT_APP_ACTIVE, adInfo);
            }
            mInstallApps.remove(packageName);
            mInstallAds.remove(packageName);
        }
    }

    class RequestAppUrlTask implements Runnable {

        private ISDKWrapper sdkWrapper;
        private AdInfo adInfo;

        RequestAppUrlTask(ISDKWrapper sdkWrapper, AdInfo adInfo) {
            this.sdkWrapper = sdkWrapper;
            this.adInfo = adInfo;
        }

        @Override
        public void run() {
            String actionUrl = sdkWrapper.requestDownloadUrl(adInfo);
            if(TextUtils.isEmpty(actionUrl)) {
                String errMsg = adInfo + " download fail action is null";
                ReaperLog.i(TAG, errMsg);
                trackActionEvent(EVENT_APP_DOWNLOAD_FAILED, adInfo, errMsg);
            }
            long id = mAdFileManager.requestDownload(actionUrl, adInfo.getAppName(), null);
            ReaperLog.i(TAG, "start download app " + id);
            trackActionEvent(EVENT_APP_START_DOWNLOAD, adInfo, null);
            mDownloadApps.put(id, adInfo);
        }
    }

    /*************************************************end move from tracker to fix open webView so many times*****************************************************************/

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

    private void cleanBeforeCache(AdCacheInfo info) {
        if (info == null)
            return;
        String cachePath = info.getCachePath();
        if (cachePath != null) {
            File cacheFile = new File(cachePath);
            if (cacheFile.exists() && cacheFile.isFile()) {
                cacheFile.delete();
            }
        }
    }

    private void postAdRequestTask(String posId, Object callBack) {
        AdRequestRunner runner = new AdRequestRunner(posId, callBack);
        AdRequestNotify notify = new AdRequestNotify();
        AdRequestTask task = new AdRequestTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                runner, notify, posId, callBack);
        mWorkThread.postTaskInFront(task);
    }

    private void postAdRequestWrapperTask(String posId, Object callBack, boolean isCache,List<ReaperAdSense> list,
                                          PriorityTaskDaemon.TaskRunnable ownerRunner) {
        AdRequestWrapperRunner runner = new AdRequestWrapperRunner(posId, list);
//        AdRequestWrapperNotify notify = new AdRequestWrapperNotify();
        AdRequestWrapperTask task;
        if (ownerRunner == null) {
            task = new AdRequestWrapperTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                    runner, mAdRequestWrapperNotify, posId, callBack, isCache, list);
        } else {
            PriorityTaskDaemon.NotifyPriorityTask notifyPriorityTask = ownerRunner.createNewTask(
                    PriorityTaskDaemon.PriorityTask.PRI_FIRST, runner, mAdRequestWrapperNotify);
            task = new AdRequestWrapperTask(notifyPriorityTask, posId, callBack, isCache, list);
        }
        mWorkThread.postTaskInFront(task);
    }

    private void onRequestAd(Object receiver, Map<String, Object> params) {
        if (receiver == null) {
            return;
        }
        Method methodOnResponse = null;
        if (mMethodCall != null) {
            methodOnResponse = mMethodCall.get(METHOD_ON_RESPONSE);
        }
        if (methodOnResponse == null) {
            try {
                methodOnResponse = receiver.getClass().getDeclaredMethod(
                        METHOD_ON_RESPONSE, Map.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (methodOnResponse != null) {
                mMethodCall = new ArrayMap<>();
                mMethodCall.put(METHOD_ON_RESPONSE, methodOnResponse);
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

    private boolean updateConfig() {
        // 获取配置信息
        boolean fetchSucceed = true;

        fetchSucceed = ReaperConfigManager.fetchReaperConfigFromServer(mContext,
                mContext.getPackageName(), SALT, mAppKey, mAppId);

        if (!fetchSucceed) {
            ReaperLog.e(TAG, "Can not fetch reaper config from server");
        }

        return fetchSucceed;
    }

    private void updateWrapper(List<ReaperAdSense> reaperAdSenses) {
        if (reaperAdSenses == null) return;
        if (mSdkWrapperSupport == null)
            mSdkWrapperSupport = new HashMap<>();
        if (mSdkWrapperAdTypeSupport == null)
            mSdkWrapperAdTypeSupport = new HashMap<>();
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_BANNER, AdType.TYPE_BANNER);
//        mSdkWrapperAdTypeSupport.put(AdType.TYPE_APP_WALL, AdType.TYPE_APP_WALL);
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_FEED, AdType.TYPE_FEED);
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_FULL_SCREEN, AdType.TYPE_FULL_SCREEN);
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_NATIVE, AdType.TYPE_NATIVE);
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_VIDEO, AdType.TYPE_VIDEO);
        mSdkWrapperAdTypeSupport.put(AdType.TYPE_PLUG_IN, AdType.TYPE_PLUG_IN);

        if (mMethodCall == null)
            mMethodCall = new HashMap<>();
        for(ReaperAdSense adSense : reaperAdSenses) {
            String adSourceName = adSense.ads_name;
            if (!mSdkWrapperSupport.containsKey(adSourceName)) {
                switch (adSourceName) {
                    case SdkName.GUANG_DIAN_TONG:
                        ISDKWrapper tencentWrapper = new TencentSDKWrapper();
                        tencentWrapper.init(mContext, null);
                        mSdkWrapperSupport.put(SdkName.GUANG_DIAN_TONG, tencentWrapper);
                        break;
                    case SdkName.MIX_ADX:
                        ISDKWrapper mixAdxWrapper = new MixAdxSDKWrapper();
                        mixAdxWrapper.init(mContext, null);
                        mSdkWrapperSupport.put(SdkName.MIX_ADX, mixAdxWrapper);
                        break;
                    case SdkName.AKAD:
                        ISDKWrapper akAdWrapper = new AKAdSDKWrapper();
                        akAdWrapper.init(mContext, null);
                        mSdkWrapperSupport.put(SdkName.AKAD, akAdWrapper);
                        break;
                    default:
                        ReaperLog.e(TAG, "not match sdk wrapper");
                }
            }
        }
    }

    private AdResponse requestWrapperAdInner(List<ReaperAdSense> reaperAdSenses, int location, String advType, AdRequestWrapperTask task) {
        if (reaperAdSenses == null)
            return null;
        ReaperLog.i(TAG, "location is ：" + location);
        if (location > reaperAdSenses.size() -1) {
            return null;
        }
        ReaperAdSense sense = reaperAdSenses.get(location);
        ReaperLog.i(TAG, "location = " + location + ",list = " + reaperAdSenses.hashCode());
        String adsName = sense.ads_name;
        ISDKWrapper sdkWrapper = mSdkWrapperSupport.get(adsName);
        if (sdkWrapper == null) {
            ReaperLog.e(TAG, "Can not find " + adsName + "'s sdk implements, may need " +
                    "upgrade reaper jar, current version " + BumpVersion.value());
            return null;
        }

        String adType = null;
        if (mSdkWrapperAdTypeSupport.containsKey(advType)) {
            adType = mSdkWrapperAdTypeSupport.get(advType);
        } else {
            ReaperLog.e(TAG, "Can not find match ad type with type name " +
                    advType);
            return null;
        }

        AdRequest.Builder builder = new AdRequest.Builder()
                .adPosId(sense.getPosId())
                .adLocalAppId(sense.ads_appid)
                .adLocalPositionId(sense.ads_posid)
                .adType(adType)
                .adExpireTime(Long.parseLong(sense.expire_time))
                .adCount(1);

        if ("pixel".equalsIgnoreCase(sense.adv_size_type)) {
            String realSize = sense.adv_real_size;
            String[] size = realSize.split("\\*");
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
        } else if ("ratio".equalsIgnoreCase(sense.adv_size_type)) {
            ReaperLog.e(TAG, "[TEST] Not support adv size type " +
                        sense.adv_size_type);
            return null;
        } else {
            ReaperLog.e("Not support adv size type " +
                        sense.adv_size_type);
            return null;
        }
        AdResponse adResponse = null;
        if (sdkWrapper.isRequestAdSupportSync()) {
            adResponse = sdkWrapper.requestAdSync(builder.create());
        } else {
            sdkWrapper.requestAdAsync(builder.create(), task);
        }

        return adResponse;
    }

    /**
     * The method for caching file into disk storage
     *
     * @param adInfo
     * @return
     */
    private void downloadAdResourceFile(AdInfo adInfo) {
        if (adInfo == null)
            return;
        String imageUrl = adInfo.getImgUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        File imageFile = null;
        try {
            imageFile = cacheAdFile(imageUrl);
        } catch (Exception e) {
            EventDownLoadParam param = new EventDownLoadParam();
            param.ad_info = adInfo;
            param.ad_num = 1;
            param.ad_appid = Integer.parseInt(mAppId);
            param.app_pkg = mContext.getPackageName();
            param.reason = "OkHttpDownloader exception in sdk " + e.toString();
            mReaperTracker.trackDownloadEvent(mContext, param);
            e.printStackTrace();
        }
        if (imageFile != null && imageFile.exists()) {
            adInfo.setImgFile(imageFile.getAbsolutePath());
        }
    }

    private void cacheAdInfo(AdInfo adInfo) {
        if (adInfo == null)
            return;
        AdCacheInfo info = new AdCacheInfo();
        info.setAdSource(adInfo.getAdName());
        if (adInfo.canCache()) {
            info.setCache(AdInfo.convertToString(adInfo));
        } else {
            info.setCache(adInfo);
        }
        // the config expire time is second
        info.setExpireTime(String.valueOf(adInfo.getExpireTime() * 1000));
        info.setUuid(adInfo.getUUID());
        info.setAdCacheId(adInfo.getAdPosId());
        ReaperLog.i(TAG, "cache ad info: " + adInfo);
        try {
            cacheAdInfo(adInfo.getAdPosId(), info);
            collateAdCache(adInfo.getAdPosId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

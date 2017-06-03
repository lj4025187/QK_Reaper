package com.fighter.cache;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.ad.AdInfo;
import com.fighter.ad.AdType;
import com.fighter.ad.SdkName;
import com.fighter.cache.downloader.AdCacheFileDownloadManager;
import com.fighter.common.PriorityTaskDaemon;
import com.fighter.common.utils.OpenUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;
import com.fighter.reaper.BumpVersion;
import com.fighter.tracker.EventClickParam;
import com.fighter.tracker.EventDisPlayParam;
import com.fighter.tracker.EventDownLoadParam;
import com.fighter.tracker.Tracker;
import com.fighter.wrapper.AKAdSDKWrapper;
import com.fighter.wrapper.AdRequest;
import com.fighter.wrapper.AdResponse;
import com.fighter.wrapper.AdResponseListener;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import static com.fighter.ad.AdEvent.EVENT_VIEW;

/**
 * the class to manager the ad cache.
 * init() fill the all the posId cache.
 * requestAdCache() firstly request ad from cache, if cache is null,
 * request two ads, one for return to client, another for cache.
 * <p>
 * Created by lichen on 17-5-17.
 */

public class AdCacheManager implements AdCacheFileDownloadManager.DownloadCallback {
    private static final String TAG = AdCacheManager.class.getSimpleName();

    private static final String SALT = "cf447fe3adac00476ee9244fd30fba74";
    private static final String METHOD_ON_RESPONSE = "onResponse";

    private static final int CACHE_MAX = 5;
    private static AdCacheManager mAdCacheManager = new AdCacheManager();
    private File mCacheDir;

    private Map<String, ISDKWrapper> mSdkWrapperSupport;
    private Map<String, String> mSdkWrapperAdTypeSupport;
    private Map<String, Method> mMethodCall;

    private List<ReaperAdSense> mAdSenseList;
    private ReaperAdvPos mReaperAdvPos;
    private PriorityTaskDaemon.NotifyPriorityTask mAdRequestWrapperAsnc;

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

    private class InitCacheRunnable extends PriorityTaskDaemon.TaskRunnable {

        private Context context;

        public InitCacheRunnable(Context context) {
            this.context = context;
        }

        @Override
        public Object doSomething() {
            boolean initSuccess = initCache(this.context);
            ReaperLog.i(TAG, "InitCacheRunnable do something init " + initSuccess);
//            String[] posIds = getAllPosId(context);
//            for (String posId : posIds) {
//                postAdRequestWrapperTask(posId, null, true, InitCacheRunnable.this);
//            }
            return initSuccess;
        }
    }
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

    private class TrackerRunnable extends PriorityTaskDaemon.TaskRunnable {

        private final String TAG = TrackerRunnable.class.getSimpleName();
        private Context context;
        private Tracker tracker;
        private int actionEvent;
        private AdInfo adInfo;
        private ISDKWrapper wrapper;

        public TrackerRunnable(Context context, Tracker tracker, int actionEvent, AdInfo adInfo, ISDKWrapper wrapper) {
            this.context = context;
            this.tracker = tracker;
            this.actionEvent = actionEvent;
            this.adInfo = adInfo;
            this.wrapper = wrapper;
        }

        @Override
        public Object doSomething() {
            if (context == null) {
                ReaperLog.e(TAG, "tracker runnable init context is null");
                return null;
            }
            //open web url or download app
            handleTouchEvent(actionEvent, adInfo);

            //ISdkWrapper onEvent
            wrapper.onEvent(actionEvent, adInfo);
            String act_type = String.valueOf(actionEvent);
            ReaperLog.i(TAG, "tracker runnable track action type " + act_type);
            //Tracker onEvent
            trackerEvent(actionEvent, adInfo);
            return adInfo;
        }

        private void trackerEvent(int actionEvent, AdInfo adInfo) {
            switch (actionEvent) {
                case EVENT_VIEW:
                    EventDisPlayParam disPlayParam = new EventDisPlayParam();
                    disPlayParam.ad_num = 1;
                    disPlayParam.ad_appid = 12222;/*this value should rewrite*/
                    disPlayParam.ad_posid = Integer.valueOf(adInfo.getAdPosId());
                    disPlayParam.ad_source = adInfo.getAdName();
                    disPlayParam.ad_type = adInfo.getAdType();
                    disPlayParam.app_pkg = context.getPackageName();
                    disPlayParam.result = "ok";
                    disPlayParam.reason = "";
                    ReaperLog.i(TAG, "EventDisPlayParam = " + disPlayParam);
                    tracker.trackDisplayEvent(context, disPlayParam);
                    break;
                case EVENT_CLICK:
                    EventClickParam clickParam = new EventClickParam();
                    clickParam.ad_num = 1;
                    clickParam.ad_appid = 12222;/*this value should rewrite*/
                    clickParam.ad_posid = Integer.valueOf(adInfo.getAdPosId());
                    clickParam.ad_source = adInfo.getAdName();
                    clickParam.ad_type = adInfo.getAdType();
                    clickParam.app_pkg = context.getPackageName();
                    clickParam.click_pos = "(100*100)";/*this value should rewrite*/
                    ReaperLog.i(TAG, "EventClickParam = " + clickParam);
                    tracker.trackClickEvent(context, clickParam);
                    break;
                case EVENT_CLOSE:
                    break;
                case EVENT_APP_START_DOWNLOAD:
                    break;
                case EVENT_APP_DOWNLOAD_COMPLETE:
                    break;
                case EVENT_APP_DOWNLOAD_FAILED:
                    break;
                case EVENT_APP_DOWNLOAD_CANCELED:
                    break;
                case EVENT_APP_INSTALL:
                    break;
                case EVENT_APP_ACTIVE:
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

    }
    /****************************************************Tracker Task end**************************************************************************/

    /****************************************************AdRequestWrapper Task start**************************************************************************/
    /**
     * request wrapper task for callback cache and notify user
     */
    private class AdRequestWrapperTask extends PriorityTaskDaemon.NotifyPriorityTask implements AdResponseListener{
        private String mPosId;
        private Object mCallBack;
        private boolean mCache;

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
                                    Object mCallBack, boolean mCache) {
            super(priority, runnable, notify);
            this.mPosId = mPosId;
            this.mCallBack = mCallBack;
            this.mCache = mCache;
        }

        public AdRequestWrapperTask(PriorityTaskDaemon.NotifyPriorityTask task, String mPosId,
                                    Object mCallBack, boolean mCache) {
            super(task);
            this.mPosId = mPosId;
            this.mCallBack = mCallBack;
            this.mCache = mCache;
        }

        @Override
        public void onAdResponse(AdResponse adResponse) {
            AdRequestWrapperAsyncRunner runner = new AdRequestWrapperAsyncRunner(adResponse);
            this.setRunnable(runner);
            mWorkThread.postTaskInFront(this);
        }
    }

    private class AdRequestWrapperAsyncRunner extends PriorityTaskDaemon.TaskRunnable {
        private AdResponse mAdResponse;

        public AdRequestWrapperAsyncRunner(AdResponse mAdResponse) {
            this.mAdResponse = mAdResponse;
        }

        @Override
        public Object doSomething() {
            if (mAdResponse.isSucceed()) {
                return mAdResponse.getAdInfo();
            }
            while(mAdResponse != null && !mAdResponse.isSucceed()) {
                mAdResponse = requestWrapperAdInner(mAdSenseList, mReaperAdvPos.adv_type, (AdRequestWrapperTask) getTask());
            }
            if (mAdResponse != null && mAdResponse.isSucceed()) {
                return mAdResponse.getAdInfo();
            }
            return null;
        }
    }

    private class AdRequestWrapperRunner extends PriorityTaskDaemon.TaskRunnable {
        private Context mContext;
        private String mPosId;

        public AdRequestWrapperRunner(Context mContext, String mPosId) {
            this.mContext = mContext;
            this.mPosId = mPosId;
        }

        @Override
        public Object doSomething() {
            AdInfo adInfo = null;
            AdResponse adResponse;
//            if(!updateConfig()) {
//                return null;
//            }
//            updateConfig();
            mAdSenseList = getWrapperConfig(mPosId);
            mReaperAdvPos = ReaperConfigManager.getReaperAdvPos(mContext, mPosId);
            if (mAdSenseList != null) {
                updateWrapper(mAdSenseList);
                do {
                    adResponse = requestWrapperAdInner(mAdSenseList, mReaperAdvPos.adv_type, (AdRequestWrapperTask) getTask());
                } while (adResponse != null && !adResponse.isSucceed());

                if (adResponse != null && adResponse.isSucceed())
                    adInfo = adResponse.getAdInfo();
            }
            downloadAdResourceFile(adInfo);
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
                if (isCache) {
                    cacheAdInfo(adInfo);
                } else {
                    onRequestAdSucceed(callBack, adInfo);
                }
            }

        }
    }

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
        public AdRequestRunner(String posId, Object callBack) {
            mPosId = posId;
            mCallBack = callBack;
        }

        @Override
        public Object doSomething() {
            // 1.post a task to pull ad for cache
            postAdRequestWrapperTask(mPosId, null, true, AdRequestRunner.this);
            // 2. if cache is full, back cache ad info
            Object info = getCacheAdInfo(mPosId);
            AdCacheInfo adCacheInfo;
            if (info != null && info instanceof AdCacheInfo) {
                adCacheInfo = (AdCacheInfo)info;
                AdInfo adInfo = AdInfo.convertFromString(adCacheInfo.getCache());
                if (isAdCacheTimeout(adCacheInfo)) {
                    EventDownLoadParam param = new EventDownLoadParam();
                    param.ad_num = 1;
                    param.ad_appid = 12222;/*this value should rewrite*/
                    param.ad_posid = Integer.valueOf(adInfo.getAdPosId());
                    param.ad_source = adInfo.getAdName();
                    param.ad_type = adInfo.getAdType();
                    param.app_pkg = mContext.getPackageName();
                    param.reason = "timeout";
                    ReaperLog.i(TAG, "EventDownLoadParam = " + param);
                    mReaperTracker.trackDownloadEvent(mContext, param);
                } else {
                    setCacheUsed(adCacheInfo);
                    return adInfo;
                }
            }
            // 3. if cache is empty, post a task call wrapper get ad
            postAdRequestWrapperTask(mPosId, mCallBack, false, AdRequestRunner.this);
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
        InitCacheRunnable initCacheRunnable = new InitCacheRunnable(context);
        InitCacheTask initCacheTask = new InitCacheTask(
                PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                initCacheRunnable,
                new PriorityTaskDaemon.TaskNotify() {
                    @Override
                    public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
                        boolean init = false;
                        if (result instanceof Boolean)
                            init = (boolean) result;
                        ReaperLog.i(TAG, " init cache task on result method is called and init " + init);
                    }
                });
        mWorkThread.postTaskInFront(initCacheTask);
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
                if (adCacheInfo.isCacheDisPlayed() || isAdCacheTimeout(adInfo)) {
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
     * fill ad cache pool when cache init fill it.
     */
    private void fillAdCachePool(String[] posIds) {
        for (String posId : posIds) {
            postAdRequestWrapperTask(posId, null, true, null);
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
    public void requestAdCache(String cacheId, Object callBack) {
        mCacheId = cacheId;
        mCallBack = callBack;
        postAdRequestTask(mCacheId, mCallBack);
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

    private void updateDiskCache(AdCacheInfo adCacheInfo) {
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

    private void setCacheUsed(AdCacheInfo adCacheInfo) {
        if (adCacheInfo == null)
            return;
        adCacheInfo.setCacheState(AdCacheInfo.CACHE_BACK_TO_USER);
        updateDiskCache(adCacheInfo);
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
            //updateDiskCache(adCacheInfo);
            cacheObjects.remove(adCacheInfo.getUuid());
            cleanBeforeCache(adCacheInfo);
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

        File cacheIdDir = new File(mCacheDir, cacheId);
        if (!cacheIdDir.exists()) {
            cacheIdDir.mkdir();
        }
        File adInfoFile = new File(cacheIdDir, cacheFileId);
        if (!adInfoFile.exists()) {
            adInfoFile.createNewFile();
        }
        adCacheInfo.setCachePath(adInfoFile.getAbsolutePath());
        adCacheInfo.setCacheState(AdCacheInfo.CACHE_IS_GOOD);
        adCacheInfoObjects.put(adCacheInfo.getUuid(), adCacheInfo);
        mAdCache.put(cacheId, adCacheInfoObjects);
        FileOutputStream fileOutputStream = new FileOutputStream(adInfoFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(adCacheInfo);
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
     *
     * @param imageUrl
     * @return cache file instance
     */
    private File cacheAdFile(String imageUrl) {
        return mAdFileManager.cacheAdFile(imageUrl);
    }

    @Override
    public void onDownloadComplete(long reference, String fileName) {
        //download app success
        ReaperLog.i(TAG, reference + " on download complete " + fileName);
        File resultFile;
        File apkFile = new File(fileName);
        if(!apkFile.exists())
            return;

        String parent = apkFile.getParent();
        ReaperLog.i(TAG, " parent string is " + parent);

        //handle the result file to apk file
        if(!fileName.endsWith(".apk")) {
            resultFile = new File(parent, reference+".apk");
            boolean rename = apkFile.renameTo(resultFile);
            ReaperLog.i(TAG, resultFile.getAbsolutePath() + " rename " + rename);
        } else {
            resultFile = apkFile;
        }

        //start install apk
        if(resultFile.getName().endsWith(".apk")) {
            installApk(resultFile);
        }
    }

    /**
     * Install apk when receive download complete
     * @param apkFile
     */
    private  void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
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
                File cacheFile = new File(cacheInfoInBottom.getCachePath());
                if (cacheFile.exists()) {
                    cacheFile.delete();
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
     *
     * @param tracker
     */
    private void postTrackerTask(Context context, Tracker tracker, int actionEvent, final AdInfo adInfo, ISDKWrapper wrapper) {
        TrackerRunnable trackerRunnable = new TrackerRunnable(context, tracker, actionEvent, adInfo, wrapper);
        TrackerTask trackerTask = new TrackerTask(
                PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                trackerRunnable,
                new PriorityTaskDaemon.TaskNotify() {
                    @Override
                    public void onResult(PriorityTaskDaemon.NotifyPriorityTask task, Object result, PriorityTaskDaemon.TaskTiming timing) {
                        ReaperLog.i(TAG, "tracker task onResult method is called");
                        if (result.equals(adInfo)) {
                            ReaperLog.i(TAG, "tracker ad info " + adInfo.getAdPosId() + "tracker event has handled");
                        }
                    }
                }
        );
        mWorkThread.postTask(trackerTask);
    }

    /**
     * This method is support for ReaperApi and use Tracker task to record event
     *
     * @param adEvent
     * @param adInfo
     */
    public void onEvent(int adEvent, AdInfo adInfo) {
        if (adEvent == EVENT_VIEW) {
            setCacheDisplayed(adInfo);
        }
        ISDKWrapper wrapper = null;
        switch (adInfo.getAdName()) {
            case SdkName.GUANG_DIAN_TONG: {
                wrapper = mSdkWrapperSupport.get(SdkName.GUANG_DIAN_TONG);
                break;
            }
            case SdkName.MIX_ADX: {
                wrapper = mSdkWrapperSupport.get(SdkName.MIX_ADX);
                break;
            }
            case SdkName.AKAD: {
                wrapper = mSdkWrapperSupport.get(SdkName.AKAD);
                break;
            }
        }
        if(wrapper != null)
            postTrackerTask(mContext, mReaperTracker, adEvent, adInfo, wrapper);
    }

    /**
     * handle touch event such as click
     *
     * @param adEvent
     * @param adInfo
     */
    private void handleTouchEvent(int adEvent, AdInfo adInfo) {
        switch (adEvent) {
            case EVENT_CLICK:
                handleAction(adInfo);
                break;
        }
    }

    /**
     * handle action browse or download
     *
     * @param adInfo
     */
    private void handleAction(AdInfo adInfo) {
        int actionType = adInfo.getActionType();
        String adName = adInfo.getAdName();
        ISDKWrapper iSdkWrapper = mSdkWrapperSupport.get(adName);
        String actionUrl = null;
        switch (actionType) {
            case AdInfo.ActionType.APP_DOWNLOAD:
                if(!iSdkWrapper.isDownloadOwn()) {
                    actionUrl = iSdkWrapper.requestDownloadUrl(adInfo);
                    long id = mAdFileManager.requestDownload(actionUrl, null, null);
                    ReaperLog.i(TAG, "start download app " + id);
                }
                break;
            case AdInfo.ActionType.BROWSER:
                if(!iSdkWrapper.isOpenWebOwn()) {
                    actionUrl = iSdkWrapper.requestWebUrl(adInfo);
                    if (!TextUtils.isEmpty(actionUrl))
                        OpenUtils.openWebUrl(mContext, actionUrl);
                }
                break;
            default:
                ReaperLog.i(TAG, " click action type is undefine");
                break;
        }
        ReaperLog.i(TAG, actionType + " url " + actionUrl);
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

    private void cleanBeforeCache(AdCacheInfo info) {
        if (info == null)
            return;
        File cacheFile = new File(info.getCachePath());
        if (cacheFile.exists() && cacheFile.isFile()) {
            cacheFile.delete();
        }
    }

    private void postAdRequestTask(String posId, Object callBack) {
        AdRequestRunner runner = new AdRequestRunner(posId, callBack);
        AdRequestNotify notify = new AdRequestNotify();
        AdRequestTask task = new AdRequestTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                runner, notify, posId, callBack);
        mWorkThread.postTaskInFront(task);
    }

    private void postAdRequestWrapperTask(String posId, Object callBack, boolean isCache,
                                          PriorityTaskDaemon.TaskRunnable ownerRunner) {
        AdRequestWrapperRunner runner = new AdRequestWrapperRunner(mContext, posId);
        AdRequestWrapperNotify notify = new AdRequestWrapperNotify();
        AdRequestWrapperTask task;
        if (ownerRunner == null) {
            task = new AdRequestWrapperTask(PriorityTaskDaemon.PriorityTask.PRI_FIRST,
                    runner, notify, posId, callBack, isCache);
        } else {
            PriorityTaskDaemon.NotifyPriorityTask notifyPriorityTask = ownerRunner.createNewTask(
                    PriorityTaskDaemon.PriorityTask.PRI_FIRST, runner, notify);
            task = new AdRequestWrapperTask(notifyPriorityTask, posId, callBack, isCache);
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
//            onRequestAdError(mCallback, "Can not fetch reaper config from server");
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
//        mSdkWrapperAdTypeSupport.put(AdType.TYPE_NATIVE_VIDEO, AdType.TYPE_NATIVE_VIDEO);
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

    private List<ReaperAdSense> getWrapperConfig(String posId) {
        ReaperAdvPos reaperAdvPos =
                ReaperConfigManager.getReaperAdvPos(mContext, posId);
        if (reaperAdvPos == null) {
            ReaperLog.e(TAG, "Can not find config info with ad position id [" + posId + "]");
            return null;
        }

        List<ReaperAdSense> reaperAdSenses = reaperAdvPos.getAdSenseList();

        if (reaperAdSenses == null) {
            ReaperAdSense sense1 = new ReaperAdSense();
            sense1.ads_name = "jx";
            sense1.expire_time = "1800";
            sense1.priority = "10";
            sense1.ads_appid = "100001";
            sense1.ads_posid = "10001";
            sense1.ads_app_key = "adbsjmemsfm";
            sense1.max_adv_num = "10";
            sense1.adv_size_type = "pixel";
            sense1.adv_real_size = "640*100";
            reaperAdvPos.addAdSense(sense1);
            ReaperAdSense sense2 = new ReaperAdSense();
            sense2.ads_name = "gdt";
            sense2.expire_time = "1800";
            sense2.priority = "20";
            sense2.ads_appid = "1104241296";
            sense2.ads_posid = "6050305154328807";
            sense2.ads_app_key = "adbsjmemsfm";
            sense2.max_adv_num = "10";
            sense2.adv_size_type = "pixel";
            sense2.adv_real_size = "640*100";
            reaperAdvPos.addAdSense(sense2);
            reaperAdSenses = reaperAdvPos.getAdSenseList();
        }
        // query reaper by posId get the best reaperAdSense
//        if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
//            ReaperAdSense adSense = ReaperConfigManager.getReaperAdSens(mContext, posId);
//            List<ReaperAdSense> adSenses = new ArrayList<>();
//            adSenses.add(adSense);
//            reaperAdSenses = adSenses;
//        }

        if (reaperAdSenses == null || reaperAdSenses.size() == 0) {
            ReaperLog.e(TAG, "Config get 0 ad sense with ad position id [" + posId + "]");
            return null;
        }
        
        switch (reaperAdvPos.adv_exposure) {
            case "first":
                Collections.sort(reaperAdSenses);
                break;
            case "loop":
                //TODO
                break;
            case "weight":
                // TODO
                break;
            default:
        }

        return reaperAdSenses;
    }

    private AdResponse requestWrapperAdInner(List<ReaperAdSense> reaperAdSenses, String advType, AdRequestWrapperTask task) {
        if (reaperAdSenses == null)
            return null;
        Iterator<ReaperAdSense> iterator = reaperAdSenses.iterator();
        ReaperAdSense sense = iterator.next();
        iterator.remove();
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
        File imageFile = cacheAdFile(imageUrl);
        if (imageFile != null && imageFile.exists()) {
            adInfo.setImgFile(imageFile.getAbsolutePath());
        }
    }

    private void cacheAdInfo(AdInfo adInfo) {
        if (adInfo == null || !adInfo.canCache())
            return;
        AdCacheInfo info = new AdCacheInfo();
        info.setAdSource(adInfo.getAdName());
        info.setCache(AdInfo.convertToString(adInfo));
        // the config expire time is second
        info.setExpireTime(String.valueOf(adInfo.getExpireTime() * 1000));
        info.setUuid(adInfo.getUUID());
        info.setAdCacheId(adInfo.getAdPosId());
        try {
            cacheAdInfo(adInfo.getAdPosId(), info);
            collateAdCache(adInfo.getAdPosId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

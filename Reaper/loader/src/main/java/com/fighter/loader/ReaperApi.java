package com.fighter.loader;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.fighter.utils.LoaderLog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApi {

    private static final java.lang.String TAG = ReaperApi.class.getSimpleName();

    //Instance of ReaperApi in reaper.rr or Reaper.apk
    private Object mInstance;
    private ReaperVersionManager mVersionManager;
    private Map<String, Method> mMethodCache;

    // ----------------------------------------------------

    public ReaperApi(Object instance, String version) {
        LoaderLog.e(TAG, "mInstance : " + instance);
        mInstance = instance;
        mMethodCache = new ArrayMap<>();
    }

    public boolean isValid() {
        return mInstance != null;
    }

    public Object requestSplashAds(String name, int time) {
        try {
            Method requestSplashAdsMethod =
                    mInstance.getClass().getDeclaredMethod("requestSplashAds", String.class, int.class);
            if (requestSplashAdsMethod == null)
                return null;
            requestSplashAdsMethod.setAccessible(true);
            return requestSplashAdsMethod.invoke(mInstance, name, time);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------

    public void init(Context appContext, String appId,
                     String appKey, Map<String, Object> extras) {
        Map<String, Object> params = new ArrayMap<>();

        putParam(params, "appContext", appContext.getApplicationContext());
        putParam(params, "appId", appId);
        putParam(params, "appKey", appKey);
        putParam(params, extras);
        invokeReaperApi("init", params);
    }

    @SuppressWarnings("unchecked")
    public AdRequester getAdRequester(String adPositionId, AdRequestCallback adRequestCallback,
                                      Map<String, Object> extras) {
        Map<String, Object> params = new ArrayMap<>();
        putParam(params, "adPositionId", adPositionId);
        putParam(params, "adRequestCallback", new AdResponse(adRequestCallback));
        putParam(params, extras);

        AdRequester adRequester = new AdRequester();
        adRequester.mParams = params;
        return adRequester;
    }

    // ----------------------------------------------------

    private Object invokeReaperApi(String methodName, Map<String, Object> params) {
        try {
            Method method = mMethodCache.get(methodName);

            if (method == null) {
                method = mInstance.getClass().getDeclaredMethod(methodName, Map.class);
                if (method != null) {
                    mMethodCache.put(methodName, method);
                }
            }

            if (method != null) {
                method.setAccessible(true);
                return method.invoke(mInstance, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void putParam(Map<String, Object> params, String key, Object value) {
        if (params != null && !TextUtils.isEmpty(key) && value != null) {
            params.put(key, value);
        }
    }

    private void putParam(Map<String, Object> params, Map<String, Object> extraParams) {
        if (params != null && extraParams != null) {
            params.putAll(extraParams);
        }
    }

    // ----------------------------------------------------
    public interface AdRequestCallback {
        public void onSuccess(List<AdInfo> ads);

        public void onFailed(String errMsg);
    }

    public class AdRequester {
        Map<String, Object> mParams;

        public void requestAd() {
            invokeReaperApi("requestAd", mParams);
        }
    }

    public class AdInfo {
        Map<String, Object> mParams;

        public void onEvent(int event, Map<String, Object> extras) {

        }

        public int getContentType() {
            Object o = mParams.get("contentType");
            return o == null ? 0 : (int) o;
        }

        public int getActionType() {
            Object o = mParams.get("actionType");
            return o == null ? 0 : (int) o;
        }

        public String getImgUrl() {
            return (String) mParams.get("imgUrl");
        }

        public File getImgFile() {
            return (File) mParams.get("imgFile");
        }

        public String getVideoUrl() {
            return (String) mParams.get("videoUrl");
        }

        public String getTitle() {
            return (String) mParams.get("title");
        }

        public String getDesc() {
            return (String) mParams.get("desc");
        }

        public String getAppIconUrl() {
            return (String) mParams.get("appIconUrl");
        }

        public String getAppName() {
            return (String) mParams.get("appName");
        }

        public String getAppPackageName() {
            return (String) mParams.get("appPackageName");
        }

        public Object getExtra(String key) {
            return mParams.get(key);
        }

        @Override
        public String toString() {
            return "AdInfo{" +
                    "mContentType=" + getContentType() +
                    ", mActionType=" + getActionType() +
                    ", mImgUrl='" + getImgUrl() + '\'' +
                    ", mImgFile=" + getImgFile() +
                    ", mVideoUrl='" + getVideoUrl() + '\'' +
                    ", mTitle='" + getTitle() + '\'' +
                    ", mDesc='" + getDesc() + '\'' +
                    ", mAppIconUrl='" + getAppIconUrl() + '\'' +
                    ", mAppName='" + getAppName() + '\'' +
                    ", mAppPackageName='" + getAppPackageName() + '\'' +
                    '}';
        }
    }

    // ----------------------------------------------------

    private class AdResponse {
        private AdRequestCallback mAdRequestCallback;

        public AdResponse(AdRequestCallback adRequestCallback) {
            mAdRequestCallback = adRequestCallback;
        }

        @SuppressWarnings("unchecked")
        public void onResponse(Map<String, Object> params) {
            if (mAdRequestCallback == null) {
                return;
            }

            Object objIsSucceed = params.get("isSucceed");
            boolean isSucceed = objIsSucceed != null && (boolean) objIsSucceed;
            String errMsg = (String) params.get("errMsg");
            if (!isSucceed) {
                mAdRequestCallback.onFailed(errMsg);
                return;
            }
            List<Map<String, Object>> oriAdInfos = (List) params.get("adInfosMap");
            if (oriAdInfos == null || oriAdInfos.size() == 0) {
                mAdRequestCallback.onFailed("Requester returns no ads");
                return;
            }
            List<AdInfo> adInfos = new ArrayList<>(oriAdInfos.size());
            for (Map<String, Object> m : oriAdInfos) {
                AdInfo adInfo = new AdInfo();
                adInfo.mParams = m;
                adInfos.add(adInfo);
            }
            mAdRequestCallback.onSuccess(adInfos);
        }
    }
}

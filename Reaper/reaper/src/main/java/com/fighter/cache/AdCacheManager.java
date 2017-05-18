package com.fighter.cache;

import android.content.Context;

import com.fighter.common.utils.EncryptUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * the class to manager the ad cache.
 *
 * Created by lichen on 17-5-17.
 */

public class AdCacheManager {
    private static final String TAG = AdCacheManager.class.getSimpleName();

    private File mCacheDir;

    private static HashMap<String, String> mAdInfoCache = new HashMap<>();
    private static HashMap<String, String> mAdResourceCache = new HashMap<>();

    public AdCacheManager(Context context) {
        initCacheEnvironment(context);
    }

    private void initCacheEnvironment(Context context) {
        File cacheDir = context.getCacheDir();
        File adCacheDir = new File(cacheDir, "ac");
        if (!adCacheDir.exists())
            adCacheDir.mkdir();
        mCacheDir = adCacheDir;
    }

    /**
     * the method is used to cache ad information to sdcard.
     *
     * @param cacheId the ad unique id
     * @param adInfo the object of ad information
     * @throws IOException it maybe throw IOException
     */
    public void cacheAdInfo(String cacheId, Object adInfo) throws IOException{
        File cacheIdDir = new File(mCacheDir, cacheId);
        if (!cacheIdDir.exists()) {
            cacheIdDir.mkdir();
        }
        File adInfoFile = new File(cacheIdDir, EncryptUtils.encryptMD5ToString("ad_info").toLowerCase());
        if (!adInfoFile.exists()) {
            adInfoFile.createNewFile();
        }
        mAdInfoCache.put(cacheId, adInfoFile.getAbsolutePath());
        FileOutputStream fileOutputStream = new FileOutputStream(adInfoFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(adInfo);
        objectOutputStream.close();
        fileOutputStream.close();
    }

    /**
     * the method is used to cache ad resources
     *
     * @param cacheId the ad unique id
     * @param adResource the object ad resources
     * @throws IOException it maybe throw IOException
     */
    public void cacheAdResource(String cacheId, Object adResource) throws IOException {
        File cacheIdDir = new File(mCacheDir, cacheId);
        if (!cacheIdDir.exists()) {
            cacheIdDir.mkdir();
        }
        File adResourceFile = new File(cacheIdDir, EncryptUtils.encryptMD5ToString("ad_resource").toLowerCase());
        if (!adResourceFile.exists()) {
            adResourceFile.createNewFile();
        }
        mAdResourceCache.put(cacheId, adResourceFile.getAbsolutePath());
        // TODO : write cache ad resource
    }

    /**
     * the method is used to get the ad cache informaton from sdcard.
     *
     * @param cacheId the ad unique id..
     * @return the object about ad information
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object getCacheAdInfo(String cacheId) throws IOException, ClassNotFoundException{
        String cacheAdInfoPath = mAdInfoCache.get(cacheId);
        File adInfoFile;
        Object adInfo = null;
        if (cacheAdInfoPath != null) {
            adInfoFile = new File(cacheAdInfoPath);
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
}

package com.fighter.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;

import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LiuJia on 2017/5/22.
 */
public class AdFileCacheManager {

    private static final String TAG = AdFileCacheManager.class.getSimpleName();
    private static final long CACHE_FILE_MAX_SIZE = 4 * 1024 * 1024;
    private ExecutorService mExecutor;
    private static LruCache<String, Bitmap> mCache;
    private AdFileCacheManager mInstance;
    private AdFileCacheUtil mAdFileCacheUtil;

    /**
     * AdFileCacheManager instance method
     *
     * @param context
     * @return
     */
    public AdFileCacheManager newInstance(Context context) {
        if (mInstance == null)
            mInstance = new AdFileCacheManager(context);
        return mInstance;
    }

    /**
     * AdFileCacheManager constructor method
     *
     * @param context
     */
    private AdFileCacheManager(Context context) {
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mAdFileCacheUtil = AdFileCacheUtil.getInstance(context, CACHE_FILE_MAX_SIZE);
        mExecutor = Executors.newFixedThreadPool(5);
    }

    /**
     * this method load image by url
     *
     * @param url
     * @param listener
     */
    public void loadImage(final String url, final AdLoadListener listener) {

        final String key = url.replaceAll("[\\W]", "");

        if (readFromCache(key) != null) {

            listener.loadImageSuccess(readFromCache(key));

        } else {

            final Bitmap sdBitmap = mAdFileCacheUtil.readBitmapCache(key);

            if (sdBitmap != null) {
                saveToCache(key, sdBitmap);
                listener.loadImageSuccess(sdBitmap);
            } else {
                final Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        listener.loadImageSuccess((Bitmap) msg.obj);
                    }
                };
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        URL bitmapUrl = null;
                        try {
                            bitmapUrl = new URL(url);
                            InputStream inputStream = null;
                            inputStream = bitmapUrl.openStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mAdFileCacheUtil.saveBitmapCache(key, bitmap);
                            saveToCache(key, bitmap);
                            Message msg = handler.obtainMessage();
                            msg.obj = bitmap;
                            handler.sendMessage(msg);
                        } catch (IOException e) {
                            ReaperLog.e(TAG, " load image " + e.toString());
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * this method load video by url
     */
    private void loadFile(final String url, final AdLoadListener listener) {
        final String key = url.replaceAll("[\\W]", "");
        String filePath = mAdFileCacheUtil.readVideoPathCache(key, url);
        if (TextUtils.isEmpty(filePath)) {
            final Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    listener.loadFileSuccess((String) msg.obj);
                }
            };
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    String path = mAdFileCacheUtil.saveCustomFile(key, url);
                    if (!TextUtils.isEmpty(path)) {
                        Message msg = handler.obtainMessage();
                        msg.obj = path;
                    }
                }
            });
        } else {
            listener.loadFileSuccess(filePath);
        }

    }

    /**
     * Cancel all download thread
     */
    public void cancelDownLoad() {
        mExecutor.shutdown();
    }

    private void saveToCache(String key, Bitmap bmp) {
        mCache.put(key, bmp);
    }

    private Bitmap readFromCache(String key) {
        return mCache.get(key);
    }

    interface AdLoadListener {
        void loadImageSuccess(Bitmap bmp);

        void loadFileSuccess(String videoPath);
    }

}

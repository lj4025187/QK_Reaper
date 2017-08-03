package com.fighter.cache.downloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.fighter.cache.AdFileCacheUtil;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.EncryptUtils;
import com.fighter.common.utils.ReaperLog;
import com.fighter.wrapper.DownloadCallback;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/5/26.
 */

public class AdCacheFileDownloadManager {

    private final static String TAG = AdCacheFileDownloadManager.class.getSimpleName();

    private Context mContext;
    private static AdCacheFileDownloadManager mInstance;
    private DownloadManager mDownloadManager;
    private DownloadCallback mCallback;
    private OkHttpDownloader mOkHttpDownloader;
    private AdFileCacheUtil mAdFileCacheUtil;
    private DownloadCompleteReceiver mDownloadReceiver;

    private final static String CACHE_FILE_DIR = "ac_file";
    private static final long MAX_SIZE = 2 * 1024 * 1024;
    private OkHttpClient mClient;
    private String mDownloadPath;


    public static AdCacheFileDownloadManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AdCacheFileDownloadManager(context);
        return mInstance;
    }

    private AdCacheFileDownloadManager(Context context) {
        mContext = context;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadReceiver = new DownloadCompleteReceiver();
        mContext.registerReceiver(mDownloadReceiver, filter);

        File cacheDir = context.getCacheDir();
        File cacheFileDir = new File(cacheDir, CACHE_FILE_DIR);
        if (!cacheFileDir.exists()) {
            boolean success = cacheDir.mkdir();
            ReaperLog.i(TAG, "init cache file download directory " + success);
        }
        mDownloadPath = cacheFileDir.getAbsolutePath();
        mAdFileCacheUtil = AdFileCacheUtil.getInstance(context, MAX_SIZE);
        mClient = new OkHttpClient.Builder()
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        mOkHttpDownloader = new OkHttpDownloader(context, mClient);
    }

    /**
     * when finish app should unregister receiver
     */
    private void release() {
        if (mDownloadReceiver != null)
            mContext.unregisterReceiver(mDownloadReceiver);
    }

    public void setDownloadCallback(DownloadCallback callBack) {
        mCallback = callBack;
    }

    /**
     * cache file such ad imageUrl
     *
     * @param imageUrl
     * @return
     */
    public File cacheAdFile(String imageUrl) throws Exception {
        if (TextUtils.isEmpty(mDownloadPath)) {
            mDownloadPath = mContext.getCacheDir() + File.separator + CACHE_FILE_DIR;
        }
        mAdFileCacheUtil.clearCacheFile(new File(mDownloadPath));
        Request request = new Request.Builder().url(imageUrl).build();
        return mOkHttpDownloader.downloadSync(request,
                mDownloadPath,
                UUID.randomUUID().toString(),
                true);
    }

    /**
     * the method is for download apps
     *
     * @param url
     * @param title
     * @param desc
     * @return
     */
    public long requestDownload(String url, String title, String desc) {
        if (TextUtils.isEmpty(url)) {
            ReaperLog.e(TAG, " request download url is null");
            return -1;
        }
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        String fileName = EncryptUtils.encryptMD5ToString(url).substring(25);
        if (!TextUtils.isEmpty(title)) {
            fileName = title;
            request.setTitle(title);
        }
        if (!TextUtils.isEmpty(desc)) {
            request.setDescription(desc);
        }
        try {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".apk");
        } catch (IllegalStateException e) {
            ReaperLog.e(TAG, " IllegalStateException from DownloadManager.Request " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            ReaperLog.e(TAG, " NullPointerException from DownloadManager.Request " + e);
            e.printStackTrace();
        }
        return mDownloadManager.enqueue(request);
    }

    /**
     * after receiving download complete
     */
    private class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String fileName = null;
            int status = 0, reason = 0;
            if (mCallback == null)
                return;

            String action = intent.getAction();
            if (TextUtils.isEmpty(action))
                return;
            switch (action) {
                case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor cursor = null;
                    try {
                        cursor = mDownloadManager.query(query);
                        if (cursor != null && cursor.moveToFirst()) {
                            fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                        }
                    } catch (Exception e) {
                        ReaperLog.e(TAG, "DownloadCompleteReceiver " + e.toString());
                        e.printStackTrace();
                    } finally {
                        if (cursor != null)
                            CloseUtils.closeIOQuietly(cursor);
                    }
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        mCallback.onDownloadComplete(reference, fileName);
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        mCallback.onDownloadFailed(reference, reason);
                    }
                    break;
            }
        }
    }
}

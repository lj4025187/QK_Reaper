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
    private void onFinsh() {
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
    public File cacheAdFile(String imageUrl) {
        if(TextUtils.isEmpty(mDownloadPath)){
            mDownloadPath = mContext.getCacheDir() + File.separator + CACHE_FILE_DIR;
        }
        mAdFileCacheUtil.clearCacheFile(new File(mDownloadPath));
        Request request = new Request.Builder().url(imageUrl).build();
        File file = mOkHttpDownloader.downloadSync(request,
                mDownloadPath,
                UUID.randomUUID().toString(),
                true);
        return file;
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
        if(TextUtils.isEmpty(url)) {
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
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".apk");
        return mDownloadManager.enqueue(request);
    }

    public interface DownloadCallback {
        void onDownloadComplete(long reference, String fileName);
    }

    /**
     * after receiving download complete
     */
    private class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String fileName = null;
            if (mCallback == null)
                return;

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(reference);
            Cursor cursor = null;
            try {
                cursor = mDownloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int fileNameIdx =
                            cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    fileName = cursor.getString(fileNameIdx);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIOQuietly(cursor);
            }

            mCallback.onDownloadComplete(reference, fileName);
        }
    }
}

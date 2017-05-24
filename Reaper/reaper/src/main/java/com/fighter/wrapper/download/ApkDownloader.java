package com.fighter.wrapper.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.fighter.common.utils.CloseUtils;

public class ApkDownloader {
    private Context mContext;
    private DownloadManager mDownloadManager;
    private DownloadCallback mCallback;

    public ApkDownloader(Context context) {
        mContext = context;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(new DownloadCompleteReceiver(), filter);
    }

    public void setDownloadCallback(DownloadCallback downloadCallback) {
        mCallback = downloadCallback;
    }

    public long requestDownload(String url, String title, String desc) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        if (!TextUtils.isEmpty(title)) {
            request.setTitle(title);
        }
        if (!TextUtils.isEmpty(desc)) {
            request.setDescription(desc);
        }
        return mDownloadManager.enqueue(request);
    }

    // ----------------------------------------------------

    public interface DownloadCallback {
        void onDownloadComplete(long reference, String fileName);
    }

    // ----------------------------------------------------

    private class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String fileName = null;
            if (mCallback != null) {
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
}

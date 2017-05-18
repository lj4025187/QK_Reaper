package com.fighter.wrapper.download;

import com.fighter.common.utils.CloseUtils;

import java.io.File;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpDownloader {
    private OkHttpClient mClient;

    public OkHttpDownloader(OkHttpClient client) {
        mClient = client;
    }

    public File downloadSync(Request request, String destFileDir, String destFileName,
                             boolean keepExtension) {
        Response response = null;

        try {
            response = mClient.newCall(request).execute();
            FileConvert convert = new FileConvert(destFileDir, destFileName, keepExtension);
            return convert.convert(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(response);
        }

        return null;
    }

    public void downloadAsync(Request request, String destFileDir, String destFileName,
                              boolean keepExtension, DownloadCallback downloadCallback) {

    }

    // ----------------------------------------------------

    public interface DownloadCallback {
        public void downloadProgress(long currentSize, long totalSize,
                                     float progress, long networkSpeed);

        public void downloadSucceed(File file);
    }
}

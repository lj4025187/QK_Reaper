package com.fighter.network;

import java.io.File;

/**
 * Created by zhangjg on 17-6-28.
 */

public class DefaultReaperNetwork implements IReaperNetwork {

    @Override
    public byte[] doHttpGet(String url) {
        return new byte[0];
    }

    @Override
    public byte[] doHttpPost(String url, byte[] postBody) {
        return new byte[0];
    }

    @Override
    public byte[] doHttpsGet(String url, String[] certs) {
        return new byte[0];
    }

    @Override
    public byte[] doHttpsPost(String url, byte[] postBody, String[] certs) {
        return new byte[0];
    }

    @Override
    public boolean doHttpDownload(String url, File target) {
        return false;
    }

    @Override
    public boolean doHttpsDownload(String url, File target, String[] certs) {
        return false;
    }
}

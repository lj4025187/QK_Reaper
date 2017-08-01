package com.fighter.network;

import java.io.File;

/**
 * Created by zhangjg on 17-6-28.
 */

public interface IReaperNetwork {
    byte[] doHttpGet(String url);
    byte[] doHttpPost(String url, byte[] postBody);
    byte[] doHttpsGet(String url, String[] certs);
    byte[] doHttpsPost(String url, byte[] postBody, String[] certs);
    boolean doHttpDownload(String url, File target);
    boolean doHttpsDownload(String url, File target, String[] certs);
}

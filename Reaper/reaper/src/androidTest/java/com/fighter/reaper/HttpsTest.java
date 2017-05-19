package com.fighter.reaper;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.common.utils.ReaperLog;
import com.fighter.download.HttpsManager;
import com.fighter.download.NetworkUtil;
import com.qiku.serversdk.custom.AppConf;
import com.qiku.serversdk.custom.RestClientResponseCallback;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Matti on 2017/5/16.
 */

@RunWith(AndroidJUnit4.class)
public class HttpsTest {

    private static final String TAG = HttpsTest.class.getSimpleName();
    private static final String URL_REAPER_HIGHER_VERSION = "https://kyfw.12306.cn/otn/";
    private static final String URL_REAPER_DOWNLOAD =
            "https://kyfw.12306.cn/otn/czxx/init";

    String CERT12306 = "-----BEGIN CERTIFICATE-----\n" +
            "MIICmjCCAgOgAwIBAgIIbyZr5/jKH6QwDQYJKoZIhvcNAQEFBQAwRzELMAkGA1UE\n" +
            "BhMCQ04xKTAnBgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5\n" +
            "MQ0wCwYDVQQDEwRTUkNBMB4XDTA5MDUyNTA2NTYwMFoXDTI5MDUyMDA2NTYwMFow\n" +
            "RzELMAkGA1UEBhMCQ04xKTAnBgNVBAoTIFNpbm9yYWlsIENlcnRpZmljYXRpb24g\n" +
            "QXV0aG9yaXR5MQ0wCwYDVQQDEwRTUkNBMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n" +
            "iQKBgQDMpbNeb34p0GvLkZ6t72/OOba4mX2K/eZRWFfnuk8e5jKDH+9BgCb29bSo\n" +
            "tqPqTbxXWPxIOz8EjyUO3bfR5pQ8ovNTOlks2rS5BdMhoi4sUjCKi5ELiqtyww/X\n" +
            "gY5iFqv6D4Pw9QvOUcdRVSbPWo1DwMmH75It6pk/rARIFHEjWwIDAQABo4GOMIGL\n" +
            "MB8GA1UdIwQYMBaAFHletne34lKDQ+3HUYhMY4UsAENYMAwGA1UdEwQFMAMBAf8w\n" +
            "LgYDVR0fBCcwJTAjoCGgH4YdaHR0cDovLzE5Mi4xNjguOS4xNDkvY3JsMS5jcmww\n" +
            "CwYDVR0PBAQDAgH+MB0GA1UdDgQWBBR5XrZ3t+JSg0Ptx1GITGOFLABDWDANBgkq\n" +
            "hkiG9w0BAQUFAAOBgQDGrAm2U/of1LbOnG2bnnQtgcVaBXiVJF8LKPaV23XQ96HU\n" +
            "8xfgSZMJS6U00WHAI7zp0q208RSUft9wDq9ee///VOhzR6Tebg9QfyPSohkBrhXQ\n" +
            "envQog555S+C3eJAAVeNCTeMS3N/M5hzBRJAoffn3qoYdAO1Q8bTguOi+2849A==\n" +
            "-----END CERTIFICATE-----";
    @Test
    public void testHttps() {

        HttpsManager manager = new HttpsManager(CERT12306);
        Response response = manager.requestSync(URL_REAPER_DOWNLOAD);
        if (response == null) {
            ReaperLog.e(TAG, "response == null.");
            return;
        }
        try {
            String s = response.body().string();
            ReaperLog.e(TAG, "body : " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDownload() {
        String apkUrl = "http://n.qikucdn.com/t/reaper.apk";
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(apkUrl)
                .build();

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Response response = client.newCall(request).execute();
            if (response == null) {
                ReaperLog.e(TAG, "response == null");
                return;
            }
            ResponseBody body = response.body();
            if (body == null) {
                ReaperLog.e(TAG, "body == null");
                return;
            }
            is = body.byteStream();
            fos = new FileOutputStream(new File("/mnt/sdcard/aa.apk"));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
            ReaperLog.e(TAG, "download success.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testServerSdk() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    AppConf appConf = new AppConf("{'baseUrl':'https://api.os.qiku.com','resourceUrl':'Reaper/version'}");
//                    appConf.getAppConfAsync("Reaper", "1.0.0", "Reaper/version", "0", null, new RestClientResponseCallback() {
//                        @Override
//                        public void resHandler(JSONObject jsonObject) {
//                            ReaperLog.e(TAG, "json : " + jsonObject);
//                        }
//                    });

                    AppConf ac = new AppConf("{'baseUrl':'https://api.os.qiku.com','resourceUrl':'api/list'}");
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("app", "Reaper"); // 设置app
                    params.put("version", "1.0.0"); // 设置version
                    params.put("api", "version"); // 设置api
                    params.put("time", "1494922168");

                    JSONObject result = ac.getAppConfSyncCustom(params);
                    ReaperLog.e(TAG, "json : " + result);

                } catch (Exception e) {
                    e.printStackTrace();
                    ReaperLog.e(TAG, "err : " + e.getMessage());
                }
            }
        }).start();

    }

    @Test
    public void testNetworkState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReaperLog.e(TAG, "netWorkState : " + NetworkUtil.getNetWorkType(InstrumentationRegistry.getContext()));
            }
        }).start();

    }
}

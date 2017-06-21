package com.fighter.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;
import com.fighter.common.utils.CloseUtils;
import com.fighter.common.utils.ReaperLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import static android.provider.CalendarContract.Instances.BEGIN;

/**
 * Utils for http request
 *
 * Created by zhangjigang on 2017/5/16.
 */

public final class ReaperConfigHttpHelper {

    private static final String TAG = "ReaperConfigHttpHelper";

    private static final String SP_REAPER_CONFIG_TIME = "sp_reaper_config_time";
    private static final String KEY_NEXT_TIME_INTERVAL = "next_time_interval";
    private static final String KEY_LAST_SUCCESS_TIME = "last_success_time";

    /**
     * Get a OkHttpClient with http
     *
     * @return
     */
    public static OkHttpClient getHttpClient() {
        return new OkHttpClient();
    }

    /**
     * Get a OkHttpClient with https
     * @return
     */
    public static OkHttpClient getHttpsClient() {
        try {
            String[] certs = ReaperConfig.TEST ? new String[] {TEST_CERT} : CERTS;
            X509TrustManager tm = trustManagerForCertificates(certs);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory sf = sslContext.getSocketFactory();
            if (tm == null || sf == null) {
                return null;
            }
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sf, tm)
                    .build();
        } catch (Exception e) {
            ReaperLog.e(TAG, "exception when getHttpsClient : " + e.getClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return encrypt json String used as post body to request config
     *
     * @param context app context
     * @param pkg app package name
     * @param salt it is allocated by config server, it is relative to sdk version
     * @param appKey it is allocated by config server for every app
     * @return encrypt json string used as post body
     */
    public static String getConfigRequestBodyAsBase64(Context context, String pkg, String salt, String appKey) {
        if (context == null) {
            throw new IllegalArgumentException("context is null !!!");
        }
        if (TextUtils.isEmpty(pkg)) {
            throw new IllegalArgumentException("package is empty !!!");
        }
        ReaperConfigRequestBody body = ReaperConfigRequestBody.create(context, pkg);
        String oriJson = body.toJson();
        String encryptKey = salt + appKey;
        IRC4 rc4 = RC4Factory.create(encryptKey);
        return rc4.encryptToBase64(oriJson);
    }


    /**
     * Return encrypt json byte array used as post body to request config
     *
     * @param context app context
     * @param pkg app package name
     * @param salt it is allocated by config server, it is relative to sdk version
     * @param appKey it is allocated by config server for every app
     * @return encrypt json byte array used as post body
     */
    public static byte[] getConfigRequestBody(Context context, String pkg, String salt, String appKey) {
        if (context == null) {
            throw new IllegalArgumentException("context is null !!!");
        }
        if (TextUtils.isEmpty(pkg)) {
            throw new IllegalArgumentException("package is empty !!!");
        }
        ReaperConfigRequestBody body = ReaperConfigRequestBody.create(context, pkg);
        String oriJson = body.toJson();
        String encryptKey = salt + appKey;
        IRC4 rc4 = RC4Factory.create(encryptKey);
        return rc4.encrypt(oriJson.getBytes());
    }

    /**
     * Parse response body to java objects
     *
     * @param context
     * @param responseBody rc4 encrypted byte array
     * @return
     */
    public static List<ReaperAdvPos> parseResponseBody (Context context, byte[] responseBody, String key) {

        if (context == null || responseBody == null || TextUtils.isEmpty(key)) {
            return null;
        }
        ReaperLog.i(TAG, "parseResponseBody");
        IRC4 rc4 = RC4Factory.create(key);
        String responseText = new String(rc4.decrypt(responseBody));

        ReaperLog.i(TAG, "parseResponseBody. decrypted response body : " + responseText);

        JSONObject responseObj = JSON.parseObject(responseText);
        String result = responseObj.getString(ReaperConfig.KEY_RES_RESULT);
        if (ReaperConfig.VALUE_RESULT_OK.equals(result)) {
            ReaperLog.i(TAG, "parseResponseBody ok");

            String nextTime = responseObj.getString(ReaperConfig.KEY_RES_NEXT_TIME);
            // save next time
            recordNextTime(context, nextTime);

            ReaperLog.i(TAG, "parseResponseBody . next time : " + nextTime);
            JSONArray posArray = responseObj.getJSONArray(ReaperConfig.KEY_RES_POS_IDS);
            int posSize = posArray.size();
            ArrayList<ReaperAdvPos> posList = new ArrayList<>(posSize);
            ReaperLog.i(TAG, "parseResponseBody pos size : " + posSize);
            for (int i = 0; i < posSize; i++) {
                JSONObject posObj = posArray.getJSONObject(i);
                ReaperAdvPos pos = posObj.toJavaObject(ReaperAdvPos.class);
                ReaperLog.i(TAG, "parse ReaperAdvPos : " + pos);
                JSONArray senseArray = posObj.getJSONArray(ReaperConfig.KEY_RES_ADSENSES);
                int senseSize = senseArray.size();
                ReaperLog.i(TAG, "    parse sense size : " + senseSize);
                for (int j = 0; j < senseSize; j++) {
                    JSONObject senseObj = senseArray.getJSONObject(j);
                    ReaperAdSense adSense = senseObj.toJavaObject(ReaperAdSense.class);
                    pos.addAdSense(adSense);
                    ReaperLog.i(TAG, "    parse sense  : " + adSense);
                }
                posList.add(pos);
            }
            return posList;
        } else if (ReaperConfig.VALUE_RESULT_ERROR.equals(result)) {
            String reason = responseObj.getString(ReaperConfig.KEY_RES_REASON);
            ReaperLog.i(TAG, "parseResponseBody . get config error : " + reason);
        }
        return null;
    }

    /**
     * Check timeout
     * If timeout, client must sync new config from server
     *
     * @param context
     * @return
     */
    public static boolean shouldRequestAgain(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences sp =
                context.getSharedPreferences(SP_REAPER_CONFIG_TIME, Context.MODE_PRIVATE);
        long nextTimeInterval = sp.getLong(KEY_NEXT_TIME_INTERVAL, 0);
        long lastSuccessTime = sp.getLong(KEY_LAST_SUCCESS_TIME, 0);
        long currentTime = System.currentTimeMillis() / 1000;
        // user device time error
        if (currentTime <= lastSuccessTime) {
            return true;
        }
        // timeout
        if (currentTime >= lastSuccessTime + nextTimeInterval) {
            return true;
        }

        return false;
    }

    /**
     * Record time when request success every time
     *
     * @param context
     * @return
     */
    public static boolean recordLastSuccessTime(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null !!!");
        }
        SharedPreferences sp =
                context.getSharedPreferences(SP_REAPER_CONFIG_TIME, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_LAST_SUCCESS_TIME, System.currentTimeMillis()/1000).apply();
        return true;
    }

    /**
     * Record next time interval
     * This value is returned on http response
     *
     * @param context
     * @param nextTime
     * @return
     */
    public static boolean recordNextTime(Context context, String nextTime) {
        if (context == null) {
            throw new IllegalArgumentException("context is null !!!");
        }
        if (TextUtils.isEmpty(nextTime)) {
            return false;
        }
        if (!TextUtils.isDigitsOnly(nextTime)) {
            throw  new IllegalArgumentException("next time must digits only !!!");
        }
        SharedPreferences sp =
                context.getSharedPreferences(SP_REAPER_CONFIG_TIME, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_NEXT_TIME_INTERVAL, Long.parseLong(nextTime)).apply();
        return true;
    }

    /**
     * Get a X509TrustManager from ssl certificates
     *
     * @param certs
     * @return
     * @throws Exception
     */
    private static X509TrustManager trustManagerForCertificates(String[] certs) throws Exception {
        if (certs == null || certs.length == 0) {
            return null;
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            ks.load(null, null);
        } catch (IOException e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "trustManagerForCertificates, KeyStore load exception");
            return null;
        }
        int index = 0;
        for (String cert : certs) {
            InputStream is = new ByteArrayInputStream(cert.getBytes());
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate certificate = cf.generateCertificate(is);
                ks.setCertificateEntry("" + index++, certificate);
            } finally {
                CloseUtils.closeIOQuietly(is);
            }
        }

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        TrustManager[] tms = tmf.getTrustManagers();
        if (tms != null && tms.length >= 0) {
            return (X509TrustManager)tms[0];
        }
        return null;
    }


    /**
     * Server Test SSL certificate
     */
    private static final String TEST_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIICMTCCAZoCCQDfS7cvvWk04DANBgkqhkiG9w0BAQUFADBdMQswCQYDVQQGEwJD\n" +
            "TjEQMA4GA1UECAwHQmVpamluZzERMA8GA1UEBwwIQ2hhb3lhbmcxDTALBgNVBAoM\n" +
            "BFFJSFUxGjAYBgNVBAMMEXQuYWR2Lm9zLnFpa3UuY29tMB4XDTE3MDUxNjA4MzIy\n" +
            "OVoXDTE3MDYxNTA4MzIyOVowXTELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB0JlaWpp\n" +
            "bmcxETAPBgNVBAcMCENoYW95YW5nMQ0wCwYDVQQKDARRSUhVMRowGAYDVQQDDBF0\n" +
            "LmFkdi5vcy5xaWt1LmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAwUXY\n" +
            "YCUh0ZpCpOr8NoR3KNBj70p8sfPsfjOmRmhHI+TD0UdQcpdGlr/yCeseMhvTr81w\n" +
            "YgG+4IElB3TqI8RBrpaDKNxMs6gmoqwEkVOijGJ67G0EV8znMJ9wZgQv/A9GAVYj\n" +
            "1Qogebv1vC8MaCjBAkyAg75CHWlWNQLmO3SFPRUCAwEAATANBgkqhkiG9w0BAQUF\n" +
            "AAOBgQCoTsc/RenXgkweH2mN1h+b59kGr0xWjRe72cB+YtQKlwcgv1B2ShUYHNZ0\n" +
            "ZYknHEKryuJ5gHcVeZwjY2ze1Xh4FXKqjKue0n/OTWKZ6x9UzuosmiRujrWXOMtS\n" +
            "tF74BjGwlhmN4iakcGXv/c6slO4P8OA2JIoRH3rIy4Lpc/+4AA==\n" +
            "-----END CERTIFICATE-----";

    /**
     * Server SSL certificate 1
     */
    private static final String CERT1 =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIFxjCCBK6gAwIBAgIQSPokzyCwxS61C6bnSHSkyzANBgkqhkiG9w0BAQsFADBE\n" +
            "MQswCQYDVQQGEwJDTjEaMBgGA1UECgwRV29TaWduIENBIExpbWl0ZWQxGTAXBgNV\n" +
            "BAMMEFdvU2lnbiBPViBTU0wgQ0EwHhcNMTYxMjA3MDYxNTM2WhcNMTkxMjA3MDYx\n" +
            "NTM2WjB7MQswCQYDVQQGEwJDTjEnMCUGA1UECgwe5YyX5Lqs5aWH6JmO56eR5oqA\n" +
            "5pyJ6ZmQ5YWs5Y+4MRIwEAYDVQQHDAnljJfkuqzluIIxEjAQBgNVBAgMCeWMl+S6\n" +
            "rOW4gjEbMBkGA1UEAwwSY24uYWR2Lm9zLnFpa3UuY29tMIIBIjANBgkqhkiG9w0B\n" +
            "AQEFAAOCAQ8AMIIBCgKCAQEAySQj1wPTG1MfKz7I7dvlTQOlQq0DKcDSVPblm0CQ\n" +
            "9OKtoF9Iyj7DpoEBdD5Du7Etoz+kI9vTuKIAAbywHdQ53GDtnvs1i3W5RFzvQ5R/\n" +
            "WG7Wwh6jeX3b9sPlzNqkdlbbhv6qIaSgtLl+/Zc35IwZ3iqUcaB47kLPR0bTTli7\n" +
            "eFuOTSuQG/5mmGLbjOR1ZmtG3Bm7WPZueEFIeHSl7IBZbbVKlfAKifz3Y9hEVHGa\n" +
            "JU/P03hucmG7SV4sAv1rth8F0PqA+Ze4w68BiHjMlvNBijS+OtRLAuPqWbzGsB7V\n" +
            "iugZQAQ0lVhtgULu+pywNpkvmVWfTfSog+xhZy32tnWgawIDAQABo4ICezCCAncw\n" +
            "DAYDVR0TAQH/BAIwADA8BgNVHR8ENTAzMDGgL6AthitodHRwOi8vd29zaWduLmNy\n" +
            "bC5jZXJ0dW0ucGwvd29zaWduLW92Y2EuY3JsMHcGCCsGAQUFBwEBBGswaTAuBggr\n" +
            "BgEFBQcwAYYiaHR0cDovL3dvc2lnbi1vdmNhLm9jc3AtY2VydHVtLmNvbTA3Bggr\n" +
            "BgEFBQcwAoYraHR0cDovL3JlcG9zaXRvcnkuY2VydHVtLnBsL3dvc2lnbi1vdmNh\n" +
            "LmNlcjAfBgNVHSMEGDAWgBShE1TcVnMsJ4LKyITv7r8A/V+rVjAdBgNVHQ4EFgQU\n" +
            "6KGUelkvvLXRz9vBZdTxwnoaxhMwDgYDVR0PAQH/BAQDAgWgMIIBIAYDVR0gBIIB\n" +
            "FzCCARMwCAYGZ4EMAQICMIIBBQYMKoRoAYb2dwIFAQwCMIH0MIHxBggrBgEFBQcC\n" +
            "AjCB5DAfFhhBc3NlY28gRGF0YSBTeXN0ZW1zIFMuQS4wAwIBARqBwFVzYWdlIG9m\n" +
            "IHRoaXMgY2VydGlmaWNhdGUgaXMgc3RyaWN0bHkgc3ViamVjdGVkIHRvIHRoZSBD\n" +
            "RVJUVU0gQ2VydGlmaWNhdGlvbiBQcmFjdGljZSBTdGF0ZW1lbnQgKENQUykgaW5j\n" +
            "b3Jwb3JhdGVkIGJ5IHJlZmVyZW5jZSBoZXJlaW4gYW5kIGluIHRoZSByZXBvc2l0\n" +
            "b3J5IGF0IGh0dHBzOi8vd3d3LmNlcnR1bS5wbC9yZXBvc2l0b3J5LjAdBgNVHSUE\n" +
            "FjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwHQYDVR0RBBYwFIISY24uYWR2Lm9zLnFp\n" +
            "a3UuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQCeGRHEjQf/yglPPI2IBHFpOsElvd+M\n" +
            "D5BFzp6BBqwX3+ZTBN7BTRCHvYogAZq2EvlLEd5Sqa2BgP1jqKH1dY7ve7VtKoL1\n" +
            "BdvN1+VFJJen+X+1ul1TTz3nafIAQXYoRrj/Sp8IB43kBZC3JbC1mYWrd+o4wUvH\n" +
            "O9LkDcWTz+l4naSyMSoKl0kF4P4HMar657MK7u6dLknYdlnzQ6e7kdWUwIAeAali\n" +
            "F1nYmwCK/qQzTUkkCUJEFiRR6g0o1I8i/+2z4eq8tFo1kpNG8i5I8TVZhWQattUT\n" +
            "O5PXRl4ktwVYO6kzyAKh0LayEx4AWEGk0EIWmOsu3LLZ29In/N9di/am\n" +
            "-----END CERTIFICATE-----";

    /**
     * Server SSL certificate 2
     */
    private static final String CERT2 =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIEtTCCA52gAwIBAgIRAO8FGnQaHZQJ/KXkZA+NPJswDQYJKoZIhvcNAQELBQAw\n" +
            "fjELMAkGA1UEBhMCUEwxIjAgBgNVBAoTGVVuaXpldG8gVGVjaG5vbG9naWVzIFMu\n" +
            "QS4xJzAlBgNVBAsTHkNlcnR1bSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEiMCAG\n" +
            "A1UEAxMZQ2VydHVtIFRydXN0ZWQgTmV0d29yayBDQTAeFw0xNjExMDkwODMzNDRa\n" +
            "Fw0yNjExMDkwODMzNDRaMEQxCzAJBgNVBAYTAkNOMRowGAYDVQQKDBFXb1NpZ24g\n" +
            "Q0EgTGltaXRlZDEZMBcGA1UEAwwQV29TaWduIE9WIFNTTCBDQTCCASIwDQYJKoZI\n" +
            "hvcNAQEBBQADggEPADCCAQoCggEBAKRzU7QtbSdi6uUiqewzx81eEdrg0RROHTs1\n" +
            "eXndSwxxUAVDC+FPYvpgWc+bYMVjUJQEIP+SNzsIGvB/YoabRoN7cLBDzPTgYnW8\n" +
            "Pl/wYWXuGNyr1E7bV9Fec37HlvhE39Ntwp31gjMFwTOZ7Zw0QzS7w9PjO4A4anwb\n" +
            "maBJgrRa3GFSgoJ+WIr5brQ6hEgm7rKRNPx6L9Sj2aSl/EWRPPv73j5xeWGcgOPp\n" +
            "U+8eZmqpX+XfCl34o5OQJWi/F7bACetVhvFtWGuLNcZ0eYwU13jOEx3NNsILzIYP\n" +
            "oWJztxd3aPkQOX6cNbJGTvLRcfmGDM0ASq3/BsCrR0o/ruCcd6cCAwEAAaOCAWYw\n" +
            "ggFiMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFKETVNxWcywngsrIhO/u\n" +
            "vwD9X6tWMB8GA1UdIwQYMBaAFAh2zcsH/yT2xc3tu5C84oQ3RnX3MA4GA1UdDwEB\n" +
            "/wQEAwIBBjAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwNQYDVR0fBC4w\n" +
            "LDAqoCigJoYkaHR0cDovL3N1YmNhLmNybC5jZXJ0dW0ucGwvY3RuY2EuY3JsMGsG\n" +
            "CCsGAQUFBwEBBF8wXTAoBggrBgEFBQcwAYYcaHR0cDovL3N1YmNhLm9jc3AtY2Vy\n" +
            "dHVtLmNvbTAxBggrBgEFBQcwAoYlaHR0cDovL3JlcG9zaXRvcnkuY2VydHVtLnBs\n" +
            "L2N0bmNhLmNlcjA5BgNVHSAEMjAwMC4GBFUdIAAwJjAkBggrBgEFBQcCARYYaHR0\n" +
            "cDovL3d3dy5jZXJ0dW0ucGwvQ1BTMA0GCSqGSIb3DQEBCwUAA4IBAQCLBeq0MMgd\n" +
            "qULSuAua1YwHNgbFAAnMXd9iiSxbIKoSfYKsrFggNCFX73ex4b64iIhQ2BBr82/B\n" +
            "MNpC4rEvnr1x0oFv8DBO1GYimQaq8E9hjnO1UYYEPelVsykOpnDLklTsBZ4vhhq/\n" +
            "hq1mbs+6G+vsAjO9jVnuxP6toOTNBqvURRumMF0P165MoFdh0kzSjUts+1d8Llnb\n" +
            "DJaZht0O19k1ZdBBmPD3cwbTI+tChOELAVt4Nb5dDGPWqSxc5Nl2j95T3aK1KL2d\n" +
            "2vV16DSVShJIz04QHatcJlNZLJDbSu70c5fPU8YiJdRpfkubANAmwcDB+uNhtYz+\n" +
            "zEji0KnE2oNA\n" +
            "-----END CERTIFICATE-----";

    /**
     * Server SSL certificate 3
     */
    private static final String CERT3 =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIEtDCCA5ygAwIBAgIRAJOShUABZXFflH8oj+/JmygwDQYJKoZIhvcNAQELBQAw\n" +
            "PjELMAkGA1UEBhMCUEwxGzAZBgNVBAoTElVuaXpldG8gU3AuIHogby5vLjESMBAG\n" +
            "A1UEAxMJQ2VydHVtIENBMB4XDTA4MTAyMjEyMDczN1oXDTI3MDYxMDEwNDYzOVow\n" +
            "fjELMAkGA1UEBhMCUEwxIjAgBgNVBAoTGVVuaXpldG8gVGVjaG5vbG9naWVzIFMu\n" +
            "QS4xJzAlBgNVBAsTHkNlcnR1bSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEiMCAG\n" +
            "A1UEAxMZQ2VydHVtIFRydXN0ZWQgTmV0d29yayBDQTCCASIwDQYJKoZIhvcNAQEB\n" +
            "BQADggEPADCCAQoCggEBAOP7faNyusLwyRSH9WsBTuFuQAe6bSddf/dbLbNax1Ff\n" +
            "q6QypmGHtm4PhtIwApf412lXoRg5XWpkecYBWaw8MUo4fNIE0kso6CBfOweizE1z\n" +
            "2/OuT8dW1Vqnlon686to1COGWSfPCSe8rG5ygxwwct/gounS4XR1Gb0qnnsVVAQb\n" +
            "10M5rVUoxeIau/TA5K44STPMdoWfOUXSpJ7yEoxR+HzkLX/1rF/rFp+xLdG6zJFC\n" +
            "d0wlyZA4b9vwzPuOHpdZPtVgTuYFKO1JeRNLukjbL/ly0znK/h/YNHL1tEDPMQHD\n" +
            "7N4RLRddH7hQ0V4Zp2neBzMoylCV+adUy1SGUEWp+UkCAwEAAaOCAWswggFnMA8G\n" +
            "A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFAh2zcsH/yT2xc3tu5C84oQ3RnX3MFIG\n" +
            "A1UdIwRLMEmhQqRAMD4xCzAJBgNVBAYTAlBMMRswGQYDVQQKExJVbml6ZXRvIFNw\n" +
            "LiB6IG8uby4xEjAQBgNVBAMTCUNlcnR1bSBDQYIDAQAgMA4GA1UdDwEB/wQEAwIB\n" +
            "BjAsBgNVHR8EJTAjMCGgH6AdhhtodHRwOi8vY3JsLmNlcnR1bS5wbC9jYS5jcmww\n" +
            "aAYIKwYBBQUHAQEEXDBaMCgGCCsGAQUFBzABhhxodHRwOi8vc3ViY2Eub2NzcC1j\n" +
            "ZXJ0dW0uY29tMC4GCCsGAQUFBzAChiJodHRwOi8vcmVwb3NpdG9yeS5jZXJ0dW0u\n" +
            "cGwvY2EuY2VyMDkGA1UdIAQyMDAwLgYEVR0gADAmMCQGCCsGAQUFBwIBFhhodHRw\n" +
            "Oi8vd3d3LmNlcnR1bS5wbC9DUFMwDQYJKoZIhvcNAQELBQADggEBAI3m/UBmo0yc\n" +
            "p6uh2oTdHDAH5tvHLeyDoVbkHTwmoaUJK+h9Yr6ydZTdCPJ/KEHkgGcCToqPwzXQ\n" +
            "1aknKOrS9KsGhkOujOP5iH3g271CgYACEnWy6BdxqyGVMUZCDYgQOdNv7C9C6kBT\n" +
            "Yr/rynieq6LVLgXqM6vp1peUQl4E7Sztapx6lX0FKgV/CF1mrWHUdqx1lpdzY70a\n" +
            "QVkppV4ig8OLWfqaova9ML9yHRyZhpzyhTwd9yaWLy75ArG1qVDoOPqbCl60BMDO\n" +
            "TjksygtbYvBNWFA0meaaLNKQ1wmB1sCqXs7+0vehukvZ1oaOGR+mBkdCcuBWCgAc\n" +
            "eLmNzJkEN0k=\n" +
            "-----END CERTIFICATE-----";

    //change to comp.360os.com
    private static final String CERT_1 =
            "-----BEGIN CERTIFICATE-----\n"+
            "MIIFwzCCBKugAwIBAgIQMWBMPW8Zads0KYExPGj+pDANBgkqhkiG9w0BAQsFADBE\n"+
            "MQswCQYDVQQGEwJDTjEaMBgGA1UECgwRV29TaWduIENBIExpbWl0ZWQxGTAXBgNV\n"+
            "BAMMEFdvU2lnbiBPViBTU0wgQ0EwHhcNMTcwMzE4MDczMDI2WhcNMjAwMzE2MDcz\n"+
            "MDI2WjB0MQswCQYDVQQGEwJDTjEnMCUGA1UECgwe5YyX5Lqs5aWH6JmO56eR5oqA\n"+
            "5pyJ6ZmQ5YWs5Y+4MRIwEAYDVQQHDAnljJfkuqzluIIxEjAQBgNVBAgMCeWMl+S6\n"+
            "rOW4gjEUMBIGA1UEAwwLKi4zNjBvcy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IB\n"+
            "DwAwggEKAoIBAQCkoCOwqjk3mmSQNkN0CFTGdqXQFcKidM2m9ToUR38KAFsHXTCO\n"+
            "MNsxLTkTVxmXz97MZ/JAUN/fX6rzgH1F37hqDBTYV82f/kdscSuthTiLw43xC/tu\n"+
            "4zkAbkfLlUmCXDGcbbOTNAzq4zg4uind/VHZWJYnsBnJdvy/Ftg78+NN4f4dmNNX\n"+
            "QEfZjr8Zg60BO0uZtqXdY6OWLNqRcCiRCQsiUjGQ2Pb3Tgf/YVCFfiWLieCYczAW\n"+
            "n08XoajDsQeQkyTRKQ7arc9y5Zlyson3AqBfYzq/rJHcg2rbh7aozHIPubAEhEhv\n"+
            "n7IUnFW99gOqisFO0Kg5tGG3dDe0bK8M28gfAgMBAAGjggJ/MIICezAMBgNVHRMB\n"+
            "Af8EAjAAMDwGA1UdHwQ1MDMwMaAvoC2GK2h0dHA6Ly93b3NpZ24uY3JsLmNlcnR1\n"+
            "bS5wbC93b3NpZ24tb3ZjYS5jcmwwdwYIKwYBBQUHAQEEazBpMC4GCCsGAQUFBzAB\n"+
            "hiJodHRwOi8vd29zaWduLW92Y2Eub2NzcC1jZXJ0dW0uY29tMDcGCCsGAQUFBzAC\n"+
            "hitodHRwOi8vcmVwb3NpdG9yeS5jZXJ0dW0ucGwvd29zaWduLW92Y2EuY2VyMB8G\n"+
            "A1UdIwQYMBaAFKETVNxWcywngsrIhO/uvwD9X6tWMB0GA1UdDgQWBBSk62wgrKKL\n"+
            "4HSJqm4C10YGCORdajAOBgNVHQ8BAf8EBAMCBaAwggEgBgNVHSAEggEXMIIBEzAI\n"+
            "BgZngQwBAgIwggEFBgwqhGgBhvZ3AgUBDAIwgfQwgfEGCCsGAQUFBwICMIHkMB8W\n"+
            "GEFzc2VjbyBEYXRhIFN5c3RlbXMgUy5BLjADAgEBGoHAVXNhZ2Ugb2YgdGhpcyBj\n"+
            "ZXJ0aWZpY2F0ZSBpcyBzdHJpY3RseSBzdWJqZWN0ZWQgdG8gdGhlIENFUlRVTSBD\n"+
            "ZXJ0aWZpY2F0aW9uIFByYWN0aWNlIFN0YXRlbWVudCAoQ1BTKSBpbmNvcnBvcmF0\n"+
            "ZWQgYnkgcmVmZXJlbmNlIGhlcmVpbiBhbmQgaW4gdGhlIHJlcG9zaXRvcnkgYXQg\n"+
            "aHR0cHM6Ly93d3cuY2VydHVtLnBsL3JlcG9zaXRvcnkuMB0GA1UdJQQWMBQGCCsG\n"+
            "AQUFBwMBBggrBgEFBQcDAjAhBgNVHREEGjAYggsqLjM2MG9zLmNvbYIJMzYwb3Mu\n"+
            "Y29tMA0GCSqGSIb3DQEBCwUAA4IBAQBhMTEeUc6zdkdtochE7RZ7yfjslE5f+wCk\n"+
            "N6yEHiK2gGJnSAEXxWWbxqMf6iqYBrCf1c3JiyY+t6TpKfceypgZXFKxoWichnEj\n"+
            "suFC09R9hRjC9UQVBEteY3DY638BpRgoaPIh+v1ggtVSHnLu5fApzyV8B/fRMgNa\n"+
            "IHw5PragV3PQiVbJlPqiMF/KWI5C18mQJqPhPNXIAvQWAvYaRZD1nRTURV041+x4\n"+
            "/qlo8upnMhIfwkdfkcfU36Eg6CwS/YRg3WXfWdwQ3V0QH7bNxI4aZzJDsksTLM0p\n"+
            "XRHg29006kz2E/cpIVyPeguZ17DDTM6rEZxSNwy8klmgMmyJ+a3T\n"+
            "-----END CERTIFICATE-----";

    private static final String CERT_2 =
            "-----BEGIN CERTIFICATE-----\n"+
            "MIIEtTCCA52gAwIBAgIRAO8FGnQaHZQJ/KXkZA+NPJswDQYJKoZIhvcNAQELBQAw\n" +
            "fjELMAkGA1UEBhMCUEwxIjAgBgNVBAoTGVVuaXpldG8gVGVjaG5vbG9naWVzIFMu\n" +
            "QS4xJzAlBgNVBAsTHkNlcnR1bSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEiMCAG\n" +
            "A1UEAxMZQ2VydHVtIFRydXN0ZWQgTmV0d29yayBDQTAeFw0xNjExMDkwODMzNDRa\n" +
            "Fw0yNjExMDkwODMzNDRaMEQxCzAJBgNVBAYTAkNOMRowGAYDVQQKDBFXb1NpZ24g\n" +
            "Q0EgTGltaXRlZDEZMBcGA1UEAwwQV29TaWduIE9WIFNTTCBDQTCCASIwDQYJKoZI\n" +
            "hvcNAQEBBQADggEPADCCAQoCggEBAKRzU7QtbSdi6uUiqewzx81eEdrg0RROHTs1\n" +
            "eXndSwxxUAVDC+FPYvpgWc+bYMVjUJQEIP+SNzsIGvB/YoabRoN7cLBDzPTgYnW8\n" +
            "Pl/wYWXuGNyr1E7bV9Fec37HlvhE39Ntwp31gjMFwTOZ7Zw0QzS7w9PjO4A4anwb\n" +
            "maBJgrRa3GFSgoJ+WIr5brQ6hEgm7rKRNPx6L9Sj2aSl/EWRPPv73j5xeWGcgOPp\n" +
            "U+8eZmqpX+XfCl34o5OQJWi/F7bACetVhvFtWGuLNcZ0eYwU13jOEx3NNsILzIYP\n" +
            "oWJztxd3aPkQOX6cNbJGTvLRcfmGDM0ASq3/BsCrR0o/ruCcd6cCAwEAAaOCAWYw\n" +
            "ggFiMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFKETVNxWcywngsrIhO/u\n" +
            "vwD9X6tWMB8GA1UdIwQYMBaAFAh2zcsH/yT2xc3tu5C84oQ3RnX3MA4GA1UdDwEB\n" +
            "/wQEAwIBBjAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwNQYDVR0fBC4w\n" +
            "LDAqoCigJoYkaHR0cDovL3N1YmNhLmNybC5jZXJ0dW0ucGwvY3RuY2EuY3JsMGsG\n" +
            "CCsGAQUFBwEBBF8wXTAoBggrBgEFBQcwAYYcaHR0cDovL3N1YmNhLm9jc3AtY2Vy\n" +
            "dHVtLmNvbTAxBggrBgEFBQcwAoYlaHR0cDovL3JlcG9zaXRvcnkuY2VydHVtLnBs\n" +
            "L2N0bmNhLmNlcjA5BgNVHSAEMjAwMC4GBFUdIAAwJjAkBggrBgEFBQcCARYYaHR0\n" +
            "cDovL3d3dy5jZXJ0dW0ucGwvQ1BTMA0GCSqGSIb3DQEBCwUAA4IBAQCLBeq0MMgd\n" +
            "qULSuAua1YwHNgbFAAnMXd9iiSxbIKoSfYKsrFggNCFX73ex4b64iIhQ2BBr82/B\n" +
            "MNpC4rEvnr1x0oFv8DBO1GYimQaq8E9hjnO1UYYEPelVsykOpnDLklTsBZ4vhhq/\n" +
            "hq1mbs+6G+vsAjO9jVnuxP6toOTNBqvURRumMF0P165MoFdh0kzSjUts+1d8Llnb\n" +
            "DJaZht0O19k1ZdBBmPD3cwbTI+tChOELAVt4Nb5dDGPWqSxc5Nl2j95T3aK1KL2d\n" +
            "2vV16DSVShJIz04QHatcJlNZLJDbSu70c5fPU8YiJdRpfkubANAmwcDB+uNhtYz+\n" +
            "zEji0KnE2oNA\n" +
            "-----END CERTIFICATE-----";

    private static final String CERT_3 =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIEtDCCA5ygAwIBAgIRAJOShUABZXFflH8oj+/JmygwDQYJKoZIhvcNAQELBQAw\n" +
            "PjELMAkGA1UEBhMCUEwxGzAZBgNVBAoTElVuaXpldG8gU3AuIHogby5vLjESMBAG\n" +
            "A1UEAxMJQ2VydHVtIENBMB4XDTA4MTAyMjEyMDczN1oXDTI3MDYxMDEwNDYzOVow\n" +
            "fjELMAkGA1UEBhMCUEwxIjAgBgNVBAoTGVVuaXpldG8gVGVjaG5vbG9naWVzIFMu\n" +
            "QS4xJzAlBgNVBAsTHkNlcnR1bSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEiMCAG\n" +
            "A1UEAxMZQ2VydHVtIFRydXN0ZWQgTmV0d29yayBDQTCCASIwDQYJKoZIhvcNAQEB\n" +
            "BQADggEPADCCAQoCggEBAOP7faNyusLwyRSH9WsBTuFuQAe6bSddf/dbLbNax1Ff\n" +
            "q6QypmGHtm4PhtIwApf412lXoRg5XWpkecYBWaw8MUo4fNIE0kso6CBfOweizE1z\n" +
            "2/OuT8dW1Vqnlon686to1COGWSfPCSe8rG5ygxwwct/gounS4XR1Gb0qnnsVVAQb\n" +
            "10M5rVUoxeIau/TA5K44STPMdoWfOUXSpJ7yEoxR+HzkLX/1rF/rFp+xLdG6zJFC\n" +
            "d0wlyZA4b9vwzPuOHpdZPtVgTuYFKO1JeRNLukjbL/ly0znK/h/YNHL1tEDPMQHD\n" +
            "7N4RLRddH7hQ0V4Zp2neBzMoylCV+adUy1SGUEWp+UkCAwEAAaOCAWswggFnMA8G\n" +
            "A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFAh2zcsH/yT2xc3tu5C84oQ3RnX3MFIG\n" +
            "A1UdIwRLMEmhQqRAMD4xCzAJBgNVBAYTAlBMMRswGQYDVQQKExJVbml6ZXRvIFNw\n" +
            "LiB6IG8uby4xEjAQBgNVBAMTCUNlcnR1bSBDQYIDAQAgMA4GA1UdDwEB/wQEAwIB\n" +
            "BjAsBgNVHR8EJTAjMCGgH6AdhhtodHRwOi8vY3JsLmNlcnR1bS5wbC9jYS5jcmww\n" +
            "aAYIKwYBBQUHAQEEXDBaMCgGCCsGAQUFBzABhhxodHRwOi8vc3ViY2Eub2NzcC1j\n" +
            "ZXJ0dW0uY29tMC4GCCsGAQUFBzAChiJodHRwOi8vcmVwb3NpdG9yeS5jZXJ0dW0u\n" +
            "cGwvY2EuY2VyMDkGA1UdIAQyMDAwLgYEVR0gADAmMCQGCCsGAQUFBwIBFhhodHRw\n" +
            "Oi8vd3d3LmNlcnR1bS5wbC9DUFMwDQYJKoZIhvcNAQELBQADggEBAI3m/UBmo0yc\n" +
            "p6uh2oTdHDAH5tvHLeyDoVbkHTwmoaUJK+h9Yr6ydZTdCPJ/KEHkgGcCToqPwzXQ\n" +
            "1aknKOrS9KsGhkOujOP5iH3g271CgYACEnWy6BdxqyGVMUZCDYgQOdNv7C9C6kBT\n" +
            "Yr/rynieq6LVLgXqM6vp1peUQl4E7Sztapx6lX0FKgV/CF1mrWHUdqx1lpdzY70a\n" +
            "QVkppV4ig8OLWfqaova9ML9yHRyZhpzyhTwd9yaWLy75ArG1qVDoOPqbCl60BMDO\n" +
            "TjksygtbYvBNWFA0meaaLNKQ1wmB1sCqXs7+0vehukvZ1oaOGR+mBkdCcuBWCgAc\n" +
            "eLmNzJkEN0k=\n" +
            "-----END CERTIFICATE-----";
    /**
     * Server SSL certificates
     */
//    private static final String[] CERTS = {CERT1, CERT2, CERT3};
    private static final String[] CERTS = {CERT_1, CERT_2, CERT_3};

}

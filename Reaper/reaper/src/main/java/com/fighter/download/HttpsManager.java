package com.fighter.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fighter.common.utils.ReaperLog;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Matti on 2017/5/12.
 */

public final class HttpsManager {
    private static final java.lang.String TAG = HttpsManager.class.getSimpleName();
    private static final boolean DEBUG_HTTPS = true;

    private OkHttpClient mHttpsClient;


    public HttpsManager(String cert) {
        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;
        final InputStream inputStream;
        try {
            //inputStream = assetManager.open("srca.cer");
            inputStream= new okio.Buffer().writeUtf8(cert).inputStream();
            trustManager = trustManagerForCertificates(inputStream);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();

            mHttpsClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            ReaperLog.e(TAG, "error : " + e.getMessage());
        }
    }

    public OkHttpClient getHttpsClient() {
        return mHttpsClient;
    }

    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     * <p>
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     * <p>
     * <p>
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     * <p>
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     */
    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        //char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyStore.load(null, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, null);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    /**
     * 同步请求
     * @param url
     * @return
     */
    public Response requestSync(@NonNull String url) {
        if (TextUtils.isEmpty(url) || mHttpsClient == null) {
            ReaperLog.e(TAG, "requestSync error, check url !");
            return null;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            return mHttpsClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG_HTTPS)
                ReaperLog.e(TAG, "requestSync : " + e.getMessage());
        }
        return null;
    }

    public void requestAsync(@NonNull String url, Callback callback) {
        if (TextUtils.isEmpty(url) || mHttpsClient == null
                || callback == null) {
            ReaperLog.e(TAG, "requestAsync error , check url or callback !");
            return;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        mHttpsClient.newCall(request).enqueue(callback);
    }

}
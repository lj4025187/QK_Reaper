package com.fighter.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.File;

/**
 *
 * check sign from the apk utils
 *
 * Created by lichen on 17-6-12.
 */

public class SignUtil {
    private static final String TAG = SignUtil.class.getSimpleName();

    private static final String SIGN_MD5 = "760C87E5A95E199E5DAB8C263C895CB1";

    public static boolean checkSign(Context context, String reaperFilePath) {
        byte[] signByte = getSignFromApk(context, reaperFilePath);
        if (signByte == null)
            return false;
        String signString = getSignMd5(signByte);
        return signString.equals(SIGN_MD5);
    }

    private static byte[] getSignFromApk(Context context, String reaperFilePath) {
        if (context == null)
            return null;
        File reaperFile = new File(reaperFilePath);
        if (!reaperFile.exists())
            return null;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(reaperFilePath, PackageManager.GET_SIGNATURES);
        if (pi == null) {
            return null;
        }
        Signature[] signatures = pi.signatures;
        if (signatures.length > 0) {
//            ReaperLog.i(TAG, "sign = " + Arrays.toString(signatures[0].toByteArray()));
            return signatures[0].toByteArray();
        }
        return null;
    }

    private static String getSignMd5(byte[] sign) {
        String signString = EncryptUtils.encryptMD5ToString(sign);
//        ReaperLog.i(TAG, "sign to string: " + signString);
        return signString;
    }
}

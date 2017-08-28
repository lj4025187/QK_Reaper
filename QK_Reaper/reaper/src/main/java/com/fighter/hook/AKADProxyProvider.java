package com.fighter.hook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ak.android.provider.AKProvider;
import com.fighter.common.utils.ReaperLog;
import com.fighter.reaper.ReaperEnv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 代理聚效的AKProvider
 * Created by lichen on 17-8-15.
 */

public class AKADProxyProvider extends ContentProvider {

    private final static String TAG = "AKADProxyProvider";

    private final static String REAPER_AUTHORITIES_SUFFIX = ".reaper.provider.ReaperProxyProvider",
            AK_AD_AUTHORITIES_SUFFIX = ".akadsdkprovider";
    private static AKADProxyProvider mInstance;
    private static String sPackageName;
    private AKProvider mAkProvider;
    private Context mContext;
    private boolean mInitialized = false;

    public static AKADProxyProvider newInstance(Context context){
        if(mInstance == null)
            mInstance = new AKADProxyProvider(context);
        return mInstance;
    }

    private AKADProxyProvider(Context context) {
        mContext = context;
        sPackageName = mContext.getPackageName();
        mAkProvider = new AKProvider();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (TextUtils.isEmpty(uri.toString()) || TextUtils.isEmpty(mode)) {
            ReaperLog.e("ReaperProxyProvider", " uri and mode exists null");
            return super.openFile(uri, mode);
        }
        ReaperLog.i("proxy provider openFile:" + uri + " ," + mode);
        //fake authorities for provider
        ReaperLog.i(TAG, " uri before " + uri + " mode " + mode/* + " initialized " + mInitialized*/);
//        if (TextUtils.isEmpty(sPackageName))
//            sPackageName = mContext.getPackageName();
//        String replace = uri.toString().replace(sPackageName + REAPER_AUTHORITIES_SUFFIX,
//                sPackageName + AK_AD_AUTHORITIES_SUFFIX);
//        ReaperLog.i(TAG, " uri after " + replace + " mode " + mode + " initialized " + mInitialized);
//        //初始化一次，不重新调用attachInfo和onCreate
//        if (!mInitialized) {
//            mAkProvider.attachInfo(ReaperEnv.sContextProxy, null);
//            boolean create = mAkProvider.onCreate();
//            mInitialized = true;
//            ReaperLog.i("ReaperProxyProvider", " uri result " + replace + " create " + create + mInitialized);
//        }
//        //openFile
//        return mAkProvider.openFile(Uri.parse(replace), mode);

        String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sllak/apk/";
        ReaperLog.i(TAG, " open file " + uri + " mode " + mode);
        ParcelFileDescriptor pfd = null;
        String uriPath = uri.getPath();
        String filename = uriPath.substring(uriPath.lastIndexOf("/"));
        ReaperLog.e(TAG, " file name " + filename);
        File file = new File(apkPath, filename);
        ReaperLog.e(TAG, " file " + file.getAbsolutePath());
        if (file.exists()) {
            pfd = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY);
        }
        return pfd;
    }
}

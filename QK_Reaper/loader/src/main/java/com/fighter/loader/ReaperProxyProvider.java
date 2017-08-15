package com.fighter.loader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qiku.proguard.annotations.NoProguard;

import java.io.FileNotFoundException;


/**
 * reaper proxy provider
 *
 * Created by lichen on 17-6-27.
 */
@NoProguard
public class ReaperProxyProvider extends ContentProvider {
    private static final String TAG = "ReaperProxyProvider";

    @NoProguard
    private ContentProvider providerProxy;

    public ReaperProxyProvider() {
    }

    @Override
    public boolean onCreate() {
        return providerProxy == null? false : providerProxy.onCreate();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return providerProxy == null? null : providerProxy.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return providerProxy == null? null : providerProxy.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return providerProxy == null? null : providerProxy.insert(uri, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return providerProxy == null? 0 : providerProxy.delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return providerProxy == null? 0 : providerProxy.update(uri, values, selection, selectionArgs);
    }


    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        return providerProxy == null? super.openFile(uri, mode) : providerProxy.openFile(uri, mode);
    }
}

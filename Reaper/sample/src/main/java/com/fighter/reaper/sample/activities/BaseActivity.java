package com.fighter.reaper.sample.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.fighter.loader.ReaperApi;
import com.fighter.reaper.sample.SampleApp;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

/**
 * Created by Administrator on 2017/5/19.
 */

public class BaseActivity extends FragmentActivity implements Handler.Callback {

    public ReaperApi mReaperApi;
    protected Context mContext;
    protected Handler mMainHandler;

    private final static String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_SETTINGS
    };

    private final static int REQUEST_CODE = 5555;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        if (application instanceof SampleApp) {
            SampleApp app = (SampleApp) application;
            mReaperApi = app.getReaperApi();
        }
        mContext = application;
        mMainHandler = new Handler(Looper.getMainLooper(), this);
        checkPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        ArrayList<String> noGrantedPermission = new ArrayList<>();
        int length = REQUESTED_PERMISSIONS.length;
        for (int i = 0; i < length; i++) {
            String permission = REQUESTED_PERMISSIONS[i];
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PERMISSION_DENIED)
                noGrantedPermission.add(permission);
        }
        if(noGrantedPermission.isEmpty()) return;
        String[] needRequests = noGrantedPermission.toArray(new String[noGrantedPermission.size()]);
        ActivityCompat.requestPermissions(this, needRequests, REQUEST_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE || permissions.length == 0) return;
        if(Arrays.asList(permissions).contains(Manifest.permission.WRITE_SETTINGS))
                checkWriteSettings();
        if(Arrays.asList(grantResults).contains(PERMISSION_DENIED)) {
            startPermissionSettings();
        }
    }

    private void startPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkWriteSettings() {
        if(!Settings.System.canWrite(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return true;
    }
}

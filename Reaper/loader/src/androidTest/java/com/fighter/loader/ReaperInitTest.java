package com.fighter.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.patch.ReaperPatchVersion;
import com.fighter.utils.LoaderLog;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by huayang on 17-5-8.
 */

@RunWith(AndroidJUnit4.class)
public class ReaperInitTest {

    public static final String TAG = ReaperInitTest.class.getSimpleName();
    public static final String PKG_NAME = "com.fighter.reaper";
    private static final String REAPER = "1.0.3.rr";
    private static final String REAPER_DIR_SDCARD =
            Environment.getExternalStorageDirectory().toString() +
                    File.separator + ".reapers" + File.separator;
    private static final String REAPER_DIR_DOWNLOAD = REAPER_DIR_SDCARD + "download" + File.separator + REAPER;

    @Before
    public void preTest() {
        LoaderLog.e(TAG, "preTest ... ");
//        Context context = InstrumentationRegistry.getContext();
//        AssetManager assetManager = context.getAssets();
//        File file = new File("/mnt/sdcard/reaper.apk");
//        try {
//            InputStream is = assetManager.open("ads/reaper.apk");
//            Assert.assertNotNull(is);
//            FileOutputStream fos = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//            int length = 0;
//            while ((length = is.read(buffer)) > 0) {
//                fos.write(buffer, 0, length);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
    }

    public void afterTest() {
        LoaderLog.e(TAG, "afterTest ... ");
//        Context context = InstrumentationRegistry.getContext();
//        String packageName = "com.fighter.reaper";
//        Uri uri = Uri.parse("package:" + packageName);
//        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
//        context.startActivity(intent);
    }

    @Test
    public void testInit() {
        Context context = InstrumentationRegistry.getContext();
        AssetManager assetManager = context.getAssets();
        boolean hasRR = false;
        boolean hasInstalled = false;
        boolean hasDownloaded = false;

        try {
            AssetFileDescriptor afd = assetManager.openFd("ads/reaper.rr");
            hasRR = afd != null;
        } catch (IOException e) {
            e.printStackTrace();
            hasRR = false;
        }

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(PKG_NAME, PackageManager.GET_META_DATA);
            hasInstalled = ai != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            hasInstalled = false;
        }

        File file = new File(REAPER_DIR_DOWNLOAD);
        if (file.exists()) {
            hasDownloaded = true;
        }

        if (!hasRR && !hasInstalled && !hasDownloaded) {
            LoaderLog.e(TAG, "Cant find any REAPER !");
            return;
        }

        //disableNetworkCheck();

        ReaperApi reaperApi = ReaperInit.init(context);
        LoaderLog.i(TAG, "reaperApi : " + reaperApi);
        Assert.assertNotNull(reaperApi);
        LoaderLog.i(TAG, "reaperApi : " + reaperApi.requestSplashAds("SplashAd", 1000));
        SystemClock.sleep(50000);
    }

    private void disableNetworkCheck() {
        try {
            Field field = ReaperInit.class.getDeclaredField("QUERY_SERVER");
            if (field == null)
                return;
            field.setAccessible(true);
            field.set(null, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testComparePatchVersion() throws Exception {

        Constructor constructor = ReaperPatchVersion.class.getDeclaredConstructor(ClassLoader.class);
        Assert.assertNotNull(constructor);
        constructor.setAccessible(true);

        ReaperPatchVersion first = (ReaperPatchVersion) constructor.newInstance(ClassLoader.getSystemClassLoader());
        first.release = 1;
        first.second = 2;
        first.revision = 3;
        first.suffix = "-beta"; //change value to see result
        ReaperPatchVersion second = (ReaperPatchVersion) constructor.newInstance(ClassLoader.getSystemClassLoader());
        second.release = 1;
        second.second = 2;
        second.revision = 3;
        second.suffix = "-stable"; //change value to see result

        Method compareMethod = ReaperInit.class
                .getDeclaredMethod("comparePatchVersion", ReaperPatchVersion.class, ReaperPatchVersion.class);
        Assert.assertNotNull(compareMethod);
        compareMethod.setAccessible(true);
        Object o = compareMethod.invoke(null, first, second);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Integer);
        LoaderLog.i(TAG, "comparePatchVersion : " + o);
    }
}

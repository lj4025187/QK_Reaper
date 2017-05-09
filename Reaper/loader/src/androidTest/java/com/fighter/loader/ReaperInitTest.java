package com.fighter.loader;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.patch.ReaperPatchVersion;
import com.fighter.utils.Slog;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by huayang on 17-5-8.
 */

@RunWith(AndroidJUnit4.class)
public class ReaperInitTest {

    public static final String TAG = ReaperInitTest.class.getSimpleName();

    @Before
    public void preTest() {
        Slog.e(TAG, "preTest ... ");
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
        Slog.e(TAG, "afterTest ... ");
//        Context context = InstrumentationRegistry.getContext();
//        String packageName = "com.fighter.reaper";
//        Uri uri = Uri.parse("package:" + packageName);
//        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
//        context.startActivity(intent);
    }

    @Test
    public void testInit() throws Exception {

        Context context = InstrumentationRegistry.getContext();
        ReaperApi reaperApi = ReaperInit.init(context);

        Slog.i(TAG, "reaperApi : " + reaperApi);
        //Assert.assertNotNull(reaperApi);
        //Slog.i(TAG, "reaperApi : " + reaperApi.requestSplashAds("SplashAd", 1000));
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
        Slog.i(TAG, "comparePatchVersion : " + o);
    }
}

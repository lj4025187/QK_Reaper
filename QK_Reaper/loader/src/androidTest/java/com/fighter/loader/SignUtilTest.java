package com.fighter.loader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.utils.SignUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Test RC4 encrypt and decrypt
 *
 * Created by zhangjg on 17-5-10.
 */

@RunWith(AndroidJUnit4.class)
public class SignUtilTest {

    private static final String TAG = SignUtilTest.class.getSimpleName();

    @Test
    public void testCheckSign() throws Exception {
        String testFilePath = "/sdcard/test.dex";
        Context context = InstrumentationRegistry.getTargetContext();
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            Assert.assertTrue(SignUtil.checkSign(context, testFilePath));
        }
    }

}

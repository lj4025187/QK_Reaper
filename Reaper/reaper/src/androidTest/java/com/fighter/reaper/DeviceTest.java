package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.common.Device;
import com.fighter.common.utils.ReaperLog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DeviceTest {
    private static final String TAG = DeviceTest.class.getSimpleName();
    @Test
    public void useGetTime() throws Exception {
        String test = Device.getCurrentLocalTime();
        ReaperLog.i(TAG, "current time = " + test);
        Assert.assertNotNull(test);
    }

    @Test
    public void useGetArea() throws Exception {
        String test = Device.getArea();
        ReaperLog.i(TAG, "Area = " + test);
        Assert.assertNotNull(test);
    }

    @Test
    public void useGetMcc() throws Exception {
        String test = Device.getMcc(InstrumentationRegistry.getTargetContext());
        ReaperLog.i(TAG, "mcc = " + test);
    }

    @Test
    public void useGetLanguage() throws Exception {
        String test = Device.getLocalLanguage();
        ReaperLog.i(TAG, "language = " + test);
        Assert.assertNotNull(test);
    }

    @Test
    public void useGetDeviceChannel() throws Exception {
        String test = Device.getDeviceChannel();
        ReaperLog.i(TAG, "channel = " + test);
    }

    @Test
    public void useNetworkString() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        String netTypeString = Device.getNetworkTypeString(context);
        ReaperLog.i(TAG, "NetworkTypeString = " + netTypeString);
        Assert.assertNotNull(netTypeString);
    }

    @Test
    public void useformatMac() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        String formatMac = Device.getFormatMac(context);

        // just log it
        Log.d(TAG, "FormatMac : " + formatMac);
    }

}

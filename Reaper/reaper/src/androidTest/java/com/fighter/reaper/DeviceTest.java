package com.fighter.reaper;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.common.Device;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DeviceTest {
    private static final String TAG = DeviceTest.class.getSimpleName();
    @Test
    public void useGetTime() throws Exception {
        String test = Device.getCurrentLocalTime();
        Log.d(TAG, "current time = " + test);
        Assert.assertNotNull(test);
    }

    @Test
    public void useGetArea() throws Exception {
        String test = Device.getArea();
        Log.d(TAG, "Area = " + test);
        Assert.assertNotNull(test);
    }
}

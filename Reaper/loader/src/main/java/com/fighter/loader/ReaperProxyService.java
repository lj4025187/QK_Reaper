package com.fighter.loader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.qiku.proguard.annotations.NoProguard;

/**
 * reaper proxy service
 *
 * Created by lichen on 17-6-27.
 */
@NoProguard
public class ReaperProxyService extends Service {
    // the service is preset
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

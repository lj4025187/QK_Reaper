package com.fighter.loader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * reaper proxy service
 *
 * Created by lichen on 17-6-27.
 */

public class ReaperProxyService extends Service {
    // the service is preset
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

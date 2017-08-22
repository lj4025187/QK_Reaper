package com.fighter.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * reaper remote service
 *
 * Created by lichen on 17-6-27.
 */

public class ReaperRemoteService extends Service {
    // the service is reaper remote
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

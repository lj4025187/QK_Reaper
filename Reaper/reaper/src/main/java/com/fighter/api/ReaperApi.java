package com.fighter.api;

import com.qiku.proguard.annotations.NoProguard;

/**
 * Created by wxthon on 5/5/17.
 */

@NoProguard
public class ReaperApi {

    //test start

    @NoProguard
    public String requestSplashAds(String name, int time) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Requested an Ad for you, your params are : " + name + "; " + time;
    }

    //test end

}

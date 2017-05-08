package com.fighter.api;

/**
 * Created by wxthon on 5/5/17.
 */

public class ReaperApi {

    //test start

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

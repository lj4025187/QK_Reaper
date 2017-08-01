package com.fighter.reaper.sample.utils;

import android.view.View;

/**
 * Created by liujia on 6/1/17.
 */

public class ViewUtils {

    public static void setViewVisibility(View view, int visibility) {
        if(view == null)
            return;
        view.setVisibility(visibility);
    }
}

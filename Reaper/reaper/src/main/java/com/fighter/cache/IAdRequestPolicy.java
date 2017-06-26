package com.fighter.cache;

import com.fighter.config.ReaperAdSense;


/**
 * Created by lichen on 17-6-7.
 */

public interface IAdRequestPolicy {
    ReaperAdSense next(int tryTime);
}


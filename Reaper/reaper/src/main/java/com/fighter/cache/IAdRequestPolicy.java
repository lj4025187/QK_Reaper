package com.fighter.cache;

import com.fighter.config.ReaperAdSense;

import java.util.List;

/**
 * Created by lichen on 17-6-7.
 */

public interface IAdRequestPolicy {
    List<ReaperAdSense> generateList();
}


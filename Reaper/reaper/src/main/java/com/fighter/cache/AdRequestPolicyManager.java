package com.fighter.cache;

import android.content.Context;

import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lichen on 17-6-7.
 */

public class AdRequestPolicyManager {
    private static final String TAG = AdRequestPolicyManager.class.getSimpleName();

    private static AdRequestFirst sFistPolicy = AdRequestFirst.getInstance();
    private static AdRequestLoop sLoopPolicy = AdRequestLoop.getInstance();
    private static AdRequestWeight sWeightPolicy = AdRequestWeight.getInstance();

    public static IAdRequestPolicy getAdRequestPolicy(Context context, String posId) {
        ReaperAdvPos reaperAdvPos = ReaperConfigManager.getReaperAdvPos(context, posId);
        if(reaperAdvPos == null) return null;
        String exposure = reaperAdvPos.adv_exposure;
        String mode = getMode(exposure);
        if (mode == null)
            return null;
        switch (mode) {
            case "first":
                sFistPolicy.setContext(context);
                sFistPolicy.setPosId(posId);
                return sFistPolicy;
            case "loop":
                sLoopPolicy.setContext(context);
                sLoopPolicy.setPosId(posId);
                return sLoopPolicy;
            case "weight":
                sWeightPolicy.setContext(context);
                sWeightPolicy.setPosId(posId);
                sWeightPolicy.setWeight(getWeight(exposure));
                return sWeightPolicy;
            default:
                ReaperLog.e(TAG, "not match policy!");
        }
        return null;
    }

    private static String getMode(String exposure) {
        if (exposure == null)
            return null;
        int index = exposure.indexOf(':');
        if (index == -1) {
            return exposure;
        } else {
            return exposure.substring(0, index);
        }
    }

    private static List<String> getWeight(String exposure) {
        if (exposure == null)
            return null;
        String [] weights = exposure.split(":");
        List<String> weightList = new ArrayList<>();
        for (int i =0; i < weights.length; i++) {
            if (i == 0)
                continue;
            weightList.add(weights[i]);
        }
        return weightList;
    }
}

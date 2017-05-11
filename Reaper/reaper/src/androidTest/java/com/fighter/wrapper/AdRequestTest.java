package com.fighter.wrapper;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AdRequestTest {
    private static final String TAG = AdRequestTest.class.getSimpleName();

    @Test
    public void testBuild() {
        String appId = "appid";
        String posId = "posId";
        int adType = AdType.TYPE_NATIVE;
        ArrayList<String> keyWords = new ArrayList<>();
        String keyWord1 = "key1";
        keyWords.add(keyWord1);
        int width = 10;
        int height = 10;
        String key = "test_key";
        String keyValue = "test_key_value";

        AdRequest.Builder builder = new AdRequest.Builder();
        builder.appId(appId)
                .adPositionId(posId)
                .adType(adType)
                .adKeyWords(keyWords)
                .adWidth(width)
                .adHeight(height)
                .adExtra(key, keyValue);
        AdRequest adRequest = builder.create();

        assertTrue(adRequest.getAppId().equals(appId));
        assertTrue(adRequest.getAdPositionId().equals(posId));
        assertTrue(adRequest.getAdType() == adType);
        assertTrue(adRequest.getAdKeyWords().get(0).equals(keyWord1));
        assertTrue(adRequest.getAdWidth() == width);
        assertTrue(adRequest.getAdHeight() == height);
        assertTrue(adRequest.getAdExtras().get(key).equals(keyValue));
    }
}

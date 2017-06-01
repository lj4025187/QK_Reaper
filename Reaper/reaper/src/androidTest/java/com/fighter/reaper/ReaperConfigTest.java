package com.fighter.reaper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;
import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfig;
import com.fighter.config.db.ReaperConfigDB;
import com.fighter.config.ReaperConfigHttpHelper;
import com.fighter.config.ReaperConfigManager;
import com.fighter.config.ReaperConfigRequestBody;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Test reaper config
 *
 * Created by zhangjigang on 2017/5/12.
 */

@RunWith(AndroidJUnit4.class)
public class ReaperConfigTest {

    private static final String TAG = ReaperConfigTest.class.getSimpleName();

    private static String RESPONSE =
            "{" +
                    "\"result\": \"ok\"," +
                    "\"reason\": \"\"," +
                    "\"next_time\": \"28800\"," +
                    "\"pos_ids\": [" +
                    "{" +
                    "\"pos_id\": \"1\"," +
                    "\"adv_type\":\"banner\"," +
                    "\"adv_exposure\": \"first\"," +
                    "\"adsenses\": [" +
                    "{" +
                    "\"ads_name\": \"juxiao\"," +
                    "\"expire_time\": \"1800\"," +
                    "\"priority\": \"10\"," +
                    "\"ads_appid\": \"100001\"," +
                    "\"ads_key\": \"adbsjmemsfm\"," +
                    "\"ads_posid\": \"10001\"," +
                    "\"max_adv_num\": \"10\"," +
                    "\"adv_size_type\": \"pixel\"," +
                    "\"adv_real_size\": \"640*100\"" +
                    "}," +
                    "{" +
                    "\"ads_name\": \"guangdiantong\"," +
                    "\"expire_time\": \"1800\"," +
                    "\"priority\": \"3\"," +
                    "\"ads_appid\": \"1104241296\"," +
                    "\"ads_key\": \"adbsjmemsfm\"," +
                    "\"ads_posid\": \"6050305154328807\"," +
                    "\"max_adv_num\": \"10\"," +
                    "\"adv_size_type\": \"pixel\"," +
                    "\"adv_real_size\": \"640*100\"" +
                    "}," +
                    "{" +
                    "\"ads_name\": \"baidu\"," +
                    "\"expire_time\": \"1800\"," +
                    "\"priority\": \"10\"," +
                    "\"ads_appid\": \"0\"," +
                    "\"ads_key\": \"adbsjmemsfm\"," +
                    "\"ads_posid\": \"128\"," +
                    "\"max_adv_num\": \"10\"," +
                    "\"adv_size_type\": \"pixel\"," +
                    "\"adv_real_size\": \"640*100\"" +
                    "}," +
                    "]" +
                    "}," +
                    "{" +
                    "\"pos_id\": \"2\"," +
                    "\"adv_type\":\"plugin\"," +
                    "\"adv_exposure\": \"first\"," +
                    "\"adsenses\": [" +
                    "{" +
                    "\"ads_name\": \"guangdiantong\"," +
                    "\"expire_time\": \"1800\"," +
                    "\"priority\": \"3\"," +
                    "\"ads_appid\": \"1104241296\"," +
                    "\"ads_key\": \"adbsjmemsfm\"," +
                    "\"ads_posid\": \"1060308114529681\"," +
                    "\"max_adv_num\": \"10\"," +
                    "\"adv_size_type\": \"pixel\"," +
                    "\"adv_real_size\": \"640*500\"" +
                    "}" +
                    "]" +
                    "}" +
                    "]" +
                    "}";

    /**
     * Test json to ReaperConfigRequestBody and ReaperConfigRequestBody to json
     *
     * @throws Exception
     */
    @Test
    public void testReaperConfigRequestBody() throws Exception {

        ReaperLog.i(TAG, "##################### testReaperConfigRequestBody ####################");


        Context context = InstrumentationRegistry.getTargetContext();

        // test empty object to json
        ReaperConfigRequestBody emptyObj = new ReaperConfigRequestBody();
        String json = emptyObj.toJson();
        Log.e(TAG, "empty ReaperConfigRequestBody to json : " + json); // empty json {}

        // test obj to json
        ReaperConfigRequestBody obj = ReaperConfigRequestBody.createTestInstance();
        json = obj.toJson();
        Log.e(TAG, "ReaperConfigRequestBody to json : " + json);

        // test json to obj
        ReaperConfigRequestBody fromJson = ReaperConfigRequestBody.fromJson(json);
        Log.e(TAG, "json to ReaperConfigRequestBody : " + fromJson);

        // test create ReaperConfigRequestBody for package
        ReaperConfigRequestBody objForPackage = ReaperConfigRequestBody.create(context, context.getPackageName());
        Log.e(TAG, "create ReaperConfigRequestBody for package : " + objForPackage);

    }

    @Test
    public void testParseResponseBody () throws Exception {

        ReaperLog.i(TAG, "##################### testParseResponseBody ####################");

        Context context = InstrumentationRegistry.getTargetContext();
        String key = ReaperConfig.TEST_SALT + ReaperConfig.TEST_APPKEY;
        IRC4 rc4 = RC4Factory.create(key);
        byte[] encrypt = rc4.encrypt(RESPONSE.getBytes());
        // just log it
        ReaperConfigHttpHelper.parseResponseBody(context, encrypt, key);
    }

    @Test
    public void testDatabase() throws Exception {

        ReaperLog.i(TAG, "##################### testDatabase ####################");

        Context context = InstrumentationRegistry.getTargetContext();

        String key = ReaperConfig.TEST_SALT + ReaperConfig.TEST_APPKEY;
        IRC4 rc4 = RC4Factory.create(key);
        byte[] encrypt = rc4.encrypt(RESPONSE.getBytes());
        List<ReaperAdvPos> posList
                = ReaperConfigHttpHelper.parseResponseBody(context, encrypt, key);

        ReaperConfigDB db = ReaperConfigDB.getInstance(context);
        db.saveReaperAdvPos(posList);

        posList = ReaperConfigManager.getAllReaperAdvPos(context);
        Assert.assertEquals(2, posList.size());

        ReaperAdvPos pos = db.queryAdvPos("1");
        Assert.assertEquals(pos.adv_type, "banner");

        ReaperAdSense sense = db.queryAdSense("2");
        Assert.assertEquals(sense.priority, "3");
    }

    @Test
    public void testFetchConfigFromServer() throws Exception {

        ReaperLog.i(TAG, "##################### testFetchConfigFromServer ####################");

        Context context = InstrumentationRegistry.getTargetContext();

        boolean fetchSuccess = ReaperConfigManager.fetchReaperConfigFromServer(
                context, context.getPackageName(),
                ReaperConfig.TEST_SALT,
                ReaperConfig.TEST_APPKEY,
                ReaperConfig.TEST_APPID);
        ReaperLog.i(TAG, "##################### testFetchConfigFromServer ####################" + fetchSuccess);
    }
}

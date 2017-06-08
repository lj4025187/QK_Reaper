package com.fighter.reaper.sample.utils;

/**
 * Created by liujia on 6/6/17.
 */

public class ResponseGenerator {


    public static String generate(String qihooType, String tencentType, String baiduType) {
        //pos_id == 1 聚效广告
        //pos_id == 2 广点通其他类型广告
        //pos_id == 3 广点通insert类型广告
        //pos_id == 4 百度广告（目前无数据返回）
        return "{" +
                    "\"result\": \"ok\"," +
                    "\"reason\": \"\"," +
                    "\"next_time\": \"100\"," +
                    "\"pos_ids\": [" +
                        "{" +
                            "\"pos_id\": \"1\"," +
//                            "\"adv_type\":\"banner_adv\"," +
                            "\"adv_type\":\"" + (qihooType == null ? "banner_adv" : qihooType) + "\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"jx\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"10\"," +
                                    "\"ads_appid\": \"100001\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"10001\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"640*100\"" +
                                "}," +
//                                "{" +
//                                    "\"ads_name\": \"baidu\"," +
//                                    "\"expire_time\": \"1800\"," +
//                                    "\"priority\": \"10\"," +
//                                    "\"ads_appid\": \"0\"," +
//                                    "\"ads_key\": \"adbsjmemsfm\"," +
//                                    "\"ads_posid\": \"128\"," +
//                                    "\"max_adv_num\": \"10\"," +
//                                    "\"adv_size_type\": \"pixel\"," +
//                                    "\"adv_real_size\": \"640*100\"" +
//                                "}," +
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"2\"," +
//                            "\"adv_type\":\"insert_adv\"," +
                            "\"adv_type\":\"" + (tencentType == null ? "insert_adv" : tencentType) + "\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"gdt\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"3\"," +
                                    "\"ads_appid\": \"1104241296\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
//                                    "\"ads_posid\": \"1060308114529681\"," +
                                    "\"ads_posid\": \"6050305154328807\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
//                                    "\"adv_real_size\": \"640*500\"" +
                                    "\"adv_real_size\": \"240*38\"" +
                                "}" +
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"3\"," +
                            "\"adv_type\":\"insert_adv\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"gdt\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"3\"," +
                                    "\"ads_appid\": \"1104241296\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"1060308114529681\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"600*500\"" +
                                "}" +
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"4\"," +
                            "\"adv_type\":\"" + baiduType + "\"," +
                            "\"adv_exposure\": \"first\"," +
                            "\"adsenses\": [" +
                                "{" +
                                    "\"ads_name\": \"baidu\"," +
                                    "\"expire_time\": \"1800\"," +
                                    "\"priority\": \"10\"," +
                                    "\"ads_appid\": \"0\"," +
                                    "\"ads_key\": \"adbsjmemsfm\"," +
                                    "\"ads_posid\": \"128\"," +
                                    "\"max_adv_num\": \"10\"," +
                                    "\"adv_size_type\": \"pixel\"," +
                                    "\"adv_real_size\": \"600*300\"" +
                                "}" +
                            "]" +
                        "}" +
                    "]" +
                "}";
    }
}

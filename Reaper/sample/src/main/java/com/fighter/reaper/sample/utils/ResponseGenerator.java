package com.fighter.reaper.sample.utils;

/**
 * Created by liujia on 6/6/17.
 */

public class ResponseGenerator {


    public static String generate() {
        return "{" +
                    "\"result\": \"ok\"," +
                    "\"reason\": \"\"," +
                    "\"next_time\": \"100\"," +
                    "\"pos_ids\": [" +
                        //pos_id == 1 聚效广告insert
                        //pos_id == 2 聚效广告banner
                        //pos_id == 3 聚效广告openapp
                        //pos_id == 4 聚效广告feed
                        //pos_id == 5 聚效广告video
                        //pos_id == 6 聚效广告original
                        "{" +
                            "\"pos_id\": \"1\"," +
                            "\"adv_type\":\"insert_adv\"," +
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
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"2\"," +
                            "\"adv_type\":\"banner_adv\"," +
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
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"3\"," +
                            "\"adv_type\":\"openapp_adv\"," +
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
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"4\"," +
                            "\"adv_type\":\"feed_adv\"," +
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
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"5\"," +
                            "\"adv_type\":\"video_adv\"," +
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
                            "]" +
                        "}," +
                        "{" +
                            "\"pos_id\": \"6\"," +
                            "\"adv_type\":\"original_adv\"," +
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
                            "]" +
                        "}," +

                        //pos_id == 7  广点通广告insert
                        //pos_id == 8  广点通广告banner
                        //pos_id == 9  广点通广告openapp
                        //pos_id == 10 广点通广告feed
                        //pos_id == 11 广点通广告video
                        //pos_id == 12 广点通广告original
                        "{" +
                            "\"pos_id\": \"7\"," +
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
                            "\"pos_id\": \"8\"," +
                            "\"adv_type\":\"banner_adv\"," +
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
                            "\"pos_id\": \"9\"," +
                            "\"adv_type\":\"openapp_adv\"," +
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
                            "\"pos_id\": \"10\"," +
                            "\"adv_type\":\"feed_adv\"," +
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
                            "\"pos_id\": \"11\"," +
                            "\"adv_type\":\"video_adv\"," +
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
                            "\"pos_id\": \"12\"," +
                            "\"adv_type\":\"original_adv\"," +
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

                        //pos_id == 13 百度广告insert（目前无数据返回）
                        //pos_id == 14 百度广告banner（目前无数据返回）
                        //pos_id == 15 百度广告openapp（目前无数据返回）
                        //pos_id == 16 百度广告feed（目前无数据返回）
                        //pos_id == 17 百度广告video（目前无数据返回）
                        //pos_id == 18 百度广告original（目前无数据返回）
                        "{" +
                            "\"pos_id\": \"13\"," +
                            "\"adv_type\":\"insert_adv\"," +
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
                        "}," +
                         "{" +
                            "\"pos_id\": \"14\"," +
                            "\"adv_type\":\"banner_adv\"," +
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
                        "}," +
                         "{" +
                            "\"pos_id\": \"15\"," +
                            "\"adv_type\":\"openapp_adv\"," +
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
                        "}," +
                         "{" +
                            "\"pos_id\": \"16\"," +
                            "\"adv_type\":\"feed_adv\"," +
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
                        "}," +
                         "{" +
                            "\"pos_id\": \"17\"," +
                            "\"adv_type\":\"video_adv\"," +
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
                        "}," +
                         "{" +
                            "\"pos_id\": \"18\"," +
                            "\"adv_type\":\"original_adv\"," +
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

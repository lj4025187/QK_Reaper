package com.fighter.config;

/**
 * 广告联盟配置信息
 *
 * Created by zhangjigang on 2017/5/12.
 */

public class ReaperAdSense {

    private String pos_id;

    public void setPosId(String pos_id) {
        this.pos_id = pos_id;
    }

    public String getPosId() {
        return pos_id;
    }


    /**
     * Do not modify the filed names
     * It is used map to json
     */
    public String ads_name;
    public String expire_time;
    public String ads_appid;
    public String ads_key;
    public String ads_posid;
    public String max_adv_num;
    public String adv_size_type;
    public String adv_real_size;


    @Override
    public String toString() {
        return "ReaperAdSense{" +
                "pos_id='" + pos_id + '\'' +
                ", ads_name='" + ads_name + '\'' +
                ", expire_time='" + expire_time + '\'' +
                ", ads_appid='" + ads_appid + '\'' +
                ", ads_key='" + ads_key + '\'' +
                ", ads_posid='" + ads_posid + '\'' +
                ", max_adv_num='" + max_adv_num + '\'' +
                ", adv_size_type='" + adv_size_type + '\'' +
                ", adv_real_size='" + adv_real_size + '\'' +
                '}';
    }
}

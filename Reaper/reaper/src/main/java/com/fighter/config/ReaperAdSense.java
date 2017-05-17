package com.fighter.config;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fighter.config.db.ReaperConfigDBHelper;

/**
 * 广告联盟配置信息
 *
 * Created by zhangjigang on 2017/5/12.
 */

public class ReaperAdSense implements Comparable{

    private String pos_id;

    public void setPosId(String pos_id) {
        this.pos_id = pos_id;
    }

    public String getPosId() {
        return pos_id;
    }


    /**
     * Do not modify the filed names
     * It is used map to json and database
     */
    public String ads_name;
    public String expire_time;
    public String priority;
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
                ", priority ='" + priority + '\'' +
                ", ads_appid='" + ads_appid + '\'' +
                ", ads_key='" + ads_key + '\'' +
                ", ads_posid='" + ads_posid + '\'' +
                ", max_adv_num='" + max_adv_num + '\'' +
                ", adv_size_type='" + adv_size_type + '\'' +
                ", adv_real_size='" + adv_real_size + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o == null) {
            return 0;
        }

        if (!(o instanceof ReaperAdSense)) {
            return 0;
        }
        ReaperAdSense other = (ReaperAdSense) o;
        if (TextUtils.isDigitsOnly(this.priority) && TextUtils.isDigitsOnly(other.priority)) {
            return Integer.parseInt(this.priority) - Integer.parseInt(other.priority);
        } else {
            return 0;
        }
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_POS_ID, pos_id);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADS_NAME, ads_name);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_EXPIRE_TIME, expire_time);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_PRIORITY, priority);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADS_APPID, ads_appid);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADS_KEY, ads_key);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADS_POSID, ads_posid);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_MAX_ADV_NUM, max_adv_num);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADV_SIZE_TYPE, adv_size_type);
        cv.put(ReaperConfigDBHelper.SENSE_COLUMN_ADB_REAL_SIZE, adv_real_size);
        return cv;
    }
}

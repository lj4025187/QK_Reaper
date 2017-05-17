package com.fighter.config.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fighter.common.utils.ReaperLog;

/**
 * Created by zhangjigang on 2017/5/13.
 */

public class ReaperConfigDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "ReaperConfigDBHelper";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "reaper_adv_config.db";

    /**
     * Database reaper_adv_pos table structure
     */
    public static final String TABLE_POS = "reaper_adv_pos";
    public static final String POS_COLUMN_POS_ID = "pos_id";
    public static final String POS_COLUMN_ADV_TYPE = "adv_type";
    public static final String POS_COLUMN_ADV_EXPOSURE = "adv_exposure";


    /**
     * Database reaper_adv_sense table structure
     */
    public static final String TABLE_SENSE = "reaper_adv_sense";
    public static final String SENSE_COLUMN_POS_ID = POS_COLUMN_POS_ID;
    public static final String SENSE_COLUMN_ADS_NAME = "ads_name";
    public static final String SENSE_COLUMN_EXPIRE_TIME = "expire_time";
    public static final String SENSE_COLUMN_PRIORITY = "priority";
    public static final String SENSE_COLUMN_ADS_APPID = "ads_appid";
    public static final String SENSE_COLUMN_ADS_KEY = "ads_key";
    public static final String SENSE_COLUMN_ADS_POSID = "ads_posid";
    public static final String SENSE_COLUMN_MAX_ADV_NUM = "max_adv_num";
    public static final String SENSE_COLUMN_ADV_SIZE_TYPE = "adv_size_type";
    public static final String SENSE_COLUMN_ADB_REAL_SIZE = "adv_real_size";


    private static final String CREATE_TABLE_POS = "CREATE TABLE " + TABLE_POS +
            " ( " +
            POS_COLUMN_POS_ID + " TEXT PRIMARY KEY , " +
            POS_COLUMN_ADV_TYPE + " TEXT , " +
            POS_COLUMN_ADV_EXPOSURE + " TEXT " +
            ");";

    private static final String CREATE_TABLE_SENSE = "CREATE TABLE " + TABLE_SENSE +
            " ( " +
            SENSE_COLUMN_POS_ID + " TEXT , " +
            SENSE_COLUMN_ADS_NAME + " TEXT , " +
            SENSE_COLUMN_EXPIRE_TIME + " TEXT , " +
            SENSE_COLUMN_PRIORITY + " TEXT , " +
            SENSE_COLUMN_ADS_APPID + " TEXT , " +
            SENSE_COLUMN_ADS_KEY + " TEXT , " +
            SENSE_COLUMN_ADS_POSID + " TEXT , " +
            SENSE_COLUMN_MAX_ADV_NUM + " TEXT , " +
            SENSE_COLUMN_ADV_SIZE_TYPE + " TEXT , " +
            SENSE_COLUMN_ADB_REAL_SIZE + " TEXT " +
            ");";


    private static ReaperConfigDBHelper sInstance;

    public synchronized static ReaperConfigDBHelper getInstance(Context context) {
        if (sInstance == null) {
            if (context == null) {
                throw new IllegalArgumentException("context is null !!!");
            }
            sInstance = new ReaperConfigDBHelper(context);
        }
        return sInstance;
    }

    private ReaperConfigDBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ReaperLog.i(TAG, "onCreate, start create table");
        db.execSQL(CREATE_TABLE_POS);
        db.execSQL(CREATE_TABLE_SENSE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ReaperLog.i(TAG, "onUpgrade, start create table");
    }
}

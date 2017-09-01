package com.fighter.config.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fighter.common.utils.ReaperLog;
import com.fighter.config.ReaperAdSense;
import com.fighter.config.ReaperAdvPos;
import com.fighter.config.ReaperConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.fighter.config.db.ReaperConfigDBHelper.POS_COLUMN_ADV_EXPOSURE;
import static com.fighter.config.db.ReaperConfigDBHelper.POS_COLUMN_ADV_TYPE;
import static com.fighter.config.db.ReaperConfigDBHelper.POS_COLUMN_POS_ID;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADB_REAL_SIZE;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADS_APPID;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADS_APP_KEY;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADS_NAME;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADS_POSID;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_ADV_SIZE_TYPE;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_EXPIRE_TIME;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_MAX_ADV_NUM;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_POS_ID;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_PRIORITY;
import static com.fighter.config.db.ReaperConfigDBHelper.SENSE_COLUMN_SILENT_INSTALL;
import static com.fighter.config.db.ReaperConfigDBHelper.TABLE_POS;
import static com.fighter.config.db.ReaperConfigDBHelper.TABLE_SENSE;

/**
 * This is the config database to save
 * configs request from server
 *
 * Created by zhangjg on 17-5-8.
 */
public class ReaperConfigDB {

    private static final String TAG = "ReaperConfigDB";

    private static ReaperConfigDB sInstance;

    private ReaperConfigDB (Context context) {
        mDBHelper = ReaperConfigDBHelper.getInstance(context);
    }

    public synchronized static ReaperConfigDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ReaperConfigDB(context);
        }
        return sInstance;
    }

    private ReaperConfigDBHelper mDBHelper;
    private ReentrantReadWriteLock mRWLock = new ReentrantReadWriteLock();

    public void saveReaperAdvPos(List<ReaperAdvPos> posList) {
        mRWLock.writeLock().lock();
        try {
            saveReaperAdvPosInner(posList);
        } finally {
            mRWLock.writeLock().unlock();
        }
    }

    public ReaperAdvPos queryAdvPos(String posId) {
        mRWLock.readLock().lock();
        try {
            return queryAdvPosInner(posId);
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    public List<ReaperAdvPos> queryAllAdvPos() {
        mRWLock.readLock().lock();
        try {
            return queryAllAdvPosInner();
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    public ReaperAdSense queryAdSense(String posId) {
        mRWLock.readLock().lock();
        try {
            return queryAdSenseInner(posId);
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    public List<ReaperAdSense> queryAllAdSense(String posId) {
        mRWLock.readLock().lock();
        try {
            return queryAllAdSenseInner(posId);
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    private List<ReaperAdvPos> queryAllAdvPosInner() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String[] columns = new String[] {POS_COLUMN_POS_ID,
                POS_COLUMN_ADV_TYPE,
                POS_COLUMN_ADV_EXPOSURE};

        Cursor cursor = db.query(TABLE_POS, columns, null, null, null, null, null);
        try {
            if (cursor == null) {
                return null;
            }
            ArrayList<ReaperAdvPos> posList = new ArrayList<>();
            while (cursor.moveToNext()) {
                ReaperAdvPos pos = new ReaperAdvPos();
                pos.pos_id = cursor.getString(cursor.getColumnIndex(POS_COLUMN_POS_ID));
                pos.adv_type = cursor.getString(cursor.getColumnIndex(POS_COLUMN_ADV_TYPE));
                pos.adv_exposure = cursor.getString(cursor.getColumnIndex(POS_COLUMN_ADV_EXPOSURE));
                posList.add(pos);
            }
            return posList;
        } finally {
            cursor.close();
            //db.close();
        }
    }

    private List<ReaperAdSense> queryAllAdSenseInner (String posId) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }

        String[] columns = new String[] {
                SENSE_COLUMN_ADS_NAME,
                SENSE_COLUMN_EXPIRE_TIME,
                SENSE_COLUMN_PRIORITY,
                SENSE_COLUMN_SILENT_INSTALL,
                SENSE_COLUMN_ADS_APPID,
                SENSE_COLUMN_ADS_APP_KEY,
                SENSE_COLUMN_ADS_POSID,
                SENSE_COLUMN_MAX_ADV_NUM,
                SENSE_COLUMN_ADV_SIZE_TYPE,
                SENSE_COLUMN_ADB_REAL_SIZE };
        String selection = SENSE_COLUMN_POS_ID + "=?";
        String[] selectionArgs = new String[] {posId};

        Cursor cursor = db.query(TABLE_SENSE, null, selection, selectionArgs, null, null, null);
        if (cursor == null) {
            return null;
        }
        List<ReaperAdSense> senseList = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                ReaperAdSense sense = new ReaperAdSense();
                sense.setPosId(posId);
                sense.ads_name = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADS_NAME));
                sense.expire_time = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_EXPIRE_TIME));
                sense.priority = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_PRIORITY));
                sense.silent_install = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_SILENT_INSTALL));
                sense.ads_appid = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADS_APPID));
                sense.ads_app_key = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADS_APP_KEY));
                sense.ads_posid = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADS_POSID));
                sense.max_adv_num = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_MAX_ADV_NUM));
                sense.adv_size_type = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADV_SIZE_TYPE));
                sense.adv_real_size = cursor.getString(cursor.getColumnIndex(SENSE_COLUMN_ADB_REAL_SIZE));
                senseList.add(sense);
            }
        } finally {
            cursor.close();
            //db.close();
        }

        return senseList;
    }

    private ReaperAdSense queryAdSenseInner(String posId) {
        // query adv exposure first
        ReaperAdvPos pos = queryAdvPosInner(posId);
        if (pos == null) {
            return null;
        }

        List<ReaperAdSense> senseList = queryAllAdSenseInner(posId);

        if (senseList == null || senseList.size() == 0) {
            return null;
        }

        if (ReaperConfig.VALUE_ADV_EXPOSURE_FIRST.equals(pos.adv_exposure)) {
            ReaperLog.i(TAG, "queryAdSenseInner . before sort : " + senseList);
            Collections.sort(senseList);
            ReaperLog.i(TAG, "queryAdSenseInner . after  sort : " + senseList);
            return senseList.get(0);

        } else {
            //TODO : support other exposure policies
        }

        return null;
    }

    private ReaperAdvPos queryAdvPosInner(String posId) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        String[] columns = new String[] {POS_COLUMN_ADV_TYPE, POS_COLUMN_ADV_EXPOSURE};
        String selection = POS_COLUMN_POS_ID + "=?";
        String[] selectionArgs = new String[] {posId};
        Cursor cursor = db.query(TABLE_POS, columns, selection, selectionArgs, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                ReaperAdvPos pos = new ReaperAdvPos();
                pos.adv_type = cursor.getString(cursor.getColumnIndex(POS_COLUMN_ADV_TYPE));
                pos.adv_exposure = cursor.getString(cursor.getColumnIndex(POS_COLUMN_ADV_EXPOSURE));
                pos.pos_id = posId;
                return pos;
            }
        } finally {
            if(cursor != null)
                cursor.close();
            //db.close();
        }
        return null;
    }

    private void saveReaperAdvPosInner(List<ReaperAdvPos> posList) {
        if (posList == null || posList.size() == 0) {
            return;
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (db == null) {
            ReaperLog.e(TAG, "saveReaperAdvPos, can not get writable database");
            return;
        }
        // clear all data before save
        db.execSQL("delete from " + TABLE_POS);
        db.execSQL("delete from " + TABLE_SENSE);

        try {
            for (ReaperAdvPos pos : posList) {
                if (pos == null)  continue;
                long pos_insert = db.insert(TABLE_POS, null, pos.toContentValues());
                if(pos_insert < 0)
                    ReaperLog.e(TAG, pos + " insert pos to " + TABLE_POS + " exits problems");
                List<ReaperAdSense> senseList = pos.getAdSenseList();
                if (senseList == null || senseList.size() == 0) continue;
                for (ReaperAdSense sense : senseList) {
                    if (sense == null) continue;
                    long sense_insert = db.insert(TABLE_SENSE, null, sense.toContentValues());
                    if(sense_insert < 0 )
                        ReaperLog.e(TAG, sense + " insert sense to " + TABLE_SENSE + " exits problems");
                }
            }
        } finally {
            //db.close();
        }
    }

}
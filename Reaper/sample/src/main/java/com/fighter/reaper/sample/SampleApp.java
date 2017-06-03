package com.fighter.reaper.sample;

import android.content.Context;

import com.fighter.loader.ReaperApplication;
import com.fighter.reaper.sample.config.SampleConfig;
import com.fighter.reaper.sample.utils.SampleLog;
import com.fighter.reaper.sample.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LiuJia on 2017/5/19.
 */

public class SampleApp extends ReaperApplication {

    private final static String TAG = SampleApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if(mReaperApi == null) {
            SampleLog.e(TAG, "Sample app onCreate method mReaperApi is null");
            ToastUtil.getInstance(this).showSingletonToast(getString(R.string.ad_reaper_init_failed));
        }
        mReaperApi.init(this, "10010", "not_a_real_key", true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //boolean success = copyDBToDatabases(this);
        //SampleLog.i(TAG, "attachBaseContext copy data base " + success);
    }

    private static final String DB_PATH = "/data/data/com.fighter.reaper.sample/databases/";
    private static final String DB_NAME = "reaper_adv_config.db";
    private static final String DB_JOURNAL_NAME = "reaper_adv_config.db-journal";

    private boolean copyDBToDatabases(Context context) {
        boolean dbSuccess = copyDBToDatabases(context, DB_NAME);
        boolean journalSuccess = copyDBToDatabases(context, DB_JOURNAL_NAME);
        return dbSuccess && journalSuccess;
    }

    public boolean copyDBToDatabases(Context context, String name) {

        String outFileName = DB_PATH + name;

        File file = new File(DB_PATH);
        if (!file.mkdirs()) {
            file.mkdirs();
        }

        File dataBaseFile = new File(outFileName);
        if (dataBaseFile.exists()) {
            dataBaseFile.delete();
        }

        InputStream in = null;
        OutputStream os = null;
        try {
            in = context.getAssets().open(DB_NAME);
            os = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

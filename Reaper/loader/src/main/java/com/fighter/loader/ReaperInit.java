package com.fighter.loader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fighter.patch.ReaperFile;
import com.fighter.patch.ReaperPatch;
import com.fighter.patch.ReaperPatchVersion;
import com.fighter.utils.Slog;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Init at ReaperApplication's onCreate.
 * If user don't extends ReaperApplication,
 * user should init it in own Application.
 *
 * eg :
 * public class App extends Application {
 *     private ReaperApi mReaperApi;
 *     public void onCreate() {
 *         mReaperApi = ReaperInit.init(this);
 *     }
 * }
 *
 * or
 *
 * public class App extends ReaperApplication {
 *     public void onCreate() {
 *         super.onCreate();
 *
 *         mReaperApi.xxx
 *     }
 * }
 *
 */

/**
 * Created by huayang on 17-5-5.
 */

public class ReaperInit {

    private static final String TAG = ReaperInit.class.getSimpleName();
    private static final boolean DEBUG_REAPER_PATCH = true;

    private static final String CLASS_REAPER_API = "com.fighter.api.ReaperApi";

    private static final String REAPER = "reaper.apk";
    private static final String REAPER_SYSTEM = "com.fighter.fighter";
    private static final String REAPER_DIR_SDCARD =
            Environment.getExternalStorageDirectory().toString() +
            File.separator + ".reapers" + File.separator;
    private static final String ASSETS_PREFIX = "file:///assets/";
    private static final String REAPER_DIR_ASSETS = ASSETS_PREFIX + "ads/" + REAPER;
    private static final String REAPER_DIR_DOWNLOAD = REAPER_DIR_SDCARD + "download" + File.separator + REAPER;
    private static final String[] ALL_REAPERS = {
            REAPER_SYSTEM,
            REAPER_DIR_ASSETS,
            REAPER_DIR_DOWNLOAD
    };

    /**
     * Get highest version of ReaperApi
     * @param context
     */
    public static ReaperApi init(Context context) {
        ReaperPatch suitableRP = getPatchForHighestVersion(context);
        if (suitableRP == null || !suitableRP.isValid()) {
            if (DEBUG_REAPER_PATCH) {
                Slog.e(TAG, "init : selectSuitablePatch error !");
            }
            return null;
        }

        ClassLoader loader = suitableRP.getPatchLoader();
        if (loader == null) {
            if (DEBUG_REAPER_PATCH) {
                Slog.e(TAG, "init : classLoader == null !");
            }
            return null;
        }
        try {
            Class claxx = loader.loadClass(CLASS_REAPER_API);
            if (claxx == null) {
                if (DEBUG_REAPER_PATCH) {
                    Slog.e(TAG, "init : cant find class " + CLASS_REAPER_API);
                }
                return null;
            }
            Object obj = claxx.newInstance();
            if (obj == null) {
                if (DEBUG_REAPER_PATCH) {
                    Slog.e(TAG, "init : " + CLASS_REAPER_API + " newInstance error !");
                }
                return null;
            }
            return new ReaperApi(obj);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get highest version of patch
     * @param context
     * @return
     */
    private static ReaperPatch getPatchForHighestVersion(Context context) {
        List<ReaperPatch> patches = new ArrayList<>(3);
        for (String reaper : ALL_REAPERS) {
            ReaperPatch patch = null;
            if (TextUtils.isEmpty(reaper))
                continue;
            if (TextUtils.equals(reaper, REAPER_SYSTEM)) {
                patch = loadSystemReaperPatch(context);
            } else if (reaper.startsWith(ASSETS_PREFIX)) {
                patch = loadReaperPatchByFD(context, reaper);
            } else {
                patch = loadReaperPatchByPath(reaper);
            }
            if (patch == null)
                continue;
            patches.add(patch);
        }

        if (patches.size() <= 0) {
            Slog.e(TAG, "getPatchForHighestVersion, cant find patches.");
            return null;
        }

        Comparator<ReaperPatch> comparator = new Comparator<ReaperPatch>() {
            @Override
            public int compare(ReaperPatch patchLeft, ReaperPatch patchRight) {
                //sort by version, dsc
                return comparePatchVersion(patchLeft.getVersion(), patchRight.getVersion());
            }
        };
        Collections.sort(patches, comparator);

        return patches.get(0);
    }

    /**
     * first > second return -1;
     * first = second return 0;
     * first < second return 1
     * @param first
     * @param second
     * @return
     */
    private static int comparePatchVersion(ReaperPatchVersion first, ReaperPatchVersion second) {
        if (first.release > second.release) {
            return -1;
        } else if (first.release < second.release) {
            return 1;
        }

        //release equals
        if (first.second > second.second) {
            return -1;
        } else if (first.second < second.second) {
            return 1;
        }

        //second equals
        if (first.revision > second.revision) {
            return -1;
        } else if (first.revision < second.revision) {
            return 1;
        }

        //revision equals
        //perhaps support other suffix ?
        String alpha = "-alpha";
        String beta = "-beta";
        String stable = "-stable";
        if (TextUtils.isEmpty(first.suffix)
                || TextUtils.isEmpty(second.suffix)) {
            return 0;
        }
        boolean fAlpha = TextUtils.equals(first.suffix, alpha);
        boolean fBeta = TextUtils.equals(first.suffix, beta);
        boolean fStable = TextUtils.equals(first.suffix, stable);
        boolean sAlpha = TextUtils.equals(second.suffix, alpha);
        boolean sBeta = TextUtils.equals(second.suffix, beta);
        boolean sStable = TextUtils.equals(second.suffix, stable);
        if ((fStable && (sBeta || sAlpha)) ||
                (fBeta && sAlpha)) {
            return -1;
        } else if ((sStable && (fBeta || fAlpha)) ||
                (sBeta && fAlpha)) {
            return 1;
        }

        return 0;
    }

    /**
     * Load ReaperPatch which system pre-installed
     * @param context
     * @return
     */
    private static ReaperPatch loadSystemReaperPatch(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo =
                    pm.getPackageInfo(REAPER_SYSTEM,
                            PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (packageInfo == null)
                return null;

            String apkPath = packageInfo.applicationInfo.sourceDir;
            ReaperFile reaperFile = new ReaperFile(apkPath);
            ReaperPatch reaperPatch = new ReaperPatch(reaperFile);
            Slog.e(TAG, "loadSystemReaperPatch:reaperPatch.isValid() : " + reaperPatch.isValid());
            return reaperPatch.isValid() ? reaperPatch : null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load ReaperPatch by path,
     * file in this path usually downloaded from server.
     * @param path
     * @return
     */
    private static ReaperPatch loadReaperPatchByPath(@NonNull String path) {
        if (TextUtils.isEmpty(path))
            return null;
        ReaperFile reaperFile = new ReaperFile(path);
        File rawFile = reaperFile.getRawFile();
        if (rawFile == null || !rawFile.exists())
            return null;
        ReaperPatch reaperPatch = new ReaperPatch(reaperFile);
        Slog.e(TAG, "loadReaperPatchByPath:reaperPatch.isValid() : " + reaperPatch.isValid());
        return reaperPatch.isValid() ? reaperPatch : null;
    }

    /**
     * Load ReaperPatch by fd, file preset at app's assets.
     * @param context
     * @param nameWithPrefix
     * @return
     */
    private static ReaperPatch loadReaperPatchByFD(Context context, @NonNull String nameWithPrefix) {
        int prefixIndex = nameWithPrefix.indexOf(ASSETS_PREFIX);
        String name = nameWithPrefix.substring(prefixIndex + 1, nameWithPrefix.length());
        AssetManager am = context.getAssets();
        AssetFileDescriptor afd;
        try {
            afd = am.openFd(name);
            ReaperFile reaperFile = new ReaperFile(afd);
            ReaperPatch reaperPatch = new ReaperPatch(reaperFile);
            Slog.e(TAG, "loadReaperPatchByFD:reaperPatch.isValid() : " + reaperPatch.isValid());
            return reaperPatch.isValid() ? reaperPatch : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

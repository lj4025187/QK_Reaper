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
import com.fighter.patch.ReaperPatchManager;
import com.fighter.patch.ReaperPatchVersion;
import com.fighter.utils.LoaderLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
    private static final String REAPER_SYSTEM = "com.fighter.reaper";
    private static final String REAPER_DIR_SDCARD =
            Environment.getExternalStorageDirectory().toString() +
            File.separator + ".reapers" + File.separator;
    private static final String ASSETS_PREFIX = "file:///assets/";
    private static final String REAPER_PATH_ASSETS = ASSETS_PREFIX + "ads/" + REAPER;
    private static final String REAPER_PATH_DOWNLOAD = REAPER_DIR_SDCARD + "download" + File.separator + REAPER;
    private static final String[] ALL_REAPERS = {
            REAPER_SYSTEM,
            REAPER_PATH_ASSETS,
            REAPER_PATH_DOWNLOAD
    };

    /**
     * Get highest version of ReaperApi
     * @param context
     */
    public static ReaperApi init(Context context) {
        context = context.getApplicationContext();
        ReaperPatch reaperPatch = getPatchForHighestVersion(context);
        ReaperApi api = makeReaperApiFromPatch(context, reaperPatch);
        if (api == null) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "init : makeApi error !");
            return null;
        }

        if (reaperPatch == null)
            return null;
        queryHigherReaperInServer(reaperPatch.getVersion());

        return api;
    }

    /**
     * Query higher than local in server. If have , download it .
     * @param currentVersion
     */
    private static void queryHigherReaperInServer(final ReaperPatchVersion currentVersion) {
        if (currentVersion == null) {
            LoaderLog.e(TAG, "queryHigherReaperInServer : currentVersion == null.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ReaperServerDesc rsd = doQueryHigherReaperInServer();
                if (rsd == null || !rsd.isValid())
                    return;

                //comparing to judge really higher or not .
                int retVal = comparePatchVersion(currentVersion, rsd.version);
                if (retVal != 1) {
                    if (DEBUG_REAPER_PATCH) {
                        LoaderLog.e(TAG, "we download a bad version ! rsd.version : " + rsd.version);
                    }
                    return;
                }

                downloadHigherReaper(rsd);
            }
        }).start();
    }

    private static ReaperServerDesc doQueryHigherReaperInServer() {
        //TODO: query from server.
        return new ReaperServerDesc();
    }

    private static void downloadHigherReaper(ReaperServerDesc rsd) {
        if (rsd == null || !rsd.isValid()) {
            return;
        }

        //TODO: download from server.
    }

    /**
     * If we get a right ReaperPatch, we will make instance of ReaperApi.
     * @param patch
     * @return
     */
    private static ReaperApi makeReaperApiFromPatch(Context context, ReaperPatch patch) {
        if (patch == null || !patch.isValid()) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "init : selectSuitablePatch error !");
            }
            return null;
        }

        ClassLoader loader = patch.getPatchLoader();
        if (loader == null) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "init : classLoader == null !");
            }
            return null;
        }
        try {
            Class claxx = loader.loadClass(CLASS_REAPER_API);
            if (claxx == null) {
                if (DEBUG_REAPER_PATCH) {
                    LoaderLog.e(TAG, "init : cant find class " + CLASS_REAPER_API);
                }
                return null;
            }
            Object obj = claxx.newInstance();
            if (obj == null) {
                if (DEBUG_REAPER_PATCH) {
                    LoaderLog.e(TAG, "init : " + CLASS_REAPER_API + " newInstance error !");
                }
                return null;
            }
            ReaperPatchVersion pv = patch.getVersion();
            String version = pv.release + pv.second + pv.revision + pv.suffix;
            return new ReaperApi(obj, version);
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
        List<ReaperFile> reaperFiles = new ArrayList<>(3);
        for (String reaper : ALL_REAPERS) {
            ReaperFile patch = null;
            if (TextUtils.isEmpty(reaper))
                continue;
            if (TextUtils.equals(reaper, REAPER_SYSTEM)) {
                patch = loadSystemReaperFile(context);
            } else if (reaper.startsWith(ASSETS_PREFIX)) {
                patch = loadReaperFileByFD(context, reaper);
            } else {
                patch = loadReaperFileByPath(reaper);
            }
            if (patch == null)
                continue;
            reaperFiles.add(patch);
        }

        if (reaperFiles.size() <= 0) {
            LoaderLog.e(TAG, "getPatchForHighestVersion, cant find ReaperFile .");
            return null;
        }

        List<ReaperPatch> patches =
                ReaperPatchManager.getInstance()
                        .unpackPatches(reaperFiles, context.getApplicationContext().getClassLoader());
        if (patches == null || patches.size() <= 0) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "getPatchForHighestVersion, cant unpack patches.");
            }
            return null;
        }
        List<ReaperPatch> comparePatches = new ArrayList<>();
        comparePatches.addAll(patches);

        Iterator<ReaperPatch> it = patches.iterator();
        while (it.hasNext()) {
            ReaperPatch patch = it.next();
            if (patch == null)
                continue;
            if (!patch.isValid()) {
                it.remove();
            }
        }
        if (patches.size() <= 0)
            return null;

        Comparator<ReaperPatch> comparator = new Comparator<ReaperPatch>() {
            @Override
            public int compare(ReaperPatch patchLeft, ReaperPatch patchRight) {
                //sort by version, dsc
                return comparePatchVersion(patchLeft.getVersion(), patchRight.getVersion());
            }
        };
        Collections.sort(patches, comparator);
        ReaperPatch targetPatch = patches.get(0);

        deleteLowerVersionIfNeeded(comparePatches, targetPatch);

        return targetPatch;
    }

    /**
     * If we are using higher version of downloaded patches,
     * consider to delete lower version of downloaded patches.
     * @param allPatches all patches we have queried.
     * @param targetPatch patch we are using.
     */
    private static void
        deleteLowerVersionIfNeeded(List<ReaperPatch> allPatches, ReaperPatch targetPatch) {
        if (allPatches == null || allPatches.size() <= 0
                || targetPatch == null) {
            return;
        }

        //ensure that we are using Reaper from downloaded.
        ReaperFile targetRF = targetPatch.getReaperFile();
        if (targetRF == null)
            return;
        File targetFile = targetRF.getRawFile();
        if (targetFile == null)
            return;
        String targetPath = targetFile.getAbsolutePath();
        if (!targetPath.startsWith(REAPER_DIR_SDCARD)) {
            return;
        }

        List<ReaperFile> filterFiles = new ArrayList<>();
        for (ReaperPatch patch : allPatches) {
            ReaperFile rf = patch.getReaperFile();
            if (rf == null || rf.getRawFile() == null)
                continue;
            if (downloadedPatchEquals(patch, targetPatch))
                continue;
            File file = rf.getRawFile();
            String path = file.getAbsolutePath();
            if (!path.startsWith(REAPER_DIR_SDCARD))
                continue;
            filterFiles.add(rf);
        }

        if (filterFiles.size() > 0)
            return;
        for (ReaperFile rf : filterFiles) {
            rf.getRawFile().delete();
        }
    }

    /**
     * Compare two downloaded ReaperPatchs .
     * @param first
     * @param second
     * @return true if first == second.
     */
    private static boolean downloadedPatchEquals(ReaperPatch first, ReaperPatch second) {
        if (first == null || second == null)
            return false;
        ReaperFile firstRF = first.getReaperFile();
        ReaperFile secondRF = second.getReaperFile();
        if (firstRF != null && secondRF != null) {
            File firstRawFile = firstRF.getRawFile();
            File secondRawFile = secondRF.getRawFile();
            if (firstRawFile != null && secondRawFile != null) {
                return TextUtils.equals(firstRawFile.getAbsolutePath(),
                        secondRawFile.getAbsolutePath());
            }
        }

        return false;
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
    private static ReaperFile loadSystemReaperFile(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo =
                    pm.getPackageInfo(REAPER_SYSTEM,
                            PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (packageInfo == null)
                return null;

            String apkPath = packageInfo.applicationInfo.sourceDir;
            ReaperFile reaperFile = new ReaperFile(apkPath);
            LoaderLog.e(TAG, "loadSystemReaperFile : " + reaperFile.getName());
            return reaperFile;
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
    private static ReaperFile loadReaperFileByPath(@NonNull String path) {
        if (TextUtils.isEmpty(path))
            return null;
        ReaperFile reaperFile = new ReaperFile(path);
        File rawFile = reaperFile.getRawFile();
        if (rawFile == null || !rawFile.exists())
            return null;
        LoaderLog.e(TAG, "loadReaperFileByPath : " + reaperFile.getName());
        return reaperFile;
    }

    /**
     * Load ReaperPatch by fd, file preset at app's assets.
     * @param context
     * @param nameWithPrefix
     * @return
     */
    private static ReaperFile loadReaperFileByFD(Context context, String nameWithPrefix) {
        if (nameWithPrefix == null || !nameWithPrefix.startsWith(ASSETS_PREFIX))
            return null;
        String name = nameWithPrefix.substring(ASSETS_PREFIX.length(), nameWithPrefix.length());
        AssetManager am = context.getAssets();
        AssetFileDescriptor afd;
        try {
            afd = am.openFd(name);
            ReaperFile reaperFile = new ReaperFile(afd);
            LoaderLog.e(TAG, "loadReaperFileByFD: " + reaperFile.getName());
            return reaperFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    static class ReaperServerDesc {
        public String url;
        public ReaperPatchVersion version;

        public boolean isValid() {
            return url != null && version != null
                    && version.isValid();
        }
    }
}

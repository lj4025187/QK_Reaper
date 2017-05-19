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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private static boolean QUERY_SERVER = true;

    private static final String CLASS_REAPER_API = "com.fighter.api.ReaperApi";
    private static final String CLASS_REAPER_DOWNLOAD = "com.fighter.download.ReaperNetwork";

    private static final String REAPER = "reaper.rr";
    private static final String REAPER_SYSTEM = "com.fighter.reaper";
    private static final String REAPER_DIR_SDCARD =
            Environment.getExternalStorageDirectory().toString() +
            File.separator + ".reapers" + File.separator + "download";
    private static final String ASSETS_PREFIX = "file:///assets/";
    private static final String REAPER_PATH_ASSETS = ASSETS_PREFIX + "ads/" + REAPER;
    private static final String RR_SUFFIX = ".rr";
    private static final String[] ALL_REAPERS = {
            REAPER_SYSTEM,
            REAPER_PATH_ASSETS,
    };

    private static Context sContext;

    /**
     * Get highest version of ReaperApi
     * @param context
     */
    public static ReaperApi init(Context context) {
        context = context.getApplicationContext();
        sContext = context;
        ReaperPatch reaperPatch = getPatchForHighestVersion(context);
        if (reaperPatch == null) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "init : cant find any patches!");
            return null;
        }
        ReaperApi api = makeReaperApiFromPatch(context, reaperPatch);
        if (api == null) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "init : makeApi error !");
            return null;
        }

        initReaper(context, reaperPatch);
        if (QUERY_SERVER) {
            queryHigherReaperInServer(reaperPatch);
        }

        return api;
    }


    /**
     * @param context
     * @param reaperPatch
     */
    private static void initReaper(Context context, ReaperPatch reaperPatch) {
        ClassLoader classLoader = reaperPatch.getPatchLoader();
        if (classLoader == null) {
            LoaderLog.e(TAG, "initReaper, classLoader == null");
            return;
        }

        //1.get class
        Class claxx = null;
        try {
            claxx = classLoader.loadClass("com.fighter.download.ReaperEnv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (claxx == null) {
            LoaderLog.e(TAG, "initReaper, cant load ReaperNetwork !");
            return;
        }

        //2.set sdk path
        try {
            Field sdkAbsPath = claxx.getDeclaredField("sSdkPath");
            if (sdkAbsPath == null) {
                LoaderLog.e(TAG, "cant find sSdkPath");
                throw new RuntimeException("cant find sSdkPath");
            }
            sdkAbsPath.setAccessible(true);
            sdkAbsPath.set(null, reaperPatch.getAbsolutePath());

            Method initForNetworkMethod = claxx.getDeclaredMethod("initForNetwork");
            if (initForNetworkMethod == null) {
                LoaderLog.e(TAG, "initForNetworkMethod == null");
                throw new RuntimeException("cant find initForNetwork");
            }
            initForNetworkMethod.setAccessible(true);
            initForNetworkMethod.invoke(null, null);

            LoaderLog.e(TAG, "initReaper success !");
        } catch (Exception e) {
            e.printStackTrace();
            LoaderLog.e(TAG, "initReaper, err : " + e.getMessage());
        }

        //3.set context obj
        try {
            Field sContext = claxx.getDeclaredField("sContext");
            if (sContext == null) {
                throw new RuntimeException("cant find sContext");
            }
            sContext.setAccessible(true);
            sContext.set(null, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Query higher than local in server. If have , download it .
     * @param patch
     */
    private static void queryHigherReaperInServer(final ReaperPatch patch) {
        if (patch == null)
            return;
        ReaperPatchVersion currentVersion = patch.getVersion();
        if (currentVersion == null || TextUtils.isEmpty(currentVersion.getVersionStr())) {
            LoaderLog.e(TAG, "queryHigherReaperInServer : currentVersion is bad!.");
            return;
        }

        ReaperVersionManager reaperManager = ReaperVersionManager
                .getInstance(currentVersion.getVersionStr());

        ClassLoader loader = patch.getPatchLoader();
        try {
            Class claxx = loader.loadClass(CLASS_REAPER_DOWNLOAD);
            if (claxx == null) {
                LoaderLog.e(TAG, "ReaperNetwork class is null !");
                return;
            }
            reaperManager.setReaperNetworkClass(claxx);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        reaperManager.queryHigherReaper();
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
            ReaperFile reaperFile = null;
            if (TextUtils.isEmpty(reaper))
                continue;
            if (TextUtils.equals(reaper, REAPER_SYSTEM)) {
                reaperFile = loadSystemReaperFile(context);
            } else if (reaper.startsWith(ASSETS_PREFIX)) {
                reaperFile = loadReaperFileByFD(context, reaper);
            }
            if (reaperFile == null)
                continue;
            reaperFiles.add(reaperFile);
        }

        if (reaperFiles.size() <= 0) {
            LoaderLog.e(TAG, "getPatchForHighestVersion, cant find ReaperFile .");
            return null;
        }

        List<ReaperPatch> patches =
                ReaperPatchManager.getInstance()
                        .unpackPatches(context, reaperFiles, context.getApplicationContext().getClassLoader());
        ReaperPatch sdReaperPatch = loadReaperFileByPath(REAPER_DIR_SDCARD);
        if (sdReaperPatch != null) {
            patches.add(sdReaperPatch);
        }

        if (patches == null || patches.size() <= 0) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "getPatchForHighestVersion, cant unpack patches.");
            }
            return null;
        }

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
        if (DEBUG_REAPER_PATCH) {
            LoaderLog.e(TAG, "get highest version : " + targetPatch.getAbsolutePath());
        }

        return targetPatch;
    }

    /**
     * If we are using higher version of downloaded patches,
     * consider to delete lower version of downloaded patches.
     * @param allPatches all patches we have queried.
     */
    private static void
        deleteLowerVersionIfNeeded(List<ReaperPatch> allPatches) {
        if (allPatches == null || allPatches.size() <= 1) {
            return;
        }
        allPatches = sortPatches(allPatches);
        List<ReaperPatch> toDeletePatches = new ArrayList<>();
        toDeletePatches.addAll(allPatches);
        for (int i = 0; i < toDeletePatches.size(); ++i) {
            if (i == 0)
                continue;
            ReaperPatch patch = toDeletePatches.get(i);
            ReaperFile reaperFile = patch.getReaperFile();
            File file = reaperFile.getRawFile();
            if (file == null)
                continue;
            boolean success = file.delete();
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "delete downloaded reaper : " + patch.getName() + " " + success);
            }
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
     * @param path path of .rr's parent
     * @return
     */
    private static ReaperPatch loadReaperFileByPath(@NonNull String path) {
        if (TextUtils.isEmpty(path))
            return null;
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length <= 0) {
            return null;
        }
        List<ReaperFile> reaperFiles = new ArrayList<>();
        for (File reaper : files) {
            if (!reaper.getName().endsWith(RR_SUFFIX)) {
                continue;
            }
            ReaperFile rf = new ReaperFile(reaper.getAbsolutePath());
            reaperFiles.add(rf);
        }

        List<ReaperPatch> patches = ReaperPatchManager.getInstance()
                .unpackPatches(sContext, reaperFiles, sContext.getClassLoader());
        if (patches == null || patches.size() <= 0) {
            return null;
        }

        Iterator<ReaperPatch> it = patches.iterator();
        while (it.hasNext()) {
            if (!it.next().isValid()) {
                it.remove();
            }
        }
        if (patches.size() <= 0) {
            return null;
        }

        patches = sortPatches(patches);
        deleteLowerVersionIfNeeded(patches);

        return patches.get(0);
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

    private static List<ReaperPatch> sortPatches(@NonNull List<ReaperPatch> patches) {
        Collections.sort(patches, new Comparator<ReaperPatch>() {
            @Override
            public int compare(ReaperPatch l, ReaperPatch r) {
                return comparePatchVersion(l.getVersion(), r.getVersion());
            }
        });
        return patches;
    }
}

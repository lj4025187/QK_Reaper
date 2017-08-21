package com.fighter.loader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import com.fighter.patch.ReaperClassLoader;
import com.fighter.patch.ReaperFile;
import com.fighter.patch.ReaperPatch;
import com.fighter.patch.ReaperPatchCryptAndroidTool;
import com.fighter.patch.ReaperPatchManager;
import com.fighter.patch.ReaperPatchVersion;
import com.fighter.utils.LoaderLog;
import com.qiku.proguard.annotations.NoProguard;

import java.io.File;
import java.io.IOException;
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
@NoProguard
public class ReaperInit {

    private static final String TAG = "ReaperInit";

    private static final boolean DEBUG_REAPER_PATCH = true;
    private static boolean QUERY_SERVER = true;

    private static final String CLASS_REAPER_API = "com.fighter.api.ReaperApi";
    private static final String CLASS_REAPER_DOWNLOAD = "com.fighter.download.ReaperNetwork";

    // reaper rr file locations
    private static final String REAPER_LOCATION_SYSTEM = "com.fighter.reaper";
    private static final String REAPER_LOCATION_ASSETS = "reaper.rr";
    private static final String REAPER_LOCATION_SDCARD =
            Environment.getExternalStorageDirectory().toString() +
            File.separator + ".reapers" + File.separator + "download";


    private static final String RR_SUFFIX = ".rr";


    private static Context sContext;
    private static ReaperApi sApi;
    private static ReaperPatch sReaperPatch;

    /**
     * Get highest version of ReaperApi
     *
     * @param context Application context
     * @return ReaperApi
     */
    @NoProguard
    public static ReaperApi init(Context context) {
        if(Process.myUid() != context.getApplicationInfo().uid){
            LoaderLog.e("Init ReaperApi in uid different from context will cause some problems");
        }
        sContext = context.getApplicationContext();

        if (sApi != null && sApi.isValid()) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "already init, use : " + sApi);
            return sApi;
        }

        if (!getPatchForHighestVersion()) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "cant find reaper patch");
            return null;
        }

        if (DEBUG_REAPER_PATCH) {
            LoaderLog.i(TAG, "finally, we use : " + (sReaperPatch.getReaperFile().hasFD() ?
                    "assets/reaper.rr" : sReaperPatch.getAbsolutePath() ));
            LoaderLog.i(TAG, "version : " + sReaperPatch.getVersion());
        }

        ReaperApi api = makeReaperApiFromPatch();
        if (api == null) {
            if (DEBUG_REAPER_PATCH)
                LoaderLog.e(TAG, "init : makeApi error !");
            return null;
        }
        sApi = api;

        if (!initReaperEnv()) {
            LoaderLog.e(TAG, "init : initReaperEnv error !");
            return null;
        }

        if (QUERY_SERVER) {
            queryHigherReaperInServer();
        }

        return api;
    }


    private static ReaperPatch getReaperPath() {
        return sReaperPatch;
    }

    /**
     * Init com.fighter.reaper.ReaperEnv
     *
     */
    private static boolean initReaperEnv() {
        if (sContext == null) {
            LoaderLog.e(TAG, "initReaperEnv, context == null");
            return false;
        }

        ClassLoader classLoader = sReaperPatch.getPatchLoader();
        if (classLoader == null) {
            LoaderLog.e(TAG, "initReaperEnv, classLoader == null");
            return false;
        }

        //1.get class
        Class reaperEnvClass = null;
        try {
            reaperEnvClass = classLoader.loadClass("com.fighter.reaper.ReaperEnv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reaperEnvClass == null) {
            LoaderLog.e(TAG, "initReaperEnv, cant load ReaperEnv !");
            return false;
        }

        //2.get sdk path
        String sdkPath = null;
        if (classLoader instanceof ReaperClassLoader) {
            ReaperClassLoader rc = (ReaperClassLoader) classLoader;
            sdkPath = rc.getRawDexPath();
        } else {
            sdkPath = sReaperPatch.getAbsolutePath();
        }
        if (TextUtils.isEmpty(sdkPath)) {
            LoaderLog.e(TAG, "initReaperEnv error ! Can't find reaper.rr or reaper.apk");
            return false;
        }

        //3.init reaper env
        try{
            Method initReaperEnv
                    = reaperEnvClass.getDeclaredMethod("init", Context.class, String.class, ClassLoader.class);
            if (initReaperEnv == null) {
                LoaderLog.e(TAG, "initReaperEnv error. cant find method initReaperEnv");
                return false;
            }
            initReaperEnv.setAccessible(true);
            initReaperEnv.invoke(null, sContext, sdkPath, classLoader);
            LoaderLog.e(TAG, "initReaperEnv success !");
        } catch (Exception e) {
            e.printStackTrace();
            LoaderLog.e(TAG, "initReaperEnv error. exception call init");
            return false;
        }

        return true;
    }

    /**
     * Query higher than local in server. If have , download it .
     */
    private static void queryHigherReaperInServer() {
        if (sReaperPatch == null)
            return;
        ReaperPatchVersion currentVersion = sReaperPatch.getVersion();
        if (currentVersion == null || TextUtils.isEmpty(currentVersion.getVersionStr())) {
            LoaderLog.e(TAG, "queryHigherReaperInServer : currentVersion is bad!.");
            return;
        }

        ClassLoader loader = sReaperPatch.getPatchLoader();
        Class reaperNetworkClass = null;
        try {
            reaperNetworkClass = loader.loadClass(CLASS_REAPER_DOWNLOAD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (reaperNetworkClass == null) {
            LoaderLog.e(TAG, "queryHigherReaperInServer. ReaperNetwork class is null !");
            return;
        }

        ReaperVersionManager rvm =
                new ReaperVersionManager(sContext, currentVersion.getVersionStr(), reaperNetworkClass);
        rvm.queryHigherReaper();
    }

    /**
     * If we get a right ReaperPatch, we will make instance of ReaperApi.
     *
     * @return
     */
    private static ReaperApi makeReaperApiFromPatch() {
        if (sReaperPatch == null || !sReaperPatch.isValid()) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "makeReaperApiFromPatch error, sReaperPatch is null or invalid");
            }
            return null;
        }

        ClassLoader loader = sReaperPatch.getPatchLoader();
        if (loader == null) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "init : classLoader == null !");
            }
            return null;
        }
        try {
            Class reaperApiClass = loader.loadClass(CLASS_REAPER_API);
            if (reaperApiClass == null) {
                if (DEBUG_REAPER_PATCH) {
                    LoaderLog.e(TAG, "init : cant find class " + CLASS_REAPER_API);
                }
                return null;
            }
            Object obj = reaperApiClass.newInstance();
            if (obj == null) {
                if (DEBUG_REAPER_PATCH) {
                    LoaderLog.e(TAG, "init : " + CLASS_REAPER_API + " newInstance error !");
                }
                return null;
            }
            ReaperPatchVersion pv = sReaperPatch.getVersion();
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
     */
    private static boolean getPatchForHighestVersion() {

        // 1. load all reaper files in the system(reaper apk, sdcard, assets)
        List<ReaperFile> reaperFiles = loadAllReaperFiles();
        if (reaperFiles == null || reaperFiles.size() == 0) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "we cant find any reaper files");
            }
            return false;
        }
        if (DEBUG_REAPER_PATCH) {
            LoaderLog.e(TAG, "we found " + reaperFiles.size() + " files");
        }

        // 2. delete all files in .reaper_patch
        ReaperPatchCryptAndroidTool.deleteAllFiles(sContext);

        // 3. unpack patches
        ClassLoader parent = sContext.getClassLoader().getParent();
        List<ReaperPatch> patches = ReaperPatchManager.getInstance().unpackPatches(sContext,
                reaperFiles, parent != null ? parent : ClassLoader.getSystemClassLoader());


        // 4. delete low version file on sdcard and remove invalid patches
        deleteLowerVersionIfNeeded(patches);
        removeInvalidPatches(patches);

        if (patches.size() == 0) {
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "we cant find valid reaper file");
            }
            return false;
        }

        // 5. sort
        sortPatches(patches);

        // 6. save reaper patch with highest version
        sReaperPatch = patches.get(0);
        if (DEBUG_REAPER_PATCH) {
            LoaderLog.i(TAG, "hasFd : " + sReaperPatch.getReaperFile().hasFD());
            LoaderLog.i(TAG, "get highest version : " + sReaperPatch.getAbsolutePath());
        }

        return sReaperPatch != null;
    }

    /**
     * Find and load all reaper files in the system
     *
     * @return
     */
    private static List<ReaperFile> loadAllReaperFiles() {
        List<ReaperFile> reaperFiles = new ArrayList<>();

        ReaperFile systemReaper = loadSystemReaperFile();
        if (systemReaper != null) {
            reaperFiles.add(systemReaper);
        }

        ReaperFile assetsReaper = loadAssetsReaperFile();
        if (assetsReaper != null) {
            reaperFiles.add(assetsReaper);
        }

        List<ReaperFile> sdcardReaperList = loadSDCardReaperFile();
        if (sdcardReaperList != null && sdcardReaperList.size() != 0) {
            reaperFiles.addAll(sdcardReaperList);
        }
        return reaperFiles;
    }

    /**
     * Remove invalid patches
     *
     * @param patches
     */
    private static void removeInvalidPatches(List<ReaperPatch> patches) {
        if (patches == null || patches.size() == 0) {
            return;
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
    }

    /**
     * If we are using higher version of downloaded patches,
     * consider to delete lower version of downloaded patches.
     * @param allPatches all patches we have queried.
     */
    private static void deleteLowerVersionIfNeeded(List<ReaperPatch> allPatches) {
        if (allPatches == null || allPatches.size() <= 1) {
            return;
        }
        List<ReaperPatch> toDeletePatches = new ArrayList<>();
        toDeletePatches.addAll(allPatches);

        sortPatches(toDeletePatches);
        LoaderLog.e(TAG, "deleteLowerVersionIfNeeded. after sort : " + toDeletePatches);
        for (int i = 0; i < toDeletePatches.size(); ++i) {
            if (i == 0) {
                continue;
            }
            ReaperPatch patch = toDeletePatches.get(i);
            if (patch == null) {
                continue;
            }
            ReaperFile reaperFile = patch.getReaperFile();
            if (reaperFile == null) {
                continue;
            }
            File file = reaperFile.getRawFile();
            if (file == null) {
                continue;
            }
            boolean success = file.delete();
            if (DEBUG_REAPER_PATCH) {
                LoaderLog.e(TAG, "delete downloaded reaper : " + patch.getName() + " " + success);
            }
        }
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
        if ((fStable && (sBeta || sAlpha)) || (fBeta && sAlpha)) {
            return -1;
        } else if ((sStable && (fBeta || fAlpha)) || (sBeta && fAlpha)) {
            return 1;
        }

        return 0;
    }

    /**
     * Load ReaperPatch which system pre-installed
     * @return
     */
    private static ReaperFile loadSystemReaperFile() {
        PackageManager pm = sContext.getPackageManager();
        try {
            PackageInfo packageInfo =
                    pm.getPackageInfo(REAPER_LOCATION_SYSTEM,
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


    private static List<ReaperFile> loadSDCardReaperFile() {

        File reaperSDCardDir = new File(REAPER_LOCATION_SDCARD);
        if (!reaperSDCardDir.exists()) {
            LoaderLog.i(TAG, REAPER_LOCATION_SDCARD + " does not exists");
            return null;
        }
        if (!reaperSDCardDir.isDirectory()) {
            LoaderLog.i(TAG, REAPER_LOCATION_SDCARD + " is not a directory");
            return null;
        }

        File[] files = reaperSDCardDir.listFiles();
        if (files == null || files.length == 0) {
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

        return reaperFiles;
    }

    /**
     * Load ReaperPatch by fd, file preset at app's assets.
     * @return
     */
    private static ReaperFile loadAssetsReaperFile() {
        AssetManager am = sContext.getAssets();
        AssetFileDescriptor afd;
        try {
            afd = am.openFd(REAPER_LOCATION_ASSETS);
            return new ReaperFile(afd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sortPatches( List<ReaperPatch> patches) {
        Collections.sort(patches, new Comparator<ReaperPatch>() {
            @Override
            public int compare(ReaperPatch l, ReaperPatch r) {
                return comparePatchVersion(l.getVersion(), r.getVersion());
            }
        });
    }
}

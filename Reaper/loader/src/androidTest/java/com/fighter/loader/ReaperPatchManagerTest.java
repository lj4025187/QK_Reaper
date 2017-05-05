package com.fighter.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fighter.patch.ReaperPatch;
import com.fighter.patch.ReaperPatchManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ReaperPatchManagerTest {

    private static final String TAG = ReaperPatchManagerTest.class.getSimpleName();

    @Test
    public void useUnpackPatchesWithFailed() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();
        List<ApplicationInfo> infos = appContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        assertFalse(infos.isEmpty());

        ApplicationInfo info = infos.get(0);
        List<File> files = new ArrayList<>();
        files.add(new File(info.sourceDir));
        List<ReaperPatch> patches = ReaperPatchManager.getInstance().unpackPatches(files);
        assertFalse(patches.isEmpty());

        assertEquals(patches.size(), 1);

        assertFalse(patches.get(0).isValid());
        assertFalse(patches.get(0).isValid());
        assertFalse(patches.get(0).getVersion().isValid());

        assertNull(patches.get(0).getAbsolutePath());
        assertNull(patches.get(0).getAbsolutePath());
        assertNotNull(patches.get(0).getPatchLoader());
    }

}

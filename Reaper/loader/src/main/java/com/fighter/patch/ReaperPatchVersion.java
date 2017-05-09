package com.fighter.patch;

import com.fighter.utils.Slog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wxthon on 5/5/17.
 */


/**
 * Version must be [main].[second].[revision]{-alpha,-beta,-stable}
 * eg: 1.0.0, 1.0.0-alpha, 1.0.0-beta, 1.0.0-stable
 */
public class ReaperPatchVersion {

    private static final String BUMP_VERSION_CLASS = "com.fighter.fighter.BumpVersion";
    private static final String BAD_VERSION = "xx.xx.xx";

    /**
     * Version fields
     */
    public int release = 0;
    public int second = 0;
    public int revision = 0;
    public String suffix = null;

    private String mVersionStr = null;

    /**
     * Constructor
     * Must get the version from patch loader in here
     *
     * @param loader
     */
    ReaperPatchVersion(ClassLoader loader) {
        try {
            Class bumpVersionClass = loader.loadClass(BUMP_VERSION_CLASS);
            if (bumpVersionClass == null)
                return;
            Method valueMethod = bumpVersionClass.getDeclaredMethod("value");
            if (valueMethod == null)
                return;
            String value = (String) valueMethod.invoke(null);
            if (value == null)
                return;
            mVersionStr = value;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // process version parse
        if (!isValid())
            return;
        String []versions = mVersionStr.split("\\.");
        release = Integer.valueOf(versions[0]);
        second = Integer.valueOf(versions[1]);
        if (versions[2].contains("-")) {
            String[] pieces = versions[2].split("-");
            revision = Integer.valueOf(pieces[0]);
            suffix = pieces[1];
        } else {
            revision = Integer.valueOf(versions[2]);
            suffix = null;
        }
    }

    /**
     * Check the version string is validate or not
     *
     * @return
     */
    public boolean isValid() {
        return mVersionStr != null && !mVersionStr.equals(BAD_VERSION)
                && mVersionStr.split("\\.").length == 3;
    }

}

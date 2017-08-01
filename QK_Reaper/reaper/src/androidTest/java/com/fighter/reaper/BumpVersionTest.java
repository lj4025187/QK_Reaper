package com.fighter.reaper;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class BumpVersionTest {
    @Test
    public void useIsValid() throws Exception {
        assertTrue(BumpVersion.isValid());
    }
}

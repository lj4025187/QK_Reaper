package com.fighter.reaper;

import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.fighter.common.rc4.IRC4;
import com.fighter.common.rc4.RC4Factory;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test RC4 encrypt and decrypt
 *
 * Created by zhangjg on 17-5-10.
 */

@RunWith(AndroidJUnit4.class)
public class RC4Test {

    private static final String TAG = RC4Test.class.getSimpleName();

    @Test
    public void testRC4Crypt() throws Exception {
        String key = "password";
        IRC4 rc4 = RC4Factory.create(key);

        String ori = "This is a secure text";
        String encrypt = rc4.encryptToBase64(ori);

        String decrypt = rc4.decryptFromBase64(encrypt);

        assertTrue(ori.equals(decrypt));
    }

    @Test
    public void testRC4CryptString() throws Exception {
        String key = "password";
        IRC4 rc4 = RC4Factory.create(key);
        String ori = "This is a secure text";
        byte[] encrypt = rc4.encrypt(ori.getBytes());
        byte[] decrypt = rc4.decrypt(encrypt);
        assertTrue(ori.equals(new String(decrypt)));
    }

    @Test
    public void testStringEncode() throws Exception {
        String s = "hello world";
        byte[] bytes = s.getBytes();
        String ss = new String(bytes);
        assertTrue(s.equals(ss));
    }
}

package com.fighter.loader;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fighter.patch.AESBlockCipher;
import com.fighter.patch.IReaperBlockCipher;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by wxthon on 5/8/17.
 */

@RunWith(AndroidJUnit4.class)
public class AESBlockCipherTest {

    private static final String TAG = AESBlockCipherTest.class.getSimpleName();

    @Test
    public void useEncryptAndDecrypt() {
        IReaperBlockCipher cipher = new AESBlockCipher();
        IReaperBlockCipher.Key key = cipher.createKey();
        for (int i = 0; i < key.data.length; ++i)
            key.data[i] = (byte) (i << 1 * 5);
        cipher.initKey(key);

        ByteBuffer inputBuffer = cipher.allocateBlockBuffer();
        ByteBuffer outputBuffer = cipher.allocateBlockBuffer();
        assertEquals(inputBuffer.position(), 0);
        assertEquals(outputBuffer.position(), 0);

        inputBuffer.put(new String("Hello World!!!!!").getBytes());
        Log.d(TAG, "-- useEncryptAndDecrypt --[Hello World!!!!!], offset: " + inputBuffer.arrayOffset() + ", position: " + inputBuffer.position());

        inputBuffer.limit(inputBuffer.position());
        inputBuffer.position(0);
        int size = cipher.encrypt(inputBuffer, outputBuffer);
        Log.d(TAG, "-- useEncryptAndDecrypt -- size: " + size + ", position: " + outputBuffer.position());
        assertEquals(size, outputBuffer.position());

        StringBuilder sb = new StringBuilder();
        outputBuffer.clear();
        for (int i = 0; i < size; ++i) {
            sb.append(Byte.toString(outputBuffer.get()));
            sb.append(" ");
        }
        Log.d(TAG, "-- useEncryptAndDecrypt --[" + sb.toString() + "]");

        outputBuffer.position(0);
        outputBuffer.limit(size);
        size = cipher.decrypt(outputBuffer, inputBuffer);
        assertEquals(size, inputBuffer.position());

        StringBuilder sb2 = new StringBuilder();
        inputBuffer.clear();
        for (int i = 0; i < size; ++i) {
            sb2.append(Byte.toString(inputBuffer.get()));
            sb2.append(" ");
        }
        Log.d(TAG, "-- useEncryptAndDecrypt -->>[" + sb2.toString() + "]");

        String text = new String(inputBuffer.array(), 0, size);
        Log.d(TAG, "-- useEncryptAndDecrypt --[" + text + "]");
        assertEquals(text, "Hello World!!!!!");
    }

}

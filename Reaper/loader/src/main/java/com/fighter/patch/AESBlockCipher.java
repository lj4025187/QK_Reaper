package com.fighter.patch;

import android.util.Log;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by wxthon on 5/8/17.
 */

public class AESBlockCipher implements IReaperBlockCipher {

    private static final String TAG = AESBlockCipher.class.getSimpleName();

    public static final String ALGORITHM = "AES/CFB/NoPadding";
    public static final int KEY_SIZE = 16;
    public static final int BLOCK_SIZE = 1024;
    public static ThreadLocal<Cipher> sThreadEncryptCipher = new ThreadLocal<>();
    public static ThreadLocal<Cipher> sThreadDecryptCipher = new ThreadLocal<>();

    private boolean mEncryptInited = false;
    private boolean mDecryptInited = false;

    private Cipher currentEncryptCipher() {
        if (mEncryptInited)
            return sThreadEncryptCipher.get();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "init encrypt cipher", e);
        } catch (NoSuchPaddingException e) {
            Log.d(TAG, "init encrypt cipher", e);
        }
        sThreadEncryptCipher.set(cipher);
        mEncryptInited = true;
        return cipher;
    }

    private Cipher currentDecryptCipher() {
        if (mDecryptInited)
            return sThreadDecryptCipher.get();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "init decrypt cipher", e);
        } catch (NoSuchPaddingException e) {
            Log.d(TAG, "init decrypt cipher", e);
        }
        sThreadDecryptCipher.set(cipher);
        mDecryptInited = true;
        return cipher;
    }

    @Override
    public ByteBuffer allocateBlockBuffer() {
        return ByteBuffer.allocate(BLOCK_SIZE);
    }

    @Override
    public Key createKey() {
        return new Key(KEY_SIZE);
    }

    private java.security.Key toCFBKey(Key key) {
        return new SecretKeySpec(key.data, ALGORITHM);
    }

    private IvParameterSpec toCFBIV(Key key) {
        return new IvParameterSpec(key.data);
    }

    @Override
    public void initKey(Key key) {
        try {
            currentEncryptCipher().init(Cipher.ENCRYPT_MODE, toCFBKey(key), toCFBIV(key));
            currentDecryptCipher().init(Cipher.DECRYPT_MODE, toCFBKey(key), toCFBIV(key));
        } catch (InvalidKeyException e) {
            Log.e(TAG, "init key error: ", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "init key error: ", e);
        }
    }

    @Override
    public int encrypt(ByteBuffer inbuffer, ByteBuffer outbuffer) {

        assert inbuffer.arrayOffset() <= inbuffer.position();
        assert inbuffer.position() < inbuffer.capacity();

        int size = inbuffer.limit() - inbuffer.position();
        if (size % KEY_SIZE != 0) {
            int stolen = KEY_SIZE - size % KEY_SIZE;
            Arrays.fill(inbuffer.array(), inbuffer.limit(), inbuffer.limit() + stolen, (byte) 0);
            inbuffer.limit(inbuffer.limit() + stolen);
        }

        Log.d(TAG, "position:" + inbuffer.position() + ", limit: " + inbuffer.limit());

        outbuffer.clear();
        Log.d(TAG, "output size:" + outbuffer.limit());
        int encryptedSize = 0;
        try {
            encryptedSize = currentEncryptCipher().doFinal(inbuffer, outbuffer);
        } catch (ShortBufferException e) {
            Log.d(TAG, "encrypt error:", e);
        } catch (IllegalBlockSizeException e) {
            Log.d(TAG, "encrypt error:", e);
        } catch (BadPaddingException e) {
            Log.d(TAG, "encrypt error:", e);
        } finally {
            return encryptedSize;
        }
    }

    @Override
    public int decrypt(ByteBuffer inbuffer, ByteBuffer outbuffer) {
        assert inbuffer.arrayOffset() <= inbuffer.position();
        assert inbuffer.position() < inbuffer.capacity();

        int size = inbuffer.limit() - inbuffer.position();
        if (size % KEY_SIZE != 0) {
            int stolen = KEY_SIZE - size % KEY_SIZE;
            Arrays.fill(inbuffer.array(), inbuffer.limit(), inbuffer.limit() + stolen, (byte) 0);
            inbuffer.limit(inbuffer.limit() + stolen);
        }

        Log.d(TAG, "decrypt position:" + inbuffer.position() + ", limit: " + inbuffer.limit());
        outbuffer.clear();
        Log.d(TAG, "decrypt output size:" + outbuffer.limit());
        int encryptedSize = 0;
        try {
            encryptedSize = currentEncryptCipher().doFinal(inbuffer, outbuffer);
        } catch (ShortBufferException e) {
            Log.d(TAG, "decrypt error:", e);
        } catch (IllegalBlockSizeException e) {
            Log.d(TAG, "decrypt error:", e);
        } catch (BadPaddingException e) {
            Log.d(TAG, "decrypt error:", e);
        } finally {
            return encryptedSize;
        }
    }
}

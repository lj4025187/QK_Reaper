package com.fighter.patch;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

    private Cipher currentEncryptCipher() throws Exception {
        if (mEncryptInited)
            return sThreadEncryptCipher.get();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("init encrypt cipher", e);
        } catch (NoSuchPaddingException e) {
            throw new Exception("init encrypt cipher", e);
        }
        sThreadEncryptCipher.set(cipher);
        mEncryptInited = true;
        return cipher;
    }

    private Cipher currentDecryptCipher() throws Exception {
        if (mDecryptInited)
            return sThreadDecryptCipher.get();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("init decrypt cipher", e);
        } catch (NoSuchPaddingException e) {
            throw new Exception("init decrypt cipher", e);
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
    public void initKey(Key key) throws Exception {
        try {
            currentEncryptCipher().init(Cipher.ENCRYPT_MODE, toCFBKey(key), toCFBIV(key));
            currentDecryptCipher().init(Cipher.DECRYPT_MODE, toCFBKey(key), toCFBIV(key));
        } catch (InvalidKeyException e) {
            throw new Exception("init key error", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new Exception("init key error: ", e);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int encrypt(ByteBuffer inbuffer, ByteBuffer outbuffer) throws Exception {

        assert inbuffer.arrayOffset() <= inbuffer.position();
        assert inbuffer.position() < inbuffer.capacity();

        int size = inbuffer.limit() - inbuffer.position();
        if (size % KEY_SIZE != 0) {
            int stolen = KEY_SIZE - size % KEY_SIZE;
            Arrays.fill(inbuffer.array(), inbuffer.limit(), inbuffer.limit() + stolen, (byte) 0);
            inbuffer.limit(inbuffer.limit() + stolen);
            size = inbuffer.limit() - inbuffer.position();
        }

        outbuffer.clear();
        int encryptedSize = 0;
        try {
            while (size > 0) {
                encryptedSize += currentEncryptCipher().update(inbuffer.array(), inbuffer.position(), KEY_SIZE,
                        outbuffer.array(), outbuffer.position());
                size -= KEY_SIZE;
                inbuffer.position(encryptedSize);
                outbuffer.position(encryptedSize);
            }
        } catch (ShortBufferException e) {
            throw new Exception("encrypt error:", e);
        } catch (IllegalBlockSizeException e) {
            throw new Exception("encrypt error:", e);
        } catch (BadPaddingException e) {
            throw new Exception("encrypt error:", e);
        } finally {
            return encryptedSize;
        }
    }

    @Override
    public int decrypt(ByteBuffer inbuffer, ByteBuffer outbuffer) throws Exception {
        assert inbuffer.arrayOffset() <= inbuffer.position();
        assert inbuffer.position() < inbuffer.capacity();

        int size = inbuffer.limit() - inbuffer.position();
        if (size % KEY_SIZE != 0) {
            throw new Exception("inBuffer size is wrong: " + size);
        }

        outbuffer.clear();
        int encryptedSize = 0;
        try {
            while (size > 0) {
                encryptedSize += currentDecryptCipher().update(inbuffer.array(), inbuffer.position(), KEY_SIZE,
                        outbuffer.array(), outbuffer.position());
                size -= KEY_SIZE;
                inbuffer.position(encryptedSize);
                outbuffer.position(encryptedSize);
            }
        } catch (ShortBufferException e) {
            throw new Exception("decrypt error:", e);
        } catch (IllegalBlockSizeException e) {
            throw new Exception("decrypt error:", e);
        } catch (BadPaddingException e) {
            throw new Exception("decrypt error:", e);
        } finally {
            return encryptedSize;
        }
    }
}

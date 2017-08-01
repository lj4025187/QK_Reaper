package com.fighter.common.rc4;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Used for RC4Impl encrypt and decrypt
 *
 * It use javax secure api
 *
 * Created by zhangjg on 17-5-10.
 */

public final class RC4JavaxImpl implements IRC4 {

    private static final String ALGORITHM = "RC4";


    private byte[] key;

    protected RC4JavaxImpl(byte[] key) {
        if (key == null || key.length == 0 ) {
            throw new IllegalArgumentException("key is empty!!!");
        }
        this.key = key;
    }

    /**
     * Encrypt a byte array with RC4Impl algorithm
     *
     * @param toEncrypt origin byte to encrypt
     * @return encrypted string when success or null when fail
     */
    @Override
    public byte[] encrypt(byte[] toEncrypt) {

        if (toEncrypt == null || toEncrypt.length == 0) {
            throw new IllegalArgumentException("toEncrypt bytes is empty!!!");
        }

        try {
            SecretKeySpec sk = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            byte[] bytes = cipher.update(toEncrypt);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt a byte array with RC4Impl algorithm
     *
     * @param toDecrypt encrypted byte array
     * @return decrypted string when success or null when fail
     */
    @Override
    public byte[] decrypt(byte[] toDecrypt) {
        if (toDecrypt == null || toDecrypt.length == 0) {
            throw new IllegalArgumentException("toDecrypt is empty!!!");
        }
        try {
            SecretKeySpec sk = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, sk);
            byte[] decrypted = cipher.update(toDecrypt);
            return  decrypted;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String encryptToBase64(String toEncrypt) {
        if (toEncrypt == null) {
            throw new IllegalArgumentException("toEncrypt is empty");
        }
        byte[] encrypt = encrypt(toEncrypt.getBytes());
        return Base64.encodeToString(encrypt, Base64.DEFAULT);
    }


    @Override
    public String decryptFromBase64(String toDecrypt) {
        if (toDecrypt == null) {
            throw new IllegalArgumentException("toDecrypt is empty");
        }
        byte[] bytes = Base64.decode(toDecrypt, Base64.DEFAULT);
        return new String(decrypt(bytes));
    }
}

package com.fighter.common.rc4;

import android.util.Base64;

/**
 * RC4Impl algorithm implements
 */
public class RC4Impl implements IRC4 {

    private byte[] key;
    private byte[] S;
    private byte[] T;
    private int keylen;

    protected RC4Impl(byte[] key) {
        this.key = key;
    }

    /**
     * Init it before each encrypt and decrypt
     */
    private void init() {

        if (key.length < 1 || key.length > 256) {
            throw new IllegalArgumentException(
                    "key must be between 1 and 256 bytes");
        }

        S = new byte[256];
        T = new byte[256];

        keylen = key.length;
        for (int i = 0; i < 256; i++) {
            S[i] = (byte) i;
            T[i] = key[i % keylen];
        }
        int j = 0;
        byte tmp;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + T[i]) & 0xFF;
            tmp = S[j];
            S[j] = S[i];
            S[i] = tmp;
        }
    }

    @Override
    public byte[] encrypt(final byte[] toEncrypt) {
        if (toEncrypt == null) {
            throw new IllegalArgumentException("toEncrypt is null");
        }
        init();
        final byte[] ciphertext = new byte[toEncrypt.length];
        int i = 0, j = 0, k, t;
        byte tmp;
        for (int counter = 0; counter < toEncrypt.length; counter++) {
            i = (i + 1) & 0xFF;
            j = (j + S[i]) & 0xFF;
            tmp = S[j];
            S[j] = S[i];
            S[i] = tmp;
            t = (S[i] + S[j]) & 0xFF;
            k = S[t];
            ciphertext[counter] = (byte) (toEncrypt[counter] ^ k);
        }
        return ciphertext;
    }

    @Override
    public byte[] decrypt(final byte[] toDecrypt) {
        if (toDecrypt == null) {
            throw new IllegalArgumentException("toDecrypt is null");
        }
        return encrypt(toDecrypt);
    }

    @Override
    public String encryptToBase64(String toEncrypt) {
        if (toEncrypt == null) {
            throw new IllegalArgumentException("toEncrypt is null");
        }
        byte[] encrypt = encrypt(toEncrypt.getBytes());
        return Base64.encodeToString(encrypt, Base64.DEFAULT);
    }

    @Override
    public String decryptFromBase64(String toDecrypt) {
        if (toDecrypt == null) {
            throw new IllegalArgumentException("toDecrypt is null");
        }
        byte[] bytes = Base64.decode(toDecrypt, Base64.DEFAULT);
        return new String(decrypt(bytes));
    }
}
package com.fighter.common.rc4;

/**
 * RC4Impl interface
 *
 * Created by zhangjigang on 2017/5/12.
 */

public interface IRC4 {
    byte[] encrypt (byte[] toEncrypt);
    byte[] decrypt (byte[] toDecrypt);
    String encryptToBase64 (String toEncrypt);
    String decryptFromBase64 (String toDecrypt);
}

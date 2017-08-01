package com.fighter.common.rc4;

/**
 * Used to create RC4Impl instance
 * There may be more than one RC4Impl implements
 *
 * Created by zhangjigang on 2017/5/12.
 */

public class RC4Factory {
    public static IRC4 create(String key) {
        //return new RC4Impl(key.getBytes());
        return new RC4JavaxImpl(key.getBytes());
    }
}

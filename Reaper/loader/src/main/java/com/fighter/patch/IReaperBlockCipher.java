package com.fighter.patch;

import java.nio.ByteBuffer;

/**
 * Created by wxthon on 5/8/17.
 */

public interface IReaperBlockCipher {

    class Key {
        public byte[] data;

        public Key(int size) {
            if (size == 0)
                return;
            data = new byte[size];
        }
    }

    ByteBuffer allocateBlockBuffer();

    Key createKey();

    void initKey(Key key);

    int encrypt(ByteBuffer inbuffer, ByteBuffer outbuffer);

    int decrypt(ByteBuffer inbuffer, ByteBuffer outbuffer);

}

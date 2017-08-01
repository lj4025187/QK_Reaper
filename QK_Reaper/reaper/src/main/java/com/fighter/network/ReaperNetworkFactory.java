package com.fighter.network;

/**
 * Created by zhangjg on 17-6-28.
 */

public class ReaperNetworkFactory {
    public IReaperNetwork getNetwork() {
        return new DefaultReaperNetwork();
    }
}

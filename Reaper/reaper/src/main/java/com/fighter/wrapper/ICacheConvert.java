package com.fighter.wrapper;

public interface ICacheConvert {
    String convertToString(AdResponse adResponse);

    AdResponse convertFromString(String cachedResponse);
}

package com.fighter.wrapper;

public interface ICacheConvert {
    /**
     * 对可缓存对象，序列化为字符串。{@link AdResponse#canCache()}
     * 仅针对未展示过的广告有效
     *
     * @param adResponse
     * @return
     */
    String convertToString(AdResponse adResponse);

    /**
     * 将序列化的对象转为原始广告响应对象
     *
     * @param cachedResponse
     * @return
     */
    AdResponse convertFromString(String cachedResponse);
}

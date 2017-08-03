
package com.fighter.config;

import android.util.Log;

import java.util.Random;

/**
 * + * Created by jia on 7/24/17.
 * +
 */
public class RandomIp {
    public static String getRandomIp() {
        //ip的范围
        int[][] range_ip =
                {
                        {607649792, 608174079},//36.56.0.0-36.63.255.255 安徽省芜湖市
                        {1783627776, 1784676351},//106.80.0.0-106.95.255.255 重庆市重庆市
                        {2035023872, 2035154943},//121.76.0.0-121.77.255.255 上海市浦东新区
                        {2078801920, 2079064063},//123.232.0.0-123.235.255.255 山东省青岛市
                        {-1950089216, -1948778497},//139.196.0.0-139.215.255.255 吉林省
                        {-1425539072, -1425014785},//171.8.0.0-171.15.255.255 河南省郑州市
                        {-1444216832, -1444151297}, //169.235.0.0-169.235.255.255 美国
                        {-770113536, -768606209},//210.25.0.0-210.47.255.255 辽宁省大连市
                        {-569376768, -564133889}, //222.16.0.0-222.95.255.255 江苏省南京市
                };
        //生成一个随机数
        Random random = new Random();
        int index = random.nextInt(9);
        String ip = numToIp(range_ip[index][0] + new Random().nextInt(range_ip[index][1] - range_ip[index][0]));//获取ip
        return ip;
    }

    /**
     * 数字拼接成ip字符串
     *
     * @param ip
     * @return
     */
    private static String numToIp(int ip) {
        int[] b = new int[4];
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        String ip_str = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);
        Log.e("ForTest", "num to ip " + ip_str);
        return ip_str;
    }
}

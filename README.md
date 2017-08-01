# Tracker

## QDAS管理平台
http://qdas.bi.qihoo.net/sdk/index.php?r=Homepage/index
> 账号：360OS 密码：123456



应用管理中可查到Reaper相关信息

- 产品名称：商业化广告SDK（Debug商业化广告SDK）
- 产品包名：com.fighter.reaper
- 活跃口径：用户主动使用
- 产品类型：SDK类
> appkey：dd458505749b2941217ddd59394240e8（Debug：a86c450b76fb8c371afead6410d55534）

## Config
广告配置中心

- 测试后台http://test.partner.360os.com/
- 手机配置host:10.139.232.146 t.adv.os.qiku.com
- 账号:15600158887
- 密码:fighter12345
- 配置即时生效命令:
curl http://10.139.232.146:1323/manual_update

## Loader
版本配置中心
- 后台http://tools.os.corp.qihoo.net/admin/users/dashboard

## 发版注意事项
### loader
```java
@NoProguard
public class Version {

    @NoProguard
    public static String VERSION = "1.0.0";

}
```
常量值VERSION = 1.0.0，用来从版本配置中心http://tools.os.corp.qihoo.net/admin/users/dashboard 获取rr文件的参数

### reaper
```java
@NoProguard
public class BumpVersion {

    // Must be final constant
    public static final int RELEASE = 1;
    public static final int SECOND = 0;
    public static final int REVISION = 0;
    public static final String SUFFIX = "-beta";

	//value返回值用来对比是否需要升级rr文件。
    @NoProguard
    public static String value() {
        String v = "" + RELEASE + "." + SECOND + "." + REVISION;
        if(BuildConfig.DEBUG) {
            if (SUFFIX != null)
                v += SUFFIX;
        }
        return v;
    }

    @NoProguard
    public static boolean isValid() {
        return RELEASE > 0 && SECOND >= 0 && REVISION >= 0;
    }
}
```

```java
public final class ReaperConfig {
	//该值从Reaper.init方法中传入，若为true，从测试环境取配置信息，且聚效需要配置为测试广告位；
    //若为false，从正式环境取配置信息，且聚效广告位需要配置为正式广告位。
    public static boolean TEST_MODE = false;
    //广告配置中心测试环境对应1.0.0-beta版本的密钥
    public static final String TEST_SALT = "e69470b7c6cbe1909bc8a3e19cdaab11";
    //广告配置中心正式环境对应1.0.0版本的密钥
    public static final String RELEASE_SALT = "cf447fe3adac00476ee9";
}
```
### 对应关系如下
reaper.arr|reaper-debug.rr|reaper-release.rr|salt|备注
---|---|---|---|---
1.0.0|1.0.0-beta||e69470b7c6cbe1909bc8a3e19cdaab11|使用中
1.0.0|1.0.0||cf447fe3adac00476ee9244fd30fba74|已弃用
1.0.0||1.0.0|cf447fe3adac00476ee9|发版中
注：rr文件升级版本，需在广告配置中心重新添加版本，并生成对应的salt。
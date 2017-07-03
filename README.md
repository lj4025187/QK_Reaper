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
value值用来对比是否升级rr文件。

```java
public final class ReaperConfig {
	
    public static boolean TEST_MODE = false;
    
    public static final String TEST_SALT = "e69470b7c6cbe1909bc8a3e19cdaab11";
    
    public static final String RELEASE_SALT = "cf447fe3adac00476ee9";
}
```




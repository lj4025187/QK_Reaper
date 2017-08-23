# Reaper API开发文档
`Version 1.0.5`
`Created By wanghaiteng@360.cn`
`Published by FighterTeam`
`Android Studio`

## 开发文档修改记录
版本|修改人员|修改内容|备注|时间
---|---|---|---|---
1.0.0|李晨|初稿|初版功能（aar:1.0.0,rr:1.0.0))|2017.06.15
1.0.1|刘佳|初稿|初版功能（aar:1.0.0,rr:1.0.0)|2017.07.17
1.0.2|刘佳|添加Q/A注意事项|初版功能（aar:1.0.2,rr:1.0.3)|2017.07.26
1.0.3|刘佳|添加广告有效期注意|初版功能（aar:1.0.3,rr:1.0.5)|2017.08.01
1.0.4|刘佳|添加混淆问题注意|初版功能（aar:1.0.3,rr:1.0.6)|2017.08.15
1.0.5|刘佳|新增多图接口|添加华屹（aar:1.0.3,rr:1.0.7)|2017.08.23
### Reaper SDK集成方式
> Reaper SDK需要如下权限

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_UPDATES" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
```
- 应用程序无需修改AndroidManifest.xml（动态权限检测需开发者自行适配）

> 集成reaper.aar

- 将aar文件放入引用 Module 的 libs 目录下，和一般的 jar 文件类似。在集成模块中修改build.gradle，把 libs 目录加入依赖
```xml
repositories {
    flatDir {
        dirs 'libs'
    }
}
```
添加aar依赖
```xml
dependencies {
    compile (name: 'reaper', ext: 'aar')
}
```

> 确保所有权限被授权后，初始化Reaper

##### 有2种方式初始化（前提：权限均已授权）

1.直接继承ReaperApplication，然后用getReaperApi()方法得到ReaperApi请求广告
> - 测试模式下，testMode设置为false，从测试环境拉取配置信息，可以通过adb logcat -s Reaper查看调试日志。移动设备
> 需要配置hosts：**10.139.232.146 t.adv.os.qiku.com**，确保可以ping通该网段后在配置中心申请相关广告位。
>
>	测试环境配置中心地址：http://test.partner.360os.com/html/entrance/allApplications.html
>	
>
> - 量产模式下，testMode设置为true，从正式环境拉取广告位配置信息，调试日志将不会输出。
> 
> 	正式环境配置中心地址：http://partner.360os.com/html/entrance/allApplications.html
>	
><u>注意：测试模式下，**聚效**只能配置测试广告位。发版本时，改值必须置为***false***。 </u>
>	
>权限申请及配置流程请咨询服务器开发人员：**张鑫润，高轩，安三星**

```java
//init 
public class MyApp extends ReaperApplication {
	
    public void onCreate() {
    	super.onCreate();
        // appContext   应用上下文
        // appId        360OS广告平台申请的APP id
        // appKey       360OS广告平台申请的APP key
        // testMode     是否是测试模式，测试模式支持设置配置文件，发版本时置为false
        mReaperApi.init(this, appid, appkey, true);
    }
}

//use
public class MyActivity extends Activity {
	
    protected void requestAd() {
    	MyApp app = (MyApp)getApplication();
        ReaperApi api = app.getReaperApi();
        //使用api
    }
    
}

```

2.Application已经继承了其他Application，使用ReaperInit初始化后，得到ReaperApi请求广告。
```java
//init
public class MyApp extends AnotherApplicaiton {
	
    private ReaperApi mReaperApi;
    
    public void onCreate() {
    	mReaperApi = ReaperInit.init(this);
    }
    
    public ReaperApi getReaperApi() {
    	return mReaperApi;
    }
}

//use
public class MyActivity extends Activity {
	
    private void requestAds() {
    	MyApp app = (MyApp)getApplication();
        ReaperApi api = app.getReaperApi();
        //使用api
        // appContext  应用上下文
        // appId       360OS广告平台申请的APP id
        // appKey      360OS广告平台申请的APP key
        // testMode    是否是测试模式，测试模式支持设置配置文件
        api.init(this, appid, appkey, true);
    }
}
```

> 调用API请求广告

```java
AdRequester adRequester =
                api.getAdRequester(adPositionId, new AdRequester.AdRequestCallback() {
                    @Override
                    public void onSuccess(AdInfo ads) {
                    // 广告请求成功回调
                    }

                    @Override
                    public void onFailed(String errMsg) {
                    // 广告请求失败回调
                    }
                }, true/*是否返回保底广告*/);
// 请求广告
adRequester.requestAd(1/*请求广告的个数，最大个数为5*/);
```
<font color="#ff0000">注：AdInfo广告存在超时时间，可以通过getExtra("expire_time")获取广告有效时间，请在有效时间内处理该广告的相关事件（展示、点击等）</font>
> 展示/点击广告上报

```java
// v               展示广告所使用的view (不传时聚效广告源将无法上报)
adInfo.onAdShow(v);
// @param activity 广告所在activity (不传时聚效无法正常处理点击)
// @param v        广告展示所在view (不传时聚效无法正常处理点击)
// @param downX    按下广告所在view时手指所在的横坐标，若填写负值或超过屏幕横坐标最大值，点击事件、打点事件会出现问题
// @param downY    按下广告所在view时手指所在的纵坐标，若填写负值或超过屏幕纵坐标最大值，点击事件、打点事件会出现问题
// @param upX      离开广告所在view时手指所在的横坐标，若填写负值或超过屏幕横坐标最大值，点击事件、打点事件会出现问题
// @param upY      离开广告所在view时手指所在的纵坐标，若填写负值或超过屏幕纵坐标最大值，点击事件、打点事件会出现问题
adInfo.onAdClicked(activity, v, downX, downY, upX, upY)
```


### 请求广告整体流程

### 广告生命周期
ReaperInit在初始化状态会自动缓存广告，并自动监管广告的有效周期，App在任何时刻均可请求广告，App得到的广告在展示或点击上报后即为失效，如需新广告必须再次请求，App自身无须做缓存。

如果在没网的状态下请求广告，Reaper会保证返回一个广告，而该广告是否有效无法保证，如果缓存依然在有效期内则App可如实上报展示或点击事件，如果由于网络问题且缓存失效，Reaper仍然会返回一条广告，理论上该广告只是用于填充广告位，所以App可根据自身逻辑进行支配该广告（展示/丢掉）。

### Reaper使用注意事项
>- **Q：为什么调用ReaperInit.init(this)后一直返回null呢？**
>- *A：必须保证应用有WRITE/READ_EXTERNAL_STORAGE等权限，最好确保可以在Context对应的应用目录下成功读写文件.*
>- **Q：按照产品需求，一次需要请求大于5条广告，requestAd(num>5)，该怎么处理？**
>- *A：那对不起，按照广告商要求，为了防治恶意请求广告，最多只支持5条，即使requestAd(10000)，依旧返回5条*
>- **Q：调试过程中360OS商业化后台配置成功后，从日志分析可以看到策略一直是null，好尴尬**
>- *A：那需要确定一下*t.adv.os.qiku.com*是否可以ping通，并且hosts确实已经按照文档进行了修改*
>- **Q：如果由于网络环境问题实在获取不到服务器下发的策略，何解？**
>- *A：可以联系相关开发人员，会为您提供一条服务器下发的策略，通过setTragetConfig设置，*
>- **Q：调用广告的点击事件，页面不进行跳转，该怎么办？**
>- *A：说明文档描述，六个参数需要确保满足要求，尤其是四个int触摸值，必须是符合规矩的，建议setOnTouchListener.*
>- **Q：Reaper SDK是否支持插件化或代理方式的调用？**
>- *A：支持，可以在ReaperInit.init(...)方法中的context参数传宿主进程的context，在ReaperApi.init(...)方法中参数context传递实际拉取广告的context.注：第二个context需要具有访问宿主文件夹的能力。*
>- **Q：...天王盖地虎**
>- *A：...宝塔镇河妖...*
>- **Q：... ...**
>- *A：... ...*
>- **Q：...一入糗百深似海**
>- *A：...从此节操是路人...*
>- **Q：Reaper SDK是否会引起应用无响应等异常？**
>- *A：Reaper SDK是不会在UI线程中进行任何耗时操作的。*
>- **Q：假设很不幸的事还是发生了，并确定是由Reaper SDK引发的崩溃，如何处理？**
>- *A：这种致命性的崩溃，概率相当低，如若出现着实抱歉，高抬贵手抓取日志，联系文档开头的邮箱，必当竭诚协作*


### Api使用说明

### ReaperApi
广告SDK API接口类
```java
/**
 * 初始化广告SDK。
 *
 * @param appContext 应用上下文
 * @param appId      360OS广告平台申请的APP id
 * @param appKey     360OS广告平台申请的APP key
 * @param testMode   是否是测试模式，测试模式支持设置配置文件
 */
public void init(Context appContext, String appId,
                     String appKey, boolean testMode);
```                     
```java                     
/**
 * 获取某广告位的广告请求句柄{@link AdRequester}，可通过句柄请求广告
 *
 * @param adPositionId         360OS广告平台申请的广告位ID
 * @param adRequestCallback    广告请求回调
 * @param needHoldAd           是否在无网络或其他异常情况下返回保底广告
 * @return
 */               
public AdRequester getAdRequester(String adPositionId,
AdRequester.AdRequestCallback adRequestCallback, boolean needHoldAd);  
```
```java
/**
 * 设置测试模式使用的Json配置数据
 *
 * @param configJson 测试的目标测试数据
 */
public void setTagetConfig(String configJson);
```
```java
/**
 * 获取设备wifi mac地址
 *
 * @param context  Context上下文
 * @return
 */
public String getMacAddress(Context context);
```                                      
### AdRequester     
广告请求类，一个AdRequester对应一个360OS广告位ID，可通过此类请求对应广告
```java
/**
 * 开始请求广告
 * @param num请求广告的数量，最多支持一次请求5条广告，大于5条返回5条。
 */
public void requestAd(int num);
```
```java
/**
 * 广告请求回调
 */
public interface AdRequestCallback {
	/**
 	 * 广告请求成功
 	 *
 	 * @param adInfo 广告
 	 */
	void onSuccess(AdInfo adInfo);
	
	/**
	 * 广告请求失败
	 *
	 * @param errMsg 失败原因
	 */
	 void onFailed(String errMsg);
}
``` 
### AdInfo
广告信息
```java
//contentType 有如下四种类型，调用如下：
int contentType = adInfo.getContentType();

/**
 * 文字类型
 */
public static final int CONTENT_TYPE_TEXT = 1;
/**
 * 纯图片类型
 */
public static final int CONTENT_TYPE_PICTURE = 2;
/**
 * 图文混合类型
 */
public static final int CONTENT_TYPE_PICTURE_WITH_TEXT = 3; 
/**
 * 视频类型
 */
public static final int CONTENT_TYPE_VIDEO = 4;
/**
 * 多图
 */
public static final int MULTI_PICTURES = 5;
```
> 注：视频类型广告涉及的视频播放、暂停、停止等界面及操作功能需集成人员自行处理。需要跳转至广告详情页时，调用onAdClicked方法即可。

```java
//actionType 有如下两种类型，调用如下：
int actionType = adInfo.getActionType();
/**
 * 点击跳转浏览器
 */
public static final int ACTION_TYPE_BROWSER = 1;  
/**
 * 点击开始下载APP
 */
public static final int ACTION_TYPE_APP_DOWNLOAD = 2;
```
```java
//广告曝光成功后上报，调用如下：
adInfo.onAdShow(view);
/**
 * 广告被展示
 *
 * @param v   展示广告所使用的view (不传时聚效广告源将无法上报)，目前该view
 *            若传null，会打点广告曝光失败
 */
public void onAdShow(View v);
```
```java
//广告被点击后上报，并打开浏览器或下载APP，调用如下：
adInfo.onAdClick(activity, view, downX, downY, upX, upY);
/**
 * 广告被点击，点击后，由SDK处理点击事件(打开浏览器或是开始下载APP)
 * 若 activity，v传为null，不能配置聚效的广告位。
 *
 * @param activity 广告所在activity (不传时聚效无法正常处理点击)
 * @param v        广告展示所在view (不传时聚效无法正常处理点击)
 * @param downX    按下广告所在view时手指所在的横坐标，若填写负值或超过屏幕横坐标最大值，点击事件、打点事件会出现问题
 * @param downY    按下广告所在view时手指所在的纵坐标，若填写负值或超过屏幕纵坐标最大值，点击事件、打点事件会出现问题
 * @param upX      离开广告所在view时手指所在的横坐标，若填写负值或超过屏幕横坐标最大值，点击事件、打点事件会出现问题
 * @param upY      离开广告所在view时手指所在的纵坐标，若填写负值或超过屏幕纵坐标最大值，点击事件、打点事件会出现问题
 */
public void onAdClicked(Activity activity, View v,
                            int downX, int downY,
                            int upX, int upY);
```
```java
adInfo.onAdClose();
/**
 * 广告被用户关闭
 */   
public void onAdClose();
```
```java
/**
 * 用户点击视频广告预览界面，准备开始播放视频
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdCardClick(int position);
```
```java
/**
 * 开始播放视频广告
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdStartPlay(int position);
```
```java
/**
 * 视频广告被暂停
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdPause(int position);
```
```java
/**
 * 视频广告继续播放
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdContinue(int position);
```
```java
/**
 * 视频广告播放完成
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdPlayComplete(int position);
```
```java
/**
 * 视频广告进入全屏播放
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdFullScreen(int position);
```
```java
/**
 * 视频广告播放中途被退出
 *
 * @param position 视频播放器当前播放位置 {@link MediaPlayer#getCurrentPosition()}
 */
public void onVideoAdExit(int position);
```
```java
/**
 * 获取广告返回的内容类型
 *
 * @return 广告内容类型
 * @see #CONTENT_TYPE_TEXT
 * @see #CONTENT_TYPE_PICTURE
 * @see #CONTENT_TYPE_PICTURE_WITH_TEXT
 * @see #CONTENT_TYPE_VIDEO
 */
public int getContentType();
```
```java
/**
 * 获取广告点击后的表现，如跳转浏览器展示网页，或者开始下载APP
 *
 * @return
 * @see #ACTION_TYPE_BROWSER
 * @see #ACTION_TYPE_APP_DOWNLOAD
 */
public int getActionType();
```
```java
/**
 * 返回展示图片的链接，{@link #getImgFile()}可返回已缓存好的图片文件，
 * 在图片文件失效时，可通过此链接重新获取并展示图片
 *
 * @return 图片URL链接
 */
public String getImgUrl();
```
```java
/**
 * 返回展示图片的链接，{@link #getImgFiles()}可返回已缓存好的图片文件，
 * 在图片文件失效时，可通过此链接重新获取并展示图片
 *
 * @return 图片URL链接集合
 */
public List<String> getImgUrls();
```
```java
/**
 * 获取{@link #getImgUrl()}对应的图片文件，可以直接用来展示，不必再下载。
 * 图片文件可能格式包括 {@code .png}、{@code .jpg}、{@code .gif}，
 * 需注意兼容性，如选择{@code glide}等支持{@code .gif}播放的库作为图片展示工具。
 * 图片文件将在成功曝光后删除，调用曝光后，若需重新展示广告，请勿读取文件，而应重新请求新广告。
 * 在图片文件失效时，可通过{@link #getImgUrl()}请求图片。
 *
 * @return 图片文件
 */
public File getImgFile();
```
```java
/**
 * 获取{@link #getImgUrls()}对应的图片文件，可以直接用来展示，不必再下载。
 * 图片文件可能格式包括 {@code .png}、{@code .jpg}、{@code .gif}，
 * 需注意兼容性，如选择{@code glide}等支持{@code .gif}播放的库作为图片展示工具。
 * 图片文件将在成功曝光后删除，调用曝光后，若需重新展示广告，请勿读取文件，而应重新请求新广告。
 * 在图片文件失效时，可通过{@link #getImgUrl()}请求图片。
 *
 * @return 图片文件
 */
public File getImgFiles();
```
```java
/**
 * 对{@link #getContentType()}为{@link #CONTENT_TYPE_VIDEO}内容类型的广告，
 * 可以通过此方法获取视频广告的链接。
 *
 * @return 视频广告链接
 */
public String getVideoUrl();
```
```java
/**
 * 广告标题(仅部分广告存在)
 *
 * @return 广告标题
 */
public String getTitle();
```
```java
/**
 * 广告详细描述(仅部分广告存在)
 *
 * @return 广告描述
 */
public String getDesc();
```
```java
/**
 * 下载APP类的广告，可通过此链接获取APP图标(可能为空)
 *
 * @return APP图标链接
 */
public String getAppIconUrl();
```
```java
/**
 * 下载APP类的广告，获取APP名称(可能为空)
 *
 * @return APP名称
 */
public String getAppName();
```
```java
/**
 * 下载APP类的广告，获取APP的包名(可能为空)
 *
 * @return APP软件包名
 */
public String getAppPackageName();
```
```java
/**
 *　广告的唯一标识
 *
 * @return 返回广告的唯一标识
 */
public String getUuid();
```
```java
/**
 * 判断广告是否可用
 *
 * @return
 */
public boolean isAvailable();
```
```java
/**
 * 对于不满足需求的业务，可通过此方法获取到更多信息。
 * 具体请与我们沟通。
 * 
 * @return 属性value值
 */
public Object getExtra(String key);
```

- 聚效：jx
- 百度联盟：baidu
- 广点通：gdt

key|返回值类型 | 返回值 | 描述 | 有效广告商
---|---|---|---|---
adPosId |String||超盟分配的广告位|jx / baidu / gdt
adLocalAppId|String||对应广告商的真实AppId|jx / baidu / gdt
adPosId |String||“超盟”分配广告位|jx / baidu / gdt 
adLocalPosId|String||真实广告商分配广告位|jx / baidu / gdt
expire_time|Long||广告有效时间(单位：秒)|jx / baidu / gdt
text    |String||扩展字段(副标题)|jx
adName  |String|jx/gdt/baidu|对应的广告源|jx / baidu / gdt
btnText |String||预留按钮文字|jx
download_app_pkg|String||下载app包名|jx / baidu
download_app_name|String||下载应用名称|jx / baidu / gdt
### 混淆问题
如果您需要使用proguard混淆代码，需确保不要混淆SDK的代码，请在集成模块的proguard-rules.pro文件尾部添加如下配置：
> ```xml
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-keep class com.fighter.** { *; }
```
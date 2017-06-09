# Reaper API开发文档
`Version 1.0.0`<p/>
`Created By wanghaiteng@360.cn`<p/>
`Published by FighterTeam`

### Reaper SDK集成方式
> 配置AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
```

> 集成reaper.jar

> 初始化Reaper


##### 有2种方式初始化
1.直接继承ReaperApplication，然后用getReaperApi()方法得到ReaperApi请求广告

```java
//init 
public class MyApp extends ReaperApplication {
	
    public void onCreate() {
    	super.onCreate();
        // appContext 应用上下文
        // appId 360OS广告平台申请的APP id
        // appKey 360OS广告平台申请的APP key
        // testMode 是否是测试模式，测试模式支持设置配置文件
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
        // appContext 应用上下文
        // appId 360OS广告平台申请的APP id
        // appKey 360OS广告平台申请的APP key
        // testMode 是否是测试模式，测试模式支持设置配置文件
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
                });
// 请求广告
adRequester.requestAd();
```

> 展示/点击广告上报

```java
// v 展示广告所使用的view (不传时聚效广告源将无法上报)
adInfo.onAdShow(v);
// activity 广告所在activity (不传时聚效无法正常处理点击)
// v        广告展示所在view (不传时聚效无法正常处理点击)
// downX    广告所在view按下时的x坐标，获取不到填-999
// downY    广告所在view按下时的y坐标，获取不到填-999
// upX      广告所在view抬起时的x坐标，获取不到填-999
// upY      广告所在view抬起时的y坐标，获取不到填-999
adInfo.onAdClicked(activity, v, downX, downY, upX, upY)
```


### 请求广告整体流程

### 广告生命周期
ReaperInit在初始化状态会自动缓存广告，并自动监管广告的有效周期，App在任何时刻均可请求广告，App得到的广告在展示或点击上报后即为失效，如需新广告必须再次请求，App自身无须做缓存。

如果在没网的状态下请求广告，Reaper会保证返回一个广告，而该广告是否有效无法保证，如果缓存依然在有效期内则App可如实上报展示或点击事件，如果由于网络问题且缓存失效，Reaper仍然会返回一条广告，理论上该广告只是用于填充广告位，所以App可根据自身逻辑进行支配该广告（展示/丢掉）。

### Reaper使用注意事项
....

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
 * @param testMode 是否是测试模式，测试模式支持设置配置文件
 */
public void init(Context appContext, String appId,
                     String appKey, boolean testMode);
```                     
```java                     
/**
 * 获取某广告位的广告请求句柄{@link AdRequester}，可通过句柄请求广告
 *
 * @param adPositionId      360OS广告平台申请的广告位ID
 * @param adRequestCallback 广告请求回调
 * @param needHoldAd 是否在无网络或其他异常情况下返回保底广告
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
 */
public void requestAd();
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
```
```java
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
/**
 * 广告被展示
 *
 * @param v 展示广告所使用的view (不传时聚效广告源将无法上报)
 */
public void onAdShow(View v);
```
```java
/**
 * 广告被点击，点击后，由SDK处理点击事件(打开浏览器或是开始下载APP)
 *
 * @param activity 广告所在activity (不传时聚效无法正常处理点击)
 * @param v        广告展示所在view (不传时聚效无法正常处理点击)
 * @param downX    广告所在view按下时的x坐标，获取不到填-999
 * @param downY    广告所在view按下时的y坐标，获取不到填-999
 * @param upX      广告所在view抬起时的x坐标，获取不到填-999
 * @param upY      广告所在view抬起时的y坐标，获取不到填-999
 */
public void onAdClicked(Activity activity, View v,
                            int downX, int downY,
                            int upX, int upY);
```
```java
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
 * 具体请于我们沟通。
 *
 * @param key 属性key值
 * @return 属性value值
 */
public Object getExtra(String key);
```
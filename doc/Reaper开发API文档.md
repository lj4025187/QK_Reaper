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
    }
}
```

> 调用API请求广告

```java
```

> 展示/点击广告上报

```java
```


### 请求广告整体流程

### 广告生命周期
ReaperInit在初始化状态会自动缓存广告，并自动监管广告的有效周期，App在任何时刻均可请求广告，App得到的广告在展示或点击上报后即为失效，如需新广告必须再次请求，App自身无须做缓存。

如果在没网的状态下请求广告，Reaper会保证返回一个广告，而该广告是否有效无法保证，如果缓存依然在有效期内则App可如实上报展示或点击事件，如果由于网络问题且缓存失效，Reaper仍然会返回一条广告，理论上该广告只是用于填充广告位，所以App可根据自身逻辑进行支配该广告（展示/丢掉）。

### Reaper使用注意事项
....

### Api使用说明

### ReaperApi
....
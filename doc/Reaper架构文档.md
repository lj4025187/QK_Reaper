# Reaper
`Version 1.0.0`<p/>
`Created By wanghaiteng@360.cn`<p/>
`Published by FighterTeam`

## 功能介绍
`Reaper`是一个聚合各大广告平台的SDK的高级SDK，Android应用接入该SDK可以使用简介的API获取优质的广告，无须为不同的广告源而烦恼。

## Reaper架构图
![](./Reaper架构图.png?raw=true)

## 组件介绍
### Reaper Api
......

### Reaper Cache
广告SDK的缓存模块，管理不同广告源的缓存广告，设有内存和磁盘缓存确保缓存的有效性和及时性，客户端请求广告优先从缓存中获取，确保客户端能快速拿到广告信息，无缓存广告时从网络请求广告，并同时填充缓存。

### Reaper Loader
........

### Reaper Patch(reaper.rr)
每个reaper.rr文件是一个Reaper Patch，该Patch是Reaper SDK的核心组件，内部有整个聚合SDK的核心逻辑，同时也承载着动态更新的使命，是目前Reaper更新的最新单元（以后可考虑做小拆分更新）。

该Patch是一个内部自定义格式的文件，被完全混淆和加密，部分Android平台上支持使用过程全加固保护。

reaper.rr由自定义工具进行签名和加密打包，出于安全考虑。

reaper.rr可能存在于Reaper.apk(OS内置)，assets/下(独立应用内置)，data/下(动态更新后)等多个地方，选取最佳兼容reaper.jar的高版本进行使用。

### Reaper Wrapper
........

### Reaper Configure
........

### Reaper Tracker
广告效果追踪模块，对每个每条广告的展示，点击以及应用下载等使用情况进行实时上报，方便结算时进行数据对比，打点模块接入集团现有QDAS打点服务。

### Reaper Patch动态更新
需要解决的问题：
1. 支持在4G或WIFI下进行patch更新
2. 支持查询当前最高版本号
3. 支持查询当前最高版本号是否兼容当前reaper.jar 
4. 能够获取兼容当前reaper.jar的最高patch版本号
5. 获取指定版本号相关信息，包括URL
6. 下载新版本的patch，通过URL

>`Request: Reaper/sdk/version?v=1.0.0`返回所有支持v的reaper版本集合

|sdk_version| reaper_version | state |
|:---------:|:--------------:|:-----:|
| 1.0.0	    | 1.0.0		     | enable|
| 1.0.0	    | 2.0.0		     | enable|
| 2.0.0	    | 1.0.0		     | enable|
| 1.0.0	    | 3.0.0		     | enable|

`sdk_version`: reaper.jar中的版本号

`reaper_version`: reaper.rr的版本

`state`:当前对应关系是否有效，可选值:`enable`，`disable`

>`Request: Reaper/reaper/version?v=1.0.0`返回reaper版本为v的相关信息

| reaper_version | update_type | patch_url |
|:--------------:|:-----------:|:---------:|
| 1.0.0 | both | https://cdn.com/reaper-1.0.0.rr |

`update_type`：更新类型，可选字段：`mobile`,`wifi`,`both`

`patch_url`:reaper patch包的url地址

### 在reaper.rr中解决Assetsh和Resources问题
有些第三方广告SDK需要在assets中放置一些自己的集成文件（如：聚效SDK），而由于Reaper支持动态更新，所以这些文件必须放置到reaper.rr中进行动态更新，所以为了放置第三方SDK在寻找assets文件失败时，reaper.rr中必须对Context、AssetManager、Resources等进行代理，从而将reaper.rr中的真实文件返回给第三方SDK。

### 在reaper.rr中解决so问题
有些第三方广告SDK会依赖so文件，所以reaper.rr中会携带有so文件，而且在第三方SDK需要加载so时会动态找到真实的依赖so，这点使用ReaperClassLoader已经支持。

## 使用示例

## 联系我们
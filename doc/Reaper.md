# Reaper

## Version：1.0

## 功能介绍
`Reaper`是一个聚合各大广告平台的SDK，Android应用接入该SDK可以使用简介的API获取优质的广告，无须为不同的广告源而烦恼。

## Api接口说明
### Reaper Api
......

### Reaper Cache
.......

### Reaper Loader
........

### Reaper Patch
........

### Reaper Wrapper
........

### Reaper Configure
........

### Reaper Tracker
........

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
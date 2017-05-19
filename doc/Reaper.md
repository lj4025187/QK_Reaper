# Reaper

## Version：1.0

## 功能介绍
`Reaper`是一个聚合各大广告平台的SDK，Android应用接入该SDK可以使用简介的API获取优质的广告，无须为不同的广告源而烦恼。

## Api接口说明

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

`patch_url`:reaper patch包的url地址，

## 使用示例

## 联系我们